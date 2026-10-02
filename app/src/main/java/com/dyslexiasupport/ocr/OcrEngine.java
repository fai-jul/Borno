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
            api.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            api.setImage(bitmap);
            String raw = api.getUTF8Text();
            return cleanUp(raw == null ? "" : raw);
        } finally {
            api.recycle();
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
        String t = raw.replace("\r", "");
        t = t.replaceAll("(?<!\\n)\\n(?!\\n)", " ");
        t = t.replaceAll("[ \\t]+", " ");
        t = t.replaceAll(" ?\\n ?", "\n");
        t = t.replaceAll("\\n{3,}", "\n\n");
        return t.trim();
    }
}
