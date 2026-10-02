package com.dyslexiasupport.ui.scan;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractManager {

    public static String getTessDataPath(Context context) {
        File dir = new File(context.getFilesDir(), "tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        copyTessDataFiles(context, dir);
        return context.getFilesDir().getAbsolutePath();
    }

    private static void copyTessDataFiles(Context context, File tessDataDir) {
        String[] files = new String[]{"ben.traineddata", "eng.traineddata"};
        for (String file : files) {
            File dest = new File(tessDataDir, file);
            if (!dest.exists() || dest.length() == 0) {
                try (InputStream in = context.getAssets().open("tessdata/" + file);
                     OutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                } catch (Exception ignored) {
                    try {
                        if (!dest.exists()) {
                            dest.createNewFile();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String performOcr(Context context, Bitmap bitmap) {
        if (bitmap == null) return "";
        String dataPath = getTessDataPath(context);
        TessBaseAPI tess = new TessBaseAPI();
        try {
            tess.init(dataPath, "ben+eng");
        } catch (Exception e) {
            try {
                tess.init(dataPath, "eng");
            } catch (Exception ex) {
                return "";
            }
        }
        tess.setImage(bitmap);
        String text = tess.getUTF8Text();
        tess.recycle();
        return text != null ? text.trim() : "";
    }
}
