package com.dyslexiasupport.ocr;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Offline Bangla + English text recognition (Tesseract). Google ML Kit has no Bengali model,
 * which is why we use Tesseract. Language files live in assets/tessdata and are copied to
 * internal storage once. Always call recognize() from a background thread.
 */
public class OcrEngine {

    private static final String LANGUAGES = "ben+eng";
    private static final String[] FILES = {"ben.traineddata", "eng.traineddata"};

    private final Context appContext;

    public OcrEngine(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    public synchronized String recognize(@NonNull Bitmap bitmap) throws IOException {
        File base = prepareLanguageData();
        TessBaseAPI api = new TessBaseAPI();
        try {
            if (!api.init(base.getAbsolutePath(), LANGUAGES)) {
                throw new IOException("Tesseract could not start");
            }
            api.setVariable("preserve_interword_spaces", "1");
            // Camera/gallery images do not carry a reliable DPI. Supplying the
            // physical resolution avoids poor line segmentation on small crops.
            api.setVariable("user_defined_dpi", "300");

            Bitmap enhanced = ImagePrep.forOcr(bitmap);
            Bitmap threshold = ImagePrep.forOcrThreshold(bitmap);
            RecognitionResult best = bestForBitmap(api, bitmap);
            RecognitionResult enhancedResult = bestForBitmap(api, enhanced);
            RecognitionResult thresholdResult = bestForBitmap(api, threshold);
            best = chooseBest(best, enhancedResult, thresholdResult);
            return OcrTextCleaner.clean(best.text);
        } finally {
            api.recycle();
        }
    }

    private RecognitionResult bestForBitmap(TessBaseAPI api, Bitmap bitmap) {
        RecognitionResult block = recognizeWithMode(
                api, bitmap, TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
        RecognitionResult auto = recognizeWithMode(
                api, bitmap, TessBaseAPI.PageSegMode.PSM_AUTO);
        RecognitionResult sparse = recognizeWithMode(
                api, bitmap, TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT);
        return chooseBest(block, auto, sparse);
    }

    private RecognitionResult recognizeWithMode(
            TessBaseAPI api, Bitmap bitmap, int pageSegMode) {
        api.setPageSegMode(pageSegMode);
        api.setImage(bitmap);
        String text = api.getUTF8Text();
        int confidence = api.meanConfidence();
        return new RecognitionResult(text == null ? "" : text, confidence);
    }

    private RecognitionResult chooseBest(RecognitionResult... results) {
        RecognitionResult best = results[0];
        int longestText = 0;
        for (RecognitionResult result : results) {
            if (!result.text.trim().isEmpty()) {
                longestText = Math.max(longestText, result.text.trim().length());
            }
        }

        for (RecognitionResult result : results) {
            int length = result.text.trim().length();
            if (length == 0) {
                continue;
            }
            // Prefer a fuller result, but allow confidence to win when two
            // variants contain roughly the same amount of text.
            int bestLength = best.text.trim().length();
            boolean fuller = length > bestLength * 1.12f;
            boolean similarlyFull = length >= bestLength * 0.85f;
            if (fuller || (similarlyFull && result.confidence > best.confidence + 3)) {
                best = result;
            }
        }
        return best;
    }

    private static final class RecognitionResult {
        final String text;
        final int confidence;

        RecognitionResult(String text, int confidence) {
            this.text = text;
            this.confidence = confidence;
        }
    }

    /** Copies the language files out of the APK the first time (or if a copy went missing). */
    private File prepareLanguageData() throws IOException {
        File base = new File(appContext.getFilesDir(), "tesseract");
        File tessdata = new File(base, "tessdata");
        if (!tessdata.exists() && !tessdata.mkdirs()) {
            throw new IOException("Cannot create " + tessdata);
        }
        for (String name : FILES) {
            File target = new File(tessdata, name);
            if (target.exists() && target.length() > 0) {
                continue;
            }
            File temp = new File(tessdata, name + ".tmp");
            try (InputStream in = appContext.getAssets().open("tessdata/" + name);
                 OutputStream out = new FileOutputStream(temp)) {
                byte[] buffer = new byte[16 * 1024];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
            // Rename only after a full copy, so an interrupted copy never looks valid.
            if (!temp.renameTo(target)) {
                throw new IOException("Cannot save " + name);
            }
        }
        return base;
    }

    /**
     * Textbook lines end mid-sentence, which is hard to read aloud and to follow.
     * Join the lines of a paragraph, keep blank lines as paragraph breaks.
     */
    static String cleanUp(String raw) {
        return OcrTextCleaner.clean(raw);
    }
}
