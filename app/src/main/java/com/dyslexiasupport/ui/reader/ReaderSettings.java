package com.dyslexiasupport.ui.reader;

/** All reader appearance choices. Plain data; the ViewModel hands out fresh copies. */
public class ReaderSettings {

    public static final int MIN_FONT_SP = 16;
    public static final int MAX_FONT_SP = 40;
    public static final float MIN_LINE = 1.0f;
    public static final float MAX_LINE = 2.4f;
    public static final int MAX_WORD_SPACING_DP = 24;
    /** Kept small on purpose: wide letter spacing pulls Bangla conjuncts apart. */
    public static final float MAX_LETTER_EM = 0.06f;
    public static final int MIN_GUIDE_LINES = 1;
    public static final int MAX_GUIDE_LINES = 3;

    public int fontSizeSp = 22;
    public float lineSpacing = 1.6f;
    public int wordSpacingDp = 4;
    public float letterSpacingEm = 0f;
    public ReaderTheme theme = ReaderTheme.CREAM;
    public ReadingGuideMode guideMode = ReadingGuideMode.OFF;
    public int guideLines = 2;
    /** Alternate colours on Bangla syllables (aksharas). */
    public boolean syllableColors = false;

    public static ReaderSettings defaults() {
        return new ReaderSettings();
    }

    public ReaderSettings copy() {
        ReaderSettings s = new ReaderSettings();
        s.fontSizeSp = fontSizeSp;
        s.lineSpacing = lineSpacing;
        s.wordSpacingDp = wordSpacingDp;
        s.letterSpacingEm = letterSpacingEm;
        s.theme = theme;
        s.guideMode = guideMode;
        s.guideLines = guideLines;
        s.syllableColors = syllableColors;
        return s;
    }

    /** Returns a copy with every value forced into its allowed range. */
    public ReaderSettings sanitized() {
        ReaderSettings s = copy();
        s.fontSizeSp = Math.max(MIN_FONT_SP, Math.min(MAX_FONT_SP, s.fontSizeSp));
        s.lineSpacing = Math.max(MIN_LINE, Math.min(MAX_LINE, s.lineSpacing));
        int w = Math.max(0, Math.min(MAX_WORD_SPACING_DP, s.wordSpacingDp));
        s.wordSpacingDp = (w / 2) * 2; // slider step is 2
        s.letterSpacingEm = Math.max(0f, Math.min(MAX_LETTER_EM, s.letterSpacingEm));
        if (s.theme == null) {
            s.theme = ReaderTheme.CREAM;
        }
        if (s.guideMode == null) {
            s.guideMode = ReadingGuideMode.OFF;
        }
        s.guideLines = Math.max(MIN_GUIDE_LINES, Math.min(MAX_GUIDE_LINES, s.guideLines));
        return s;
    }
}
