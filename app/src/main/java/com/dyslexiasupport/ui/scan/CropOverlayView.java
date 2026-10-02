package com.dyslexiasupport.ui.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Interactive crop overlay view showing crop window over image preview.
 */
public class CropOverlayView extends View {

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF cropRect = new RectF();
    private float touchX, touchY;
    private int activeHandle = HANDLE_NONE;

    private static final int HANDLE_NONE = 0;
    private static final int HANDLE_MOVE = 1;
    private static final int HANDLE_TOP_LEFT = 2;
    private static final int HANDLE_TOP_RIGHT = 3;
    private static final int HANDLE_BOTTOM_LEFT = 4;
    private static final int HANDLE_BOTTOM_RIGHT = 5;

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        backgroundPaint.setColor(Color.argb(128, 0, 0, 0));
        backgroundPaint.setStyle(Paint.Style.FILL);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float marginX = w * 0.1f;
        float marginY = h * 0.1f;
        cropRect.set(marginX, marginY, w - marginX, h - marginY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), cropRect.top, backgroundPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, backgroundPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, backgroundPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), backgroundPaint);

        canvas.drawRect(cropRect, borderPaint);

        float radius = 16f;
        canvas.drawCircle(cropRect.left, cropRect.top, radius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, radius, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, radius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, radius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = getHitHandle(x, y);
                touchX = x;
                touchY = y;
                return activeHandle != HANDLE_NONE;

            case MotionEvent.ACTION_MOVE:
                if (activeHandle != HANDLE_NONE) {
                    float dx = x - touchX;
                    float dy = y - touchY;

                    if (activeHandle == HANDLE_MOVE) {
                        cropRect.offset(dx, dy);
                    } else if (activeHandle == HANDLE_TOP_LEFT) {
                        cropRect.left += dx;
                        cropRect.top += dy;
                    } else if (activeHandle == HANDLE_TOP_RIGHT) {
                        cropRect.right += dx;
                        cropRect.top += dy;
                    } else if (activeHandle == HANDLE_BOTTOM_LEFT) {
                        cropRect.left += dx;
                        cropRect.bottom += dy;
                    } else if (activeHandle == HANDLE_BOTTOM_RIGHT) {
                        cropRect.right += dx;
                        cropRect.bottom += dy;
                    }

                    float minSize = 100f;
                    if (cropRect.width() < minSize) cropRect.right = cropRect.left + minSize;
                    if (cropRect.height() < minSize) cropRect.bottom = cropRect.top + minSize;

                    touchX = x;
                    touchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeHandle = HANDLE_NONE;
                break;
        }
        return super.onTouchEvent(event);
    }

    private int getHitHandle(float x, float y) {
        float threshold = 60f;
        if (dist(x, y, cropRect.left, cropRect.top) < threshold) return HANDLE_TOP_LEFT;
        if (dist(x, y, cropRect.right, cropRect.top) < threshold) return HANDLE_TOP_RIGHT;
        if (dist(x, y, cropRect.left, cropRect.bottom) < threshold) return HANDLE_BOTTOM_LEFT;
        if (dist(x, y, cropRect.right, cropRect.bottom) < threshold) return HANDLE_BOTTOM_RIGHT;
        if (cropRect.contains(x, y)) return HANDLE_MOVE;
        return HANDLE_NONE;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    public Bitmap cropBitmap(Bitmap src) {
        if (src == null) return null;
        float scaleX = (float) src.getWidth() / getWidth();
        float scaleY = (float) src.getHeight() / getHeight();

        int left = Math.max(0, (int) (cropRect.left * scaleX));
        int top = Math.max(0, (int) (cropRect.top * scaleY));
        int width = Math.min(src.getWidth() - left, (int) (cropRect.width() * scaleX));
        int height = Math.min(src.getHeight() - top, (int) (cropRect.height() * scaleY));

        if (width <= 0 || height <= 0) return src;
        return Bitmap.createBitmap(src, left, top, width, height);
    }
}
