package com.dyslexiasupport.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.auth.AuthResult;
import com.dyslexiasupport.data.auth.LocalAuthRepository;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.databinding.ActivityRegisterBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.common.UiUtils;
import com.dyslexiasupport.ui.home.MainActivity;
import com.google.android.material.chip.Chip;

public class RegisterActivity extends BaseActivity {

    private static final int MIN_PASSWORD = 6;

    private ActivityRegisterBinding binding;
    private LocalAuthRepository auth;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());

        auth = new LocalAuthRepository(this);
        session = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });
        binding.btnHaveAccount.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        binding.layoutName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);

        String name = UiUtils.textOf(binding.inputName);
        String email = UiUtils.textOf(binding.inputEmail);
        String password = UiUtils.textOf(binding.inputPassword);

        boolean valid = true;
        if (name.isEmpty()) {
            binding.layoutName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (password.length() < MIN_PASSWORD) {
            binding.layoutPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!valid) {
            return;
        }

        AuthResult result = auth.register(name, email, password, selectedRole(), selectedAvatar());
        if (result.isSuccess()) {
            session.signIn(result.user);
            MainActivity.launchFresh(this);
        } else {
            binding.layoutEmail.setError(getString(result.errorRes));
        }
    }

    private Role selectedRole() {
        int id = binding.roleGroup.getCheckedChipId();
        if (id == R.id.chipRoleParent) {
            return Role.PARENT;
        } else if (id == R.id.chipRoleTeacher) {
            return Role.TEACHER;
        } else if (id == R.id.chipRoleLearner) {
            return Role.LEARNER;
        }
        return Role.CHILD;
    }

    private String selectedAvatar() {
        Chip chip = binding.avatarGroup.findViewById(binding.avatarGroup.getCheckedChipId());
        return chip != null ? chip.getText().toString() : "\uD83D\uDC3C";
    }
}
