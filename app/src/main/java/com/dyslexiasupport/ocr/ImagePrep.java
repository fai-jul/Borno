package com.dyslexiasupport.ocr;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

/** Turns camera / gallery images into a size and look that text recognition handles well. */
public final class ImagePrep {

    /** Longest side we keep for the picture the user crops (memory-friendly). */
    public static final int MAX_SIDE = 2400;
    /** Very small crops are enlarged to at least this, so letters have enough pixels. */
    private static final int MIN_OCR_SIDE = 1600;

    private ImagePrep() {
    }

    public static Bitmap fromImageProxy(@NonNull ImageProxy image) {
        Bitmap bmp = image.toBitmap();
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            bmp = rotate(bmp, rotation);
        }
        return scaleDown(bmp, MAX_SIDE);
    }

    public static Bitmap decodeUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        ContentResolver cr = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(cr, uri);
            // ImageDecoder applies the photo's rotation (EXIF) by itself.
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                int w = info.getSize().getWidth();
                int h = info.getSize().getHeight();
                int longSide = Math.max(w, h);
                if (longSide > MAX_SIDE) {
                    float f = MAX_SIDE / (float) longSide;
                    decoder.setTargetSize(Math.round(w * f), Math.round(h * f));
                }
            });
        }

        // Older phones: decode a smaller copy, then fix the rotation from the EXIF tag.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap bmp;
        try (InputStream in = cr.openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) {
            throw new IOException("Could not decode image");
        }
        int degrees = 0;
        try (InputStream in = cr.openInputStream(uri)) {
            if (in != null) {
                int o = new ExifInterface(in).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (o == ExifInterface.ORIENTATION_ROTATE_90) degrees = 90;
                else if (o == ExifInterface.ORIENTATION_ROTATE_180) degrees = 180;
                else if (o == ExifInterface.ORIENTATION_ROTATE_270) degrees = 270;
            }
        }
        if (degrees != 0) {
            bmp = rotate(bmp, degrees);
        }
        return scaleDown(bmp, MAX_SIDE);
    }

    /** Grey + stronger contrast; scaled so letters are neither tiny nor huge. */
    public static Bitmap forOcr(@NonNull Bitmap cropped) {
        Bitmap sized = cropped;
        int longSide = Math.max(cropped.getWidth(), cropped.getHeight());
        if (longSide < MIN_OCR_SIDE) {
            float f = MIN_OCR_SIDE / (float) longSide;
            sized = Bitmap.createScaledBitmap(cropped,
                    Math.round(cropped.getWidth() * f), Math.round(cropped.getHeight() * f), true);
        } else if (longSide > MAX_SIDE) {
            sized = scaleDown(cropped, MAX_SIDE);
        }

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        // A moderate contrast lift improves paper/background separation without
        // erasing thin strokes and Bengali vowel marks.
        float contrast = 1.28f;
        float shift = (-0.5f * contrast + 0.5f) * 255f;
        matrix.postConcat(new ColorMatrix(new float[]{
                contrast, 0, 0, 0, shift,
                0, contrast, 0, 0, shift,
                0, 0, contrast, 0, shift,
                0, 0, 0, 1, 0}));

        Bitmap out = Bitmap.createBitmap(sized.getWidth(), sized.getHeight(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        new Canvas(out).drawBitmap(sized, 0f, 0f, paint);
        return out;
    }

    /** A high-contrast variant for camera pages with shadows or grey paper. */
    public static Bitmap forOcrThreshold(@NonNull Bitmap cropped) {
        Bitmap enhanced = forOcr(cropped);
        Bitmap out = Bitmap.createBitmap(enhanced.getWidth(), enhanced.getHeight(),
                Bitmap.Config.ARGB_8888);
        int[] pixels = new int[enhanced.getWidth() * enhanced.getHeight()];
        enhanced.getPixels(pixels, 0, enhanced.getWidth(), 0, 0,
                enhanced.getWidth(), enhanced.getHeight());
        for (int i = 0; i < pixels.length; i++) {
            int gray = (int) (0.299f * android.graphics.Color.red(pixels[i])
                    + 0.587f * android.graphics.Color.green(pixels[i])
                    + 0.114f * android.graphics.Color.blue(pixels[i]));
            int value = gray < 185 ? 0 : 255;
            pixels[i] = android.graphics.Color.rgb(value, value, value);
        }
        out.setPixels(pixels, 0, enhanced.getWidth(), 0, 0,
                enhanced.getWidth(), enhanced.getHeight());
        return out;
    }

    public static Bitmap scaleDown(@NonNull Bitmap src, int maxSide) {
        int longSide = Math.max(src.getWidth(), src.getHeight());
        if (longSide <= maxSide) {
            return src;
        }
        float f = maxSide / (float) longSide;
        return Bitmap.createScaledBitmap(src,
                Math.round(src.getWidth() * f), Math.round(src.getHeight() * f), true);
    }

    public static Bitmap rotate(@NonNull Bitmap src, int degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }
}
