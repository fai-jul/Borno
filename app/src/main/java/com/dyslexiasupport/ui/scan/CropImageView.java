package com.dyslexiasupport.ui.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Shows a photo with a draggable crop rectangle. Drag a corner to resize, drag inside to move.
 * The rectangle is stored as fractions (0..1) of the photo, so it survives size changes.
 */
public class CropImageView extends View {

    private static final int NONE = 0, MOVE = 1, TL = 2, TR = 3, BL = 4, BR = 5;
    private static final float MIN_FRACTION = 0.12f;

    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint scrimPaint = new Paint();
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF imageRect = new RectF();
    private final RectF crop = new RectF(0.06f, 0.06f, 0.94f, 0.94f);
    private final RectF box = new RectF();
    private final float handleRadius;
    private final float touchRadius;

    @Nullable private Bitmap bitmap;
    private int dragMode = NONE;
    private float lastX;
    private float lastY;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float d = context.getResources().getDisplayMetrics().density;
        handleRadius = 11f * d;
        touchRadius = 36f * d;
        scrimPaint.setColor(0xAA000000);
        borderPaint.setColor(0xFFFFFFFF);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f * d);
        handlePaint.setColor(0xFFF4A62A); // mango
    }

    public void setBitmap(@Nullable Bitmap b) {
        bitmap = b;
        crop.set(0.06f, 0.06f, 0.94f, 0.94f);
        updateImageRect();
        invalidate();
    }

    @Nullable
    public Bitmap getCroppedBitmap() {
        if (bitmap == null) {
            return null;
        }
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        int x = Math.max(0, Math.round(crop.left * bw));
        int y = Math.max(0, Math.round(crop.top * bh));
        int w = Math.max(1, Math.min(bw - x, Math.round(crop.width() * bw)));
        int h = Math.max(1, Math.min(bh - y, Math.round(crop.height() * bh)));
        return Bitmap.createBitmap(bitmap, x, y, w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateImageRect();
    }

    private void updateImageRect() {
        if (bitmap == null || getWidth() == 0) {
            return;
        }
        float vw = getWidth() - getPaddingLeft() - getPaddingRight();
        float vh = getHeight() - getPaddingTop() - getPaddingBottom();
        float scale = Math.min(vw / bitmap.getWidth(), vh / bitmap.getHeight());
        float w = bitmap.getWidth() * scale;
        float h = bitmap.getHeight() * scale;
        float left = getPaddingLeft() + (vw - w) / 2f;
        float top = getPaddingTop() + (vh - h) / 2f;
        imageRect.set(left, top, left + w, top + h);
    }

    /** Crop rectangle in view pixels. */
    private void toView(RectF out) {
        out.set(imageRect.left + crop.left * imageRect.width(),
                imageRect.top + crop.top * imageRect.height(),
                imageRect.left + crop.right * imageRect.width(),
                imageRect.top + crop.bottom * imageRect.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, null, imageRect, bitmapPaint);
        toView(box);

        // Dim everything outside the crop box.
        canvas.drawRect(imageRect.left, imageRect.top, imageRect.right, box.top, scrimPaint);
        canvas.drawRect(imageRect.left, box.bottom, imageRect.right, imageRect.bottom, scrimPaint);
        canvas.drawRect(imageRect.left, box.top, box.left, box.bottom, scrimPaint);
        canvas.drawRect(box.right, box.top, imageRect.right, box.bottom, scrimPaint);

        canvas.drawRect(box, borderPaint);
        canvas.drawCircle(box.left, box.top, handleRadius, handlePaint);
        canvas.drawCircle(box.right, box.top, handleRadius, handlePaint);
        canvas.drawCircle(box.left, box.bottom, handleRadius, handlePaint);
        canvas.drawCircle(box.right, box.bottom, handleRadius, handlePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (bitmap == null) {
            return false;
        }
        float x = e.getX();
        float y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                toView(box);
                if (near(x, y, box.left, box.top)) dragMode = TL;
                else if (near(x, y, box.right, box.top)) dragMode = TR;
                else if (near(x, y, box.left, box.bottom)) dragMode = BL;
                else if (near(x, y, box.right, box.bottom)) dragMode = BR;
                else if (box.contains(x, y)) dragMode = MOVE;
                else dragMode = NONE;
                lastX = x;
                lastY = y;
                if (dragMode != NONE && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return dragMode != NONE;
            case MotionEvent.ACTION_MOVE:
                if (dragMode == NONE) {
                    return false;
                }
                drag((x - lastX) / imageRect.width(), (y - lastY) / imageRect.height());
                lastX = x;
                lastY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragMode = NONE;
                return true;
            default:
                return dragMode != NONE;
        }
    }

    private boolean near(float x, float y, float cx, float cy) {
        float dx = x - cx;
        float dy = y - cy;
        return dx * dx + dy * dy <= touchRadius * touchRadius;
    }

    private void drag(float dx, float dy) {
        switch (dragMode) {
            case TL:
                crop.left = clamp(crop.left + dx, 0f, crop.right - MIN_FRACTION);
                crop.top = clamp(crop.top + dy, 0f, crop.bottom - MIN_FRACTION);
                break;
            case TR:
                crop.right = clamp(crop.right + dx, crop.left + MIN_FRACTION, 1f);
                crop.top = clamp(crop.top + dy, 0f, crop.bottom - MIN_FRACTION);
                break;
            case BL:
                crop.left = clamp(crop.left + dx, 0f, crop.right - MIN_FRACTION);
                crop.bottom = clamp(crop.bottom + dy, crop.top + MIN_FRACTION, 1f);
                break;
            case BR:
                crop.right = clamp(crop.right + dx, crop.left + MIN_FRACTION, 1f);
                crop.bottom = clamp(crop.bottom + dy, crop.top + MIN_FRACTION, 1f);
                break;
            case MOVE:
                float w = crop.width();
                float h = crop.height();
                float left = clamp(crop.left + dx, 0f, 1f - w);
                float top = clamp(crop.top + dy, 0f, 1f - h);
                crop.set(left, top, left + w, top + h);
                break;
            default:
                break;
        }
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
