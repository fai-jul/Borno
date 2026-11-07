package com.dyslexiasupport.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.firebase.CloudSyncManager;
import com.dyslexiasupport.data.firebase.LinkCodeManager;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.model.User;
import com.dyslexiasupport.databinding.ActivityLoginBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.common.UiUtils;
import com.dyslexiasupport.ui.home.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.concurrent.Executor;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;
    private GoogleSignInClient googleSignInClient;
    private boolean submitting;
    private Executor biometricExecutor;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            firebaseAuthWithGoogle(account);
                        } else {
                            Toast.makeText(this, R.string.login_google_failed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.login_google_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setSubmitting(false);
                }
            });

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

        session = new SessionManager(this);
        biometricExecutor = ContextCompat.getMainExecutor(this);

        setupGoogleSignIn();
        setupBiometricLogin();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnGoogleLogin.setOnClickListener(v -> attemptGoogleLogin());
        binding.btnForgotPassword.setOnClickListener(v -> attemptForgotPassword());

        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
        binding.btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupBiometricLogin() {
        boolean available = BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                == BiometricManager.BIOMETRIC_SUCCESS;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean enabled = session.isBiometricEnabled() && firebaseUser != null;
        binding.btnBiometricLogin.setVisibility(available && enabled ? android.view.View.VISIBLE
                : android.view.View.GONE);
        binding.btnBiometricLogin.setOnClickListener(v -> authenticateBiometric());
    }

    private void authenticateBiometric() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || !session.isBiometricEnabled()) {
            binding.btnBiometricLogin.setVisibility(android.view.View.GONE);
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(this, biometricExecutor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        setSubmitting(true);
                        session.setFirebaseUid(firebaseUser.getUid());
                        User user = new User(
                                session.getName().isEmpty() ? getString(R.string.guest_name)
                                        : session.getName(),
                                session.getEmail().isEmpty() && firebaseUser.getEmail() != null
                                        ? firebaseUser.getEmail() : session.getEmail(),
                                session.getRole(), session.getAvatar(), false);
                        session.signIn(user);
                        LinkCodeManager.ensureUserLinkCode(LoginActivity.this, user);
                        CloudSyncManager.syncAll(LoginActivity.this, success -> {});
                        MainActivity.launchFresh(LoginActivity.this);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        setSubmitting(false);
                    }
                });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.login_biometric_title))
                .setSubtitle(getString(R.string.login_biometric_subtitle))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        prompt.authenticate(promptInfo);
    }

    private void setupGoogleSignIn() {
        String defaultWebClientId = getString(R.string.default_web_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(defaultWebClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void attemptGoogleLogin() {
        if (!submitting && googleSignInClient != null) {
            setSubmitting(true);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && FirebaseAuth.getInstance().getCurrentUser() != null) {
                        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                        String name = account.getDisplayName() != null ? account.getDisplayName() : "Friend";
                        String email = account.getEmail() != null ? account.getEmail() : "";
                        User user = new User(name, email, Role.CHILD, "👤", false);

                        session.signIn(user);
                        session.setFirebaseUid(fbUser.getUid());

                        LinkCodeManager.ensureUserLinkCode(LoginActivity.this, user);
                        CloudSyncManager.syncAll(LoginActivity.this, success -> {});

                        MainActivity.launchFresh(LoginActivity.this);
                    } else {
                        setSubmitting(false);
                        Toast.makeText(this, R.string.login_google_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptForgotPassword() {
        binding.layoutEmail.setError(null);
        String email = UiUtils.textOf(binding.inputEmail).trim().toLowerCase(java.util.Locale.ROOT);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.login_reset_email_sent, Toast.LENGTH_LONG).show();
                    } else {
                        String msg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage() : getString(R.string.error_generic);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptLogin() {
        if (submitting) return;
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);

        String email = UiUtils.textOf(binding.inputEmail).trim().toLowerCase(java.util.Locale.ROOT);
        String password = UiUtils.textOf(binding.inputPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            return;
        }
        if (password.isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.error_password_short));
            return;
        }

        setSubmitting(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getUser() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        User user = new User(firebaseUser.getDisplayName() != null
                                ? firebaseUser.getDisplayName() : getString(R.string.guest_name),
                                email, Role.CHILD, "👤", false);
                        if (!firebaseUser.isEmailVerified()) {
                            setSubmitting(false);
                            startActivity(new Intent(this, EmailVerificationActivity.class)
                                    .putExtra(EmailVerificationActivity.EXTRA_EMAIL, email)
                                    .putExtra(EmailVerificationActivity.EXTRA_NAME, user.getName())
                                    .putExtra(EmailVerificationActivity.EXTRA_ROLE, user.getRole().name())
                                    .putExtra(EmailVerificationActivity.EXTRA_AVATAR, user.getAvatar()));
                            return;
                        }
                        session.signIn(user);
                        session.setFirebaseUid(firebaseUser.getUid());
                        session.setBiometricEnabled(true);
                        LinkCodeManager.ensureUserLinkCode(this, user);
                        CloudSyncManager.syncAll(this, success -> {});
                        MainActivity.launchFresh(this);
                    } else {
                        setSubmitting(false);
                        binding.layoutPassword.setError(firebaseError(task.getException()));
                    }
                });
    }

    private void setSubmitting(boolean value) {
        submitting = value;
        binding.btnLogin.setEnabled(!value);
        binding.btnGoogleLogin.setEnabled(!value);
        binding.btnBiometricLogin.setEnabled(!value);
        binding.btnCreate.setEnabled(!value);
        binding.btnForgotPassword.setEnabled(!value);
    }

    private String firebaseError(Exception error) {
        if (error == null) {
            return getString(R.string.error_generic);
        }
        if (error instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
                || error instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            return getString(R.string.error_login_failed);
        }
        if (error instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) error).getErrorCode();
            if ("ERROR_USER_NOT_FOUND".equals(code) || "ERROR_WRONG_PASSWORD".equals(code)
                    || "ERROR_INVALID_CREDENTIAL".equals(code)
                    || "ERROR_INVALID_LOGIN_CREDENTIAL".equals(code)
                    || "invalid-credential".equalsIgnoreCase(code)
                    || "user-not-found".equalsIgnoreCase(code)
                    || "wrong-password".equalsIgnoreCase(code)) {
                return getString(R.string.error_login_failed);
            }
            if ("ERROR_TOO_MANY_REQUESTS".equals(code) || "too-many-requests".equalsIgnoreCase(code)) {
                return getString(R.string.error_auth_too_many_requests);
            }
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(code) || "operation-not-allowed".equalsIgnoreCase(code)) {
                return "Email/Password sign-in is disabled in Firebase Console.";
            }
            if ("ERROR_INVALID_EMAIL".equals(code) || "invalid-email".equalsIgnoreCase(code)) {
                return getString(R.string.error_email_invalid);
            }
        }
        if (error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
            return error.getMessage();
        }
        return getString(R.string.error_generic);
    }
}
