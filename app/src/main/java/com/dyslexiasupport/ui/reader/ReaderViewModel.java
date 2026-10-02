package com.dyslexiasupport.ui.reader;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Shared by ReaderFragment and ReaderSettingsSheet (both ask the Activity for it),
 * so a slider moved in the sheet updates the page behind it instantly.
 */
public class ReaderViewModel extends AndroidViewModel {

    public static final int SAMPLE_BN = 0;
    public static final int SAMPLE_EN = 1;

    private final ReaderSettingsStore store;
    private final MutableLiveData<ReaderSettings> settings = new MutableLiveData<>();
    private final MutableLiveData<Integer> sample = new MutableLiveData<>();

    public ReaderViewModel(@NonNull Application application) {
        super(application);
        store = new ReaderSettingsStore(application);
        settings.setValue(store.load());
    }

    public LiveData<ReaderSettings> getSettings() {
        return settings;
    }

    public LiveData<Integer> getSample() {
        return sample;
    }

    public void update(@NonNull ReaderSettings newSettings) {
        ReaderSettings clean = newSettings.sanitized();
        settings.setValue(clean);
        store.save(clean);
    }

    public void resetSettings() {
        update(ReaderSettings.defaults());
    }

    public void setSample(int index) {
        sample.setValue(index);
    }

    /** First open only: pick the sample that matches the app language. */
    public void initSample(boolean bangla) {
        if (sample.getValue() == null) {
            sample.setValue(bangla ? SAMPLE_BN : SAMPLE_EN);
        }
    }
}
