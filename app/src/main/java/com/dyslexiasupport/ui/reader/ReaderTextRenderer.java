package com.dyslexiasupport.ui.reader;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * The one place that turns ReaderSettings into pixels on a TextView.
 * Phase 5 (read-aloud highlight) extends this class.
 */
public final class ReaderTextRenderer {

    private ReaderTextRenderer() {
    }

    public static void apply(@NonNull TextView view, @NonNull CharSequence raw,
                             @NonNull ReaderSettings s) {
        Context c = view.getContext();
        float density = c.getResources().getDisplayMetrics().density;

        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, s.fontSizeSp);
        view.setLineSpacing(0f, s.lineSpacing);
        view.setLetterSpacing(s.letterSpacingEm);
        view.setTextColor(ContextCompat.getColor(c, s.theme.textRes));

        SpannableStringBuilder out = new SpannableStringBuilder(raw);
        addWordSpacing(out, s.wordSpacingDp * density);
        if (s.syllableColors) {
            addSyllableColors(out, ContextCompat.getColor(c, s.theme.accentRes));
        }
        view.setText(out, TextView.BufferType.SPANNABLE);
    }

    private static void addWordSpacing(SpannableStringBuilder out, float extraPx) {
        if (extraPx <= 0f) {
            return;
        }
        for (int i = 0; i < out.length(); i++) {
            if (out.charAt(i) == ' ') {
                out.setSpan(new WordSpacingSpan(extraPx), i, i + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /** In every Bangla word, colour each second akshara with the theme accent. */
    private static void addSyllableColors(SpannableStringBuilder out, int accent) {
        int n = out.length();
        int i = 0;
        while (i < n) {
            if (!BanglaText.isRunChar(out.charAt(i))) {
                i++;
                continue;
            }
            int start = i;
            while (i < n && BanglaText.isRunChar(out.charAt(i))) {
                i++;
            }
            List<int[]> parts = BanglaText.aksharas(out, start, i);
            for (int k = 1; k < parts.size(); k += 2) {
                int[] r = parts.get(k);
                out.setSpan(new ForegroundColorSpan(accent), r[0], r[1],
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
