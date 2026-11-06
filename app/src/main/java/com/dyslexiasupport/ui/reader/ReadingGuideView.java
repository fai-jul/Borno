package com.dyslexiasupport.ui.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.dyslexiasupport.R;

/**
 * Transparent overlay on top of the reading text.
 *  - RULER: a tinted band you can drag up and down.
 *  - FOCUS: same band, but the text above and below is covered by a soft scrim.
 * The band stays fixed on screen while the text scrolls under it, like a real ruler.
 * Touches that start outside the band fall through, so normal scrolling still works.
 * A quick tap INSIDE the band is reported to the listener (so words stay tappable).
 */
public class ReadingGuideView extends View {

    public interface OnBandTapListener {
        /** Coordinates are in this view's space (same as the scroll area behind it). */
        void onBandTap(float x, float y);
    }

    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF handleRect = new RectF();
    private final float density;
    private final float touchSlop;

    private ReadingGuideMode mode = ReadingGuideMode.OFF;
    private float bandHeight;
    private float bandTop = -1f;
    private float dragOffset;
    private float downX;
    private float downY;
    private boolean pressing;
    private boolean dragging;
    @Nullable private OnBandTapListener tapListener;

    public ReadingGuideView(Context context) {
        this(context, null);
    }

    public ReadingGuideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadingGuideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = context.getResources().getDisplayMetrics().density;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        int accent = ContextCompat.getColor(context, R.color.ds_streak_orange);
        int primary = ContextCompat.getColor(context, R.color.ds_primary);
        int ink = ContextCompat.getColor(context, R.color.ds_ink);

        bandPaint.setColor(ColorUtils.setAlphaComponent(accent, 0x47));
        edgePaint.setColor(ColorUtils.setAlphaComponent(accent, 0xFF));
        handlePaint.setColor(ColorUtils.setAlphaComponent(primary, 0xFF));
        scrimPaint.setColor(ColorUtils.setAlphaComponent(ink, 0x77));

        setElevation(10f * density);
        setVisibility(GONE);
    }

    public void setOnBandTapListener(@Nullable OnBandTapListener listener) {
        tapListener = listener;
    }

    /**
     * @param bandHeightPx height of the clear window, in pixels
     * @param scrimColor   colour used to dim the text outside the window (FOCUS only)
     */
    public void configure(ReadingGuideMode newMode, float bandHeightPx, @ColorInt int scrimColor) {
        mode = newMode;
        bandHeight = bandHeightPx;
        if (scrimColor != 0) {
            scrimPaint.setColor(scrimColor);
        }
        // GONE lets every touch pass straight through to the text when the guide is off.
        setVisibility(mode == ReadingGuideMode.OFF ? GONE : VISIBLE);
        if (getHeight() > 0) {
            float initialTop = (bandTop < 0) ? getHeight() * 0.3f : bandTop;
            bandTop = clampTop(initialTop);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > 0) {
            float initialTop = (bandTop < 0) ? h * 0.3f : bandTop;
            bandTop = clampTop(initialTop);
            invalidate();
        }
    }

    private float clampTop(float top) {
        int h = getHeight();
        if (h <= 0) {
            return top;
        }
        float max = Math.max(0f, h - bandHeight);
        return Math.max(0f, Math.min(max, top));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mode == ReadingGuideMode.OFF) {
            return;
        }
        float w = getWidth();
        float h = getHeight();
        if (h <= 0) {
            return;
        }
        float top = clampTop(bandTop < 0 ? h * 0.3f : bandTop);
        float bottom = top + bandHeight;
        float edge = 3f * density;

        if (mode == ReadingGuideMode.FOCUS) {
            canvas.drawRect(0f, 0f, w, top, scrimPaint);
            canvas.drawRect(0f, bottom, w, h, scrimPaint);
        }
        canvas.drawRect(0f, top, w, bottom, bandPaint);
        canvas.drawRect(0f, top, w, top + edge, edgePaint);
        canvas.drawRect(0f, bottom - edge, w, bottom, edgePaint);

        // Grip so people discover that the band can be dragged.
        float gripW = 7f * density;
        float gripH = Math.min(32f * density, bandHeight * 0.7f);
        float cx = w - 16f * density;
        float cy = top + bandHeight / 2f;
        handleRect.set(cx - gripW / 2f, cy - gripH / 2f, cx + gripW / 2f, cy + gripH / 2f);
        canvas.drawRoundRect(handleRect, gripW, gripW, handlePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == ReadingGuideMode.OFF) {
            return false;
        }
        float slop = 12f * density; // easier for small fingers
        float top = clampTop(bandTop < 0 ? getHeight() * 0.3f : bandTop);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getY() >= top - slop && event.getY() <= top + bandHeight + slop) {
                    pressing = true;
                    dragging = false;
                    downX = event.getX();
                    downY = event.getY();
                    dragOffset = event.getY() - top;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                return false; // outside the band: let the text scroll
            case MotionEvent.ACTION_MOVE:
                if (!pressing) {
                    return false;
                }
                if (!dragging && Math.abs(event.getY() - downY) > touchSlop) {
                    dragging = true;
                }
                if (dragging) {
                    bandTop = clampTop(event.getY() - dragOffset);
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                boolean wasTap = pressing && !dragging
                        && Math.abs(event.getX() - downX) <= touchSlop;
                pressing = false;
                dragging = false;
                if (wasTap && tapListener != null) {
                    tapListener.onBandTap(downX, downY);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressing = false;
                dragging = false;
                return true;
            default:
                return pressing;
        }
    }
}
