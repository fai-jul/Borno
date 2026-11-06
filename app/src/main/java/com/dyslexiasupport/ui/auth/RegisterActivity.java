package com.dyslexiasupport.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.model.User;
import com.dyslexiasupport.databinding.ActivityRegisterBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.common.UiUtils;
import com.dyslexiasupport.ui.home.MainActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private static final int MIN_PASSWORD = 6;

    private ActivityRegisterBinding binding;
    private SessionManager session;
    private boolean submitting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());

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
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        if (submitting) return;
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

        Role role = selectedRole();
        String avatar = selectedAvatar();
        setSubmitting(true);
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null
                            || task.getResult().getUser() == null) {
                        setSubmitting(false);
                        binding.layoutEmail.setError(firebaseError(task.getException()));
                        return;
                    }
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    firebaseUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build())
                            .addOnCompleteListener(profileTask -> {
                                if (!profileTask.isSuccessful()) {
                                    setSubmitting(false);
                                    binding.layoutName.setError(getString(R.string.error_generic));
                                    return;
                                }
                                User user = new User(name, email, role, avatar, false);
                                session.setFirebaseUid(firebaseUser.getUid());
                                com.dyslexiasupport.data.firebase.LinkCodeManager
                                        .ensureUserLinkCode(this, user);
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (!emailTask.isSuccessful()) {
                                                setSubmitting(false);
                                                binding.layoutEmail.setError(getString(R.string.error_generic));
                                                return;
                                            }
                                            startActivity(new android.content.Intent(
                                                    this, EmailVerificationActivity.class)
                                                    .putExtra(EmailVerificationActivity.EXTRA_EMAIL, email)
                                                    .putExtra(EmailVerificationActivity.EXTRA_NAME, name)
                                                    .putExtra(EmailVerificationActivity.EXTRA_ROLE, role.name())
                                                    .putExtra(EmailVerificationActivity.EXTRA_AVATAR, avatar));
                                        });
                            });
                });
    }

    private void setSubmitting(boolean value) {
        submitting = value;
        binding.btnRegister.setEnabled(!value);
        binding.btnHaveAccount.setEnabled(!value);
        binding.btnBack.setEnabled(!value);
    }

    private Role selectedRole() {
        int id = binding.roleGroup.getCheckedChipId();
        if (id == R.id.chipRoleParent) {
            return Role.PARENT;
        } else if (id == R.id.chipRoleTeacher) {
            return Role.TEACHER;
        }
        return Role.CHILD;
    }

    private String selectedAvatar() {
        Chip chip = binding.avatarGroup.findViewById(binding.avatarGroup.getCheckedChipId());
        return chip != null ? chip.getText().toString() : "\uD83D\uDC3C";
    }

    private String firebaseError(Exception error) {
        if (error instanceof com.google.firebase.auth.FirebaseAuthException) {
            String code = ((com.google.firebase.auth.FirebaseAuthException) error).getErrorCode();
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                return getString(R.string.error_email_taken);
            }
            if ("ERROR_WEAK_PASSWORD".equals(code)) {
                return getString(R.string.error_password_short);
            }
            if ("ERROR_TOO_MANY_REQUESTS".equals(code)) {
                return getString(R.string.error_auth_too_many_requests);
            }
        }
        return getString(R.string.error_generic);
    }
}
