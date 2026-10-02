package com.dyslexiasupport.data.library;

import androidx.annotation.NonNull;

/**
 * Data model for a reading text item in the library.
 */
public class ReadingText {

    @NonNull
    public final String id;
    @NonNull
    public final String title;
    @NonNull
    public final String body;
    @NonNull
    public final String emoji;
    public final int level;
    @NonNull
    public final String lang;

    public ReadingText(@NonNull String id, @NonNull String title, @NonNull String body,
                       @NonNull String emoji, int level, @NonNull String lang) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.emoji = emoji;
        this.level = level;
        this.lang = lang;
    }

    public boolean isBangla() {
        return "bn".equalsIgnoreCase(lang) || "bangla".equalsIgnoreCase(lang);
    }

    public int minutes() {
        if (body.trim().isEmpty()) {
            return 1;
        }
        String[] words = body.trim().split("\\s+");
        return Math.max(1, (int) Math.ceil(words.length / 80.0));
    }
}
