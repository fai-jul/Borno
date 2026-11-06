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
    @NonNull
    public final String category; // "demo", "nctb", "scanned"

    public ReadingText(@NonNull String id, @NonNull String title, @NonNull String body,
                       @NonNull String emoji, int level, @NonNull String lang) {
        this(id, title, body, emoji, level, lang, id.startsWith("nctb_") ? "nctb" : "demo");
    }

    public ReadingText(@NonNull String id, @NonNull String title, @NonNull String body,
                       @NonNull String emoji, int level, @NonNull String lang, @NonNull String category) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.emoji = emoji;
        this.level = level;
        this.lang = lang;
        this.category = category;
    }

    public boolean isBangla() {
        return "bn".equalsIgnoreCase(lang) || "bangla".equalsIgnoreCase(lang);
    }

    public boolean isNctb() {
        return "nctb".equalsIgnoreCase(category) || id.startsWith("nctb_");
    }

    /**
     * Calculates the expected reading duration from this topic's actual body text.
     * The result is deliberately kept in seconds so short topics do not all become "1 min".
     */
    public int readingTimeSeconds() {
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }

        int wordCount = trimmed.split("\\s+").length;
        // Bangla text generally needs more processing time than English text.
        int baseWpm = isBangla() ? 105 : 130;
        double difficultyFactor = level <= 1 ? 1.20 : level == 2 ? 1.08 : 1.0;
        double readingSeconds = (wordCount * 60.0 / baseWpm) * difficultyFactor;

        // Sentence punctuation adds a short, measurable pause to the reading time.
        int pauses = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (character == '.' || character == '!' || character == '?'
                    || character == '।' || character == ';' || character == ':') {
                pauses++;
            }
        }
        readingSeconds += pauses * 0.35;
        return Math.max(1, (int) Math.ceil(readingSeconds));
    }

    public int minutes() {
        return Math.max(1, (int) Math.round(readingTimeSeconds() / 60.0));
    }

    @NonNull
    public String timeFormatted(@NonNull android.content.Context context) {
        int totalSec = readingTimeSeconds();
        if (totalSec < 60) {
            return context.getString(com.dyslexiasupport.R.string.library_seconds, totalSec);
        } else {
            int mins = totalSec / 60;
            int secs = totalSec % 60;
            if (secs > 0 && mins < 5) {
                return context.getString(com.dyslexiasupport.R.string.library_min_sec, mins, secs);
            } else {
                return context.getString(com.dyslexiasupport.R.string.library_minutes, mins);
            }
        }
    }
}
