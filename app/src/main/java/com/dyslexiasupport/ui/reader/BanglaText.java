package com.dyslexiasupport.ui.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Bangla helpers with no Android dependencies (easy to test).
 *
 * An "akshara" is Bangla's natural reading chunk: a consonant (plus any joined consonants
 * written with hasanta, e.g. ট্ট), then an optional vowel sign (kar), then optional
 * chandrabindu / anusvara / visarga. Each akshara is one shaping cluster, so colouring
 * akshara by akshara never breaks how a letter is drawn.
 */
public final class BanglaText {

    private static final char HASANTA = '\u09CD';
    private static final char NUKTA = '\u09BC';
    private static final char ZWNJ = '\u200C';
    private static final char ZWJ = '\u200D';

    private BanglaText() {
    }

    public static boolean isConsonant(char c) {
        return (c >= '\u0995' && c <= '\u09B9')
                || c == '\u09CE' || c == '\u09DC' || c == '\u09DD' || c == '\u09DF';
    }

    public static boolean isIndependentVowel(char c) {
        return c >= '\u0985' && c <= '\u0994';
    }

    public static boolean isVowelSign(char c) {
        return (c >= '\u09BE' && c <= '\u09C4') || c == '\u09C7' || c == '\u09C8'
                || c == '\u09CB' || c == '\u09CC' || c == '\u09D7';
    }

    public static boolean isModifier(char c) {
        return c == '\u0981' || c == '\u0982' || c == '\u0983';
    }

    private static boolean isJoiner(char c) {
        return c == ZWNJ || c == ZWJ;
    }

    private static boolean isBanglaDigit(char c) {
        return c >= '\u09E6' && c <= '\u09EF';
    }

    /** Character that can be inside a Bangla word run (letters, signs, joiners; no digits/symbols). */
    public static boolean isRunChar(char c) {
        if (isJoiner(c)) {
            return true;
        }
        return c >= '\u0980' && c <= '\u09FF' && !isBanglaDigit(c)
                && !(c >= '\u09F2' && c <= '\u09FB');
    }

    /** Character that belongs to a tappable word (any script). */
    public static boolean isWordChar(char c) {
        if (isJoiner(c) || Character.isLetterOrDigit(c)) {
            return true;
        }
        int type = Character.getType(c);
        return type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK;
    }

    public static boolean containsBangla(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u0980' && c <= '\u09FF' && !isBanglaDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /** Start/end of the word around {@code index}, or null if the tap landed on a gap. */
    public static int[] wordBoundsAt(CharSequence text, int index) {
        int len = text.length();
        if (len == 0) {
            return null;
        }
        int i = Math.max(0, Math.min(index, len - 1));
        if (!isWordChar(text.charAt(i))) {
            i--;
            if (i < 0 || !isWordChar(text.charAt(i))) {
                return null;
            }
        }
        int s = i;
        while (s > 0 && isWordChar(text.charAt(s - 1))) {
            s--;
        }
        int e = i + 1;
        while (e < len && isWordChar(text.charAt(e))) {
            e++;
        }
        return new int[]{s, e};
    }

    /** Splits one Bangla word run [start, end) into akshara ranges. */
    public static List<int[]> aksharas(CharSequence t, int start, int end) {
        List<int[]> out = new ArrayList<>();
        int i = start;
        while (i < end) {
            int s = i;
            char c = t.charAt(i);
            if (isConsonant(c)) {
                i++;
                i = skipNuktaAndJoiners(t, i, end);
                while (i < end && t.charAt(i) == HASANTA) {
                    int j = skipJoiners(t, i + 1, end);
                    if (j < end && isConsonant(t.charAt(j))) {
                        i = skipNuktaAndJoiners(t, j + 1, end);
                    } else {
                        i = j; // trailing hasanta, e.g. word-final ক্
                        break;
                    }
                }
            } else {
                i++; // independent vowel or a stray sign: its own start
            }
            while (i < end && isVowelSign(t.charAt(i))) {
                i++;
            }
            while (i < end && isModifier(t.charAt(i))) {
                i++;
            }
            out.add(new int[]{s, i});
        }
        return out;
    }

    /** Akshara strings of a word (non-Bangla stretches are returned unsplit). */
    public static List<String> aksharaStrings(String word) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < word.length()) {
            int s = i;
            boolean run = isRunChar(word.charAt(i));
            while (i < word.length() && isRunChar(word.charAt(i)) == run) {
                i++;
            }
            if (run) {
                for (int[] r : aksharas(word, s, i)) {
                    out.add(word.substring(r[0], r[1]));
                }
            } else {
                out.add(word.substring(s, i));
            }
        }
        return out;
    }

    /** True for aksharas with joined consonants, such as ক্ষ or ন্ধ্য. */
    public static boolean isConjunct(String akshara) {
        for (int i = 0; i < akshara.length() - 1; i++) {
            if (akshara.charAt(i) == HASANTA) {
                int j = skipJoiners(akshara, i + 1, akshara.length());
                if (j < akshara.length() && isConsonant(akshara.charAt(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The separate consonants inside a conjunct: ক্ষ -> [ক, ষ]. */
    public static List<String> conjunctParts(String akshara) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < akshara.length(); i++) {
            char c = akshara.charAt(i);
            if (isConsonant(c)) {
                StringBuilder sb = new StringBuilder().append(c);
                if (i + 1 < akshara.length() && akshara.charAt(i + 1) == NUKTA) {
                    sb.append(NUKTA);
                    i++;
                }
                parts.add(sb.toString());
            }
        }
        return parts;
    }

    private static int skipJoiners(CharSequence t, int i, int end) {
        while (i < end && isJoiner(t.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int skipNuktaAndJoiners(CharSequence t, int i, int end) {
        while (i < end && (t.charAt(i) == NUKTA || isJoiner(t.charAt(i)))) {
            i++;
        }
        return i;
    }
}
