package com.dyslexiasupport.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.model.User;

public class LocalAuthRepository {

    private static final String PREF_NAME = "dyslexia_users";
    private static final String PREFIX_USER = "user_";
    private final SharedPreferences prefs;

    public LocalAuthRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public AuthResult login(String email, String password) {
        String key = PREFIX_USER + email.toLowerCase().trim();
        String storedPassword = prefs.getString(key + "_pass", null);
        if (storedPassword == null || !storedPassword.equals(password)) {
            return AuthResult.failure(R.string.error_login_failed);
        }

        String name = prefs.getString(key + "_name", "User");
        String roleStr = prefs.getString(key + "_role", Role.LEARNER.name());
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            role = Role.LEARNER;
        }
        String avatar = prefs.getString(key + "_avatar", "🐼");

        User user = new User(name, email, role, avatar, false);
        return AuthResult.success(user);
    }

    public AuthResult register(String name, String email, String password, Role role, String avatar) {
        String cleanEmail = email.toLowerCase().trim();
        String key = PREFIX_USER + cleanEmail;
        if (prefs.contains(key + "_pass")) {
            return AuthResult.failure(R.string.error_email_taken);
        }

        prefs.edit()
                .putString(key + "_pass", password)
                .putString(key + "_name", name)
                .putString(key + "_role", role != null ? role.name() : Role.LEARNER.name())
                .putString(key + "_avatar", avatar != null ? avatar : "🐼")
                .apply();

        User user = new User(name, cleanEmail, role, avatar, false);
        return AuthResult.success(user);
    }
}
