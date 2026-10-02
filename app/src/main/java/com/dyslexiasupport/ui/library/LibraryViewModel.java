package com.dyslexiasupport.ui.library;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.dyslexiasupport.data.library.LibraryPrefs;
import com.dyslexiasupport.data.library.LibraryRepository;
import com.dyslexiasupport.data.library.ReadingText;
import com.dyslexiasupport.data.local.entity.DocumentEntity;
import com.dyslexiasupport.data.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared by the Library list and the Reader (both ask the Activity for it).
 * Combines the bundled texts (assets) with the user's scanned documents (database),
 * then applies the filter chips. Scanned documents have id "doc_<rowId>" and level 0.
 */
public class LibraryViewModel extends AndroidViewModel {

    public static final int LANG_ALL = 0;
    public static final int LANG_BN = 1;
    public static final int LANG_EN = 2;

    private final LibraryPrefs prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<ReadingText>> all = new MutableLiveData<>();
    private final MutableLiveData<List<LibraryItem>> visible =
            new MutableLiveData<>(new ArrayList<>());

    private final LiveData<List<DocumentEntity>> documentsLive;
    private final Observer<List<DocumentEntity>> documentsObserver;

    @Nullable private List<ReadingText> bundledTexts;          // null until assets are loaded
    private List<DocumentEntity> documents = new ArrayList<>();

    private int langFilter = LANG_ALL;
    private int levelFilter = 0; // 0 = any level
    private boolean onlySaved = false;

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        prefs = new LibraryPrefs(application);

        // Scanned documents: Room hands us a fresh list whenever the table changes.
        documentsLive = new AppRepository(application).getAllDocuments();
        documentsObserver = docs -> {
            documents = docs != null ? docs : new ArrayList<>();
            combine();
        };
        documentsLive.observeForever(documentsObserver);

        // Bundled texts: read on a background thread.
        LibraryRepository repo = new LibraryRepository(application);
        io.execute(() -> {
            List<ReadingText> loaded = repo.loadAll();
            mainHandler.post(() -> {
                bundledTexts = loaded;
                combine();
            });
        });
    }

    /** Null until the bundled texts have finished loading. */
    public LiveData<List<ReadingText>> getAll() {
        return all;
    }

    public LiveData<List<LibraryItem>> getVisible() {
        return visible;
    }

    @Nullable
    public ReadingText findById(@Nullable String id) {
        List<ReadingText> list = all.getValue();
        if (list == null || id == null) {
            return null;
        }
        for (ReadingText t : list) {
            if (t.id.equals(id)) {
                return t;
            }
        }
        return null;
    }

    public int getLangFilter() {
        return langFilter;
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public boolean isOnlySaved() {
        return onlySaved;
    }

    public void setLangFilter(int lang) {
        langFilter = lang;
        refresh();
    }

    public void setLevelFilter(int level) {
        levelFilter = level;
        refresh();
    }

    public void setOnlySaved(boolean only) {
        onlySaved = only;
        refresh();
    }

    /** @return true if the text is now saved */
    public boolean toggleFavourite(@NonNull String id) {
        boolean now = prefs.toggleFavourite(id);
        refresh();
        return now;
    }

    /** @return true if the text is now marked as read */
    public boolean toggleRead(@NonNull String id) {
        boolean now = prefs.toggleRead(id);
        refresh();
        return now;
    }

    /** Scanned documents first (newest first), then the bundled texts. */
    private void combine() {
        if (bundledTexts == null) {
            return;
        }
        List<ReadingText> merged = new ArrayList<>();
        for (DocumentEntity d : documents) {
            merged.add(new ReadingText("doc_" + d.id, d.title, d.body, "\uD83D\uDCF7", 0, d.lang));
        }
        merged.addAll(bundledTexts);
        all.setValue(merged);
        refresh();
    }

    private void refresh() {
        List<ReadingText> source = all.getValue();
        if (source == null) {
            return;
        }
        Set<String> favourites = prefs.getFavourites();
        Set<String> read = prefs.getRead();
        List<LibraryItem> out = new ArrayList<>();
        for (ReadingText t : source) {
            if (langFilter == LANG_BN && !t.isBangla()) continue;
            if (langFilter == LANG_EN && t.isBangla()) continue;
            if (levelFilter != 0 && t.level != levelFilter) continue;
            boolean fav = favourites.contains(t.id);
            if (onlySaved && !fav) continue;
            out.add(new LibraryItem(t, fav, read.contains(t.id)));
        }
        visible.setValue(out); // always a NEW list, so the adapter can diff it
    }

    @Override
    protected void onCleared() {
        documentsLive.removeObserver(documentsObserver);
        io.shutdown();
    }
}
