package com.dyslexiasupport.ui.reader;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.dyslexiasupport.R;

/** Reading background + text colour pairs (+ the accent used for coloured syllables). */
public enum ReaderTheme {
    CREAM(R.color.reader_cream_bg, R.color.reader_cream_text, R.color.reader_accent_light,
            R.string.theme_cream, false),
    YELLOW(R.color.reader_yellow_bg, R.color.reader_yellow_text, R.color.reader_accent_light,
            R.string.theme_yellow, false),
    MINT(R.color.reader_mint_bg, R.color.reader_mint_text, R.color.reader_accent_light,
            R.string.theme_mint, false),
    SKY(R.color.reader_sky_bg, R.color.reader_sky_text, R.color.reader_accent_light,
            R.string.theme_sky, false),
    PEACH(R.color.reader_peach_bg, R.color.reader_peach_text, R.color.reader_accent_light,
            R.string.theme_peach, false),
    NIGHT(R.color.reader_night_bg, R.color.reader_night_text, R.color.reader_accent_night,
            R.string.theme_night, true);

    @ColorRes public final int bgRes;
    @ColorRes public final int textRes;
    @ColorRes public final int accentRes;
    @StringRes public final int nameRes;
    /** true when the background is dark (status-bar icons must turn light). */
    public final boolean dark;

    ReaderTheme(@ColorRes int bgRes, @ColorRes int textRes, @ColorRes int accentRes,
                @StringRes int nameRes, boolean dark) {
        this.bgRes = bgRes;
        this.textRes = textRes;
        this.accentRes = accentRes;
        this.nameRes = nameRes;
        this.dark = dark;
    }

    public static ReaderTheme fromName(@Nullable String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return CREAM;
        }
    }
}
