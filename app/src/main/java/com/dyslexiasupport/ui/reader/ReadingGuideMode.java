package com.dyslexiasupport.ui.reader;

import androidx.annotation.Nullable;

/** OFF = nothing, RULER = tinted band, FOCUS = band + everything else dimmed. */
public enum ReadingGuideMode {
    OFF, RULER, FOCUS;

    public static ReadingGuideMode fromName(@Nullable String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return OFF;
        }
    }
}
