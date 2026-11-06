package com.dyslexiasupport.ui.scan;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.entity.DocumentEntity;
import com.dyslexiasupport.data.repository.AppRepository;
import com.dyslexiasupport.ocr.ImagePrep;
import com.dyslexiasupport.ocr.OcrEngine;
import com.dyslexiasupport.ui.reader.BanglaText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Holds the scan flow state, so rotating the phone does not lose the photo or the text. */
public class ScanViewModel extends AndroidViewModel {

    private static final String TAG = "ScanViewModel";

    public static final int STEP_CAMERA = 0;
    public static final int STEP_CROP = 1;
    public static final int STEP_EDIT = 2;

    private final MutableLiveData<Integer> step = new MutableLiveData<>(STEP_CAMERA);
    private final MutableLiveData<Bitmap> source = new MutableLiveData<>();
    private final MutableLiveData<Boolean> working = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> error = new MutableLiveData<>();

    /** Latest text of the edit screen (kept in sync by a TextWatcher). */
    public String editedTitle = "";
    public String editedBody = "";
    public boolean askedCameraPermission = false;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final OcrEngine ocr;
    private final AppRepository repository;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        ocr = new OcrEngine(application);
        repository = new AppRepository(application);
    }

    public LiveData<Integer> getStep() { return step; }
    public LiveData<Bitmap> getSource() { return source; }
    public LiveData<Boolean> getWorking() { return working; }
    public LiveData<Integer> getError() { return error; }

    public void setStep(int s) { step.setValue(s); }
    public void clearError() { error.setValue(null); }

    public boolean hasText() {
        return !editedBody.trim().isEmpty();
    }

    public void loadFromCamera(@NonNull ImageProxy image) {
        working.setValue(true);
        io.execute(() -> {
            try {
                Bitmap bmp = ImagePrep.fromImageProxy(image);
                main.post(() -> photoReady(bmp));
            } catch (Exception e) {
                Log.e(TAG, "camera image failed", e);
                main.post(() -> fail(R.string.scan_error_image));
            } finally {
                image.close(); // always give the frame back to the camera
            }
        });
    }

    public void loadFromUri(@NonNull Uri uri) {
        working.setValue(true);
        io.execute(() -> {
            try {
                Bitmap bmp = ImagePrep.decodeUri(getApplication(), uri);
                main.post(() -> photoReady(bmp));
            } catch (Exception e) {
                Log.e(TAG, "gallery image failed", e);
                main.post(() -> fail(R.string.scan_error_image));
            }
        });
    }

    private void photoReady(Bitmap bmp) {
        working.setValue(false);
        source.setValue(bmp);
        step.setValue(STEP_CROP);
    }

    /** Reads the cropped area. New text is added after any text we already have (multi-page). */
    public void runOcr(@Nullable Bitmap cropped) {
        if (cropped == null) {
            fail(R.string.scan_error_image);
            return;
        }
        working.setValue(true);
        io.execute(() -> {
            try {
                // Keep the camera crop unmodified here. OcrEngine evaluates the
                // original and enhanced variants, which protects Bengali marks
                // while still improving low-contrast English text.
                String text = ocr.recognize(cropped);
                main.post(() -> ocrDone(text));
            } catch (Exception e) {
                Log.e(TAG, "ocr failed", e);
                main.post(() -> fail(R.string.scan_error_ocr));
            }
        });
    }

    private void ocrDone(String text) {
        working.setValue(false);
        if (text.trim().isEmpty()) {
            error.setValue(R.string.scan_error_no_text);
            return;
        }
        String existing = editedBody.trim();
        editedBody = existing.isEmpty() ? text : existing + "\n\n" + text;
        if (editedTitle.trim().isEmpty()) {
            String date = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(new Date());
            editedTitle = getApplication().getString(R.string.scan_default_title, date);
        }
        step.setValue(STEP_EDIT);
    }

    private void fail(@StringRes int message) {
        working.setValue(false);
        error.setValue(message);
    }

    /** @return false if there is nothing to save */
    public boolean save() {
        String body = editedBody.trim();
        if (body.isEmpty()) {
            return false;
        }
        String title = editedTitle.trim();
        if (title.isEmpty()) {
            title = getApplication().getString(R.string.scan_default_title,
                    new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(new Date()));
        }
        String lang = BanglaText.containsBangla(body) ? "bn" : "en";
        repository.insertDocument(new DocumentEntity(title, body, lang, System.currentTimeMillis()));
        return true;
    }

    @Override
    protected void onCleared() {
        io.shutdown();
    }
}
