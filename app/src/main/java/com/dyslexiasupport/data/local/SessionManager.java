package com.dyslexiasupport.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.model.User;

public class SessionManager {

    private static final String PREF_NAME = "dyslexia_session";
    private static final String KEY_SIGNED_IN = "signed_in";
    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_AVATAR = "avatar";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return prefs.getBoolean(KEY_SIGNED_IN, false);
    }

    public void signIn(User user) {
        if (user == null) return;
        prefs.edit()
                .putBoolean(KEY_SIGNED_IN, true)
                .putBoolean(KEY_IS_GUEST, user.isGuest())
                .putString(KEY_NAME, user.getName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_ROLE, user.getRole() != null ? user.getRole().name() : Role.LEARNER.name())
                .putString(KEY_AVATAR, user.getAvatar())
                .apply();
    }

    public void signInAsGuest(String guestName) {
        prefs.edit()
                .putBoolean(KEY_SIGNED_IN, true)
                .putBoolean(KEY_IS_GUEST, true)
                .putString(KEY_NAME, guestName)
                .putString(KEY_EMAIL, "")
                .putString(KEY_ROLE, Role.LEARNER.name())
                .putString(KEY_AVATAR, "👤")
                .apply();
    }

    public void signOut() {
        prefs.edit().clear().apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "👤");
    }

    public Role getRole() {
        String roleStr = prefs.getString(KEY_ROLE, Role.LEARNER.name());
        try {
            return Role.valueOf(roleStr);
        } catch (Exception e) {
            return Role.LEARNER;
        }
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }
}
