package com.dyslexiasupport.data.auth;

import androidx.annotation.StringRes;

import com.dyslexiasupport.data.model.User;

public class AuthResult {
    private final boolean success;
    public final User user;
    @StringRes
    public final int errorRes;

    public static AuthResult success(User user) {
        return new AuthResult(true, user, 0);
    }

    public static AuthResult failure(@StringRes int errorRes) {
        return new AuthResult(false, null, errorRes);
    }

    private AuthResult(boolean success, User user, @StringRes int errorRes) {
        this.success = success;
        this.user = user;
        this.errorRes = errorRes;
    }

    public boolean isSuccess() {
        return success;
    }
}
