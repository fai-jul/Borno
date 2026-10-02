package com.dyslexiasupport.ui.reader;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Put on a space character to make it wider. Android has no "word spacing" setting,
 * so we replace each space with an empty box of (normal space width + extra).
 */
public class WordSpacingSpan extends ReplacementSpan {

    private final float extraPx;

    public WordSpacingSpan(float extraPx) {
        this.extraPx = extraPx;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(" ") + extraPx);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        // A space draws nothing; only its width matters.
    }
}
