package com.dyslexiasupport.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.model.User;
import java.util.UUID;

public class SessionManager {

    private static final String PREF_NAME = "dyslexia_session";
    private static final String KEY_SIGNED_IN = "signed_in";
    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_LINK_CODE = "link_code";
    private static final String KEY_FIREBASE_UID = "firebase_uid";
    private static final String KEY_LINKED_COUNT = "linked_count";
    private static final String KEY_LOCAL_OWNER = "local_owner";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_DAILY_GOAL_MODE = "daily_goal_mode";
    private static final String KEY_ASSISTANT_ENABLED = "assistant_enabled";
    private static final String KEY_READING_ONLY = "reading_only";
    private static final String KEY_ASSIGNED_TEXT = "assigned_text_id";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

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
                .putString(KEY_ROLE, user.getRole() != null ? user.getRole().name() : Role.CHILD.name())
                .putString(KEY_AVATAR, user.getAvatar())
                .apply();
    }

    public void signOut() {
        prefs.edit().clear().apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "👤");
    }

    public Role getRole() {
        String roleStr = prefs.getString(KEY_ROLE, Role.CHILD.name());
        try {
            return Role.valueOf(roleStr);
        } catch (Exception e) {
            return Role.CHILD;
        }
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }

    public String getLinkCode() {
        return prefs.getString(KEY_LINK_CODE, "");
    }

    public void setLinkCode(String code) {
        prefs.edit().putString(KEY_LINK_CODE, code).apply();
    }

    public String getFirebaseUid() {
        return prefs.getString(KEY_FIREBASE_UID, "");
    }

    /** Stable owner key for local data; Firebase UID takes precedence after cloud sign-in. */
    public String getOwnerId() {
        String firebaseUid = getFirebaseUid();
        if (!firebaseUid.isEmpty()) {
            return firebaseUid;
        }
        String owner = prefs.getString(KEY_LOCAL_OWNER, "");
        if (owner.isEmpty()) {
            owner = "local_" + UUID.randomUUID();
            prefs.edit().putString(KEY_LOCAL_OWNER, owner).apply();
        }
        return owner;
    }

    public void setFirebaseUid(String uid) {
        prefs.edit().putString(KEY_FIREBASE_UID, uid).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public int getLinkedCount() {
        return prefs.getInt(KEY_LINKED_COUNT, 0);
    }

    public void setLinkedCount(int count) {
        prefs.edit().putInt(KEY_LINKED_COUNT, count).apply();
    }

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 10);
    }

    public void setDailyGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_GOAL, Math.max(1, goal)).apply();
    }

    public boolean isGoalInExercises() {
        return prefs.getBoolean(KEY_DAILY_GOAL_MODE, false);
    }

    public void setGoalInExercises(boolean exercises) {
        prefs.edit().putBoolean(KEY_DAILY_GOAL_MODE, exercises).apply();
    }

    public boolean isAssistantEnabled() {
        return prefs.getBoolean(KEY_ASSISTANT_ENABLED, true);
    }

    public void setAssistantEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ASSISTANT_ENABLED, enabled).apply();
    }

    public boolean isReadingOnly() {
        return prefs.getBoolean(KEY_READING_ONLY, false);
    }

    public void setReadingOnly(boolean enabled) {
        prefs.edit().putBoolean(KEY_READING_ONLY, enabled).apply();
    }

    public String getAssignedTextId() {
        return prefs.getString(KEY_ASSIGNED_TEXT, "");
    }

    public void setAssignedTextId(String textId) {
        prefs.edit().putString(KEY_ASSIGNED_TEXT, textId == null ? "" : textId).apply();
    }
}
