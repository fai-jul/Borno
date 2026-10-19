package com.dyslexiasupport.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.auth.AuthResult;
import com.dyslexiasupport.data.auth.LocalAuthRepository;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.databinding.ActivityLoginBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.common.UiUtils;
import com.dyslexiasupport.ui.home.MainActivity;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private LocalAuthRepository auth;
    private SessionManager session;

    /** Opens the login screen and clears the back stack (used after logout). */
    public static void launchFresh(Activity from) {
        Intent intent = new Intent(from, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());

        auth = new LocalAuthRepository(this);
        session = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
        binding.btnGuest.setOnClickListener(v -> {
            session.signInAsGuest(getString(R.string.guest_name));
            MainActivity.launchFresh(this);
        });
        binding.btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);

        String email = UiUtils.textOf(binding.inputEmail);
        String password = UiUtils.textOf(binding.inputPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            return;
        }
        if (password.isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.error_password_short));
            return;
        }

        AuthResult result = auth.login(email, password);
        if (result.isSuccess()) {
            session.signIn(result.user);
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                                String uid = task.getResult().getUser().getUid();
                                session.setFirebaseUid(uid);
                                com.dyslexiasupport.data.firebase.LinkCodeManager.ensureUserLinkCode(LoginActivity.this, result.user);
                                com.dyslexiasupport.data.firebase.CloudSyncManager.syncAll(LoginActivity.this, success -> {});
                            }
                        });
            } catch (Exception ignored) {}
            MainActivity.launchFresh(this);
        } else {
            binding.layoutPassword.setError(getString(result.errorRes));
        }
    }
}
