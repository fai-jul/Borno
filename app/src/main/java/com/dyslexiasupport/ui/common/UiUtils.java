package com.dyslexiasupport.ui.common;

import android.widget.TextView;

import androidx.annotation.Nullable;

public final class UiUtils {

    private UiUtils() {
    }

    /** Trimmed text of a TextView/EditText, never null. */
    public static String textOf(@Nullable TextView view) {
        if (view == null || view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }
}
