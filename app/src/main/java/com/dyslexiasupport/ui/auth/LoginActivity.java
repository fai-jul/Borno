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

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.auth.AuthResult;
import com.dyslexiasupport.data.auth.LocalAuthRepository;
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

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private LocalAuthRepository auth;
    private SessionManager session;
    private GoogleSignInClient googleSignInClient;

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

        auth = new LocalAuthRepository(this);
        session = new SessionManager(this);

        setupGoogleSignIn();

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
        binding.btnGuest.setOnClickListener(v -> {
            session.signInAsGuest(getString(R.string.guest_name));
            MainActivity.launchFresh(this);
        });
        binding.btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
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
        if (googleSignInClient != null) {
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
                        User user = new User(name, email, Role.LEARNER, "👤", false);

                        session.signIn(user);
                        session.setFirebaseUid(fbUser.getUid());

                        LinkCodeManager.ensureUserLinkCode(LoginActivity.this, user);
                        CloudSyncManager.syncAll(LoginActivity.this, success -> {});

                        MainActivity.launchFresh(LoginActivity.this);
                    } else {
                        Toast.makeText(this, R.string.login_google_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptForgotPassword() {
        binding.layoutEmail.setError(null);
        String email = UiUtils.textOf(binding.inputEmail);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.login_reset_email_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                });
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
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                                String uid = task.getResult().getUser().getUid();
                                session.setFirebaseUid(uid);
                                LinkCodeManager.ensureUserLinkCode(LoginActivity.this, result.user);
                                CloudSyncManager.syncAll(LoginActivity.this, success -> {});
                            }
                        });
            } catch (Exception ignored) {}
            MainActivity.launchFresh(this);
        } else {
            binding.layoutPassword.setError(getString(result.errorRes));
        }
    }
}
