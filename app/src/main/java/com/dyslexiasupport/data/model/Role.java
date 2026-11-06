package com.dyslexiasupport.data.model;

import androidx.annotation.StringRes;

import com.dyslexiasupport.R;

public enum Role {
    CHILD(R.string.role_child),
    PARENT(R.string.role_parent),
    TEACHER(R.string.role_teacher);

    @StringRes
    public final int labelRes;

    Role(@StringRes int labelRes) {
        this.labelRes = labelRes;
    }
}
