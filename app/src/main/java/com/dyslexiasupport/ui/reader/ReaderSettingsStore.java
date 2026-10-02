package com.dyslexiasupport.ui.reader;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/** Saves reader settings on the device (SharedPreferences). */
public class ReaderSettingsStore {

    private static final String PREFS = "reader_settings";
    private static final String K_SIZE = "font_size_sp";
    private static final String K_LINE = "line_spacing";
    private static final String K_WORD = "word_spacing_dp";
    private static final String K_LETTER = "letter_spacing_em";
    private static final String K_THEME = "theme";
    private static final String K_GUIDE_MODE = "guide_mode";
    private static final String K_GUIDE_LINES = "guide_lines";
    private static final String K_SYLLABLES = "syllable_colors";

    private final SharedPreferences prefs;

    public ReaderSettingsStore(@NonNull Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public ReaderSettings load() {
        ReaderSettings d = ReaderSettings.defaults();
        ReaderSettings s = new ReaderSettings();
        s.fontSizeSp = prefs.getInt(K_SIZE, d.fontSizeSp);
        s.lineSpacing = prefs.getFloat(K_LINE, d.lineSpacing);
        s.wordSpacingDp = prefs.getInt(K_WORD, d.wordSpacingDp);
        s.letterSpacingEm = prefs.getFloat(K_LETTER, d.letterSpacingEm);
        s.theme = ReaderTheme.fromName(prefs.getString(K_THEME, null));
        s.guideMode = ReadingGuideMode.fromName(prefs.getString(K_GUIDE_MODE, null));
        s.guideLines = prefs.getInt(K_GUIDE_LINES, d.guideLines);
        s.syllableColors = prefs.getBoolean(K_SYLLABLES, d.syllableColors);
        return s.sanitized();
    }

    public void save(@NonNull ReaderSettings s) {
        prefs.edit()
                .putInt(K_SIZE, s.fontSizeSp)
                .putFloat(K_LINE, s.lineSpacing)
                .putInt(K_WORD, s.wordSpacingDp)
                .putFloat(K_LETTER, s.letterSpacingEm)
                .putString(K_THEME, s.theme.name())
                .putString(K_GUIDE_MODE, s.guideMode.name())
                .putInt(K_GUIDE_LINES, s.guideLines)
                .putBoolean(K_SYLLABLES, s.syllableColors)
                .apply();
    }
}
