package com.dyslexiasupport.data.library;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Persists user's library preferences such as saved (favourite) and read texts.
 */
public class LibraryPrefs {

    private static final String PREF_NAME = "library_prefs";
    private static final String KEY_FAVOURITES = "favourites";
    private static final String KEY_READ = "read_texts";

    private final SharedPreferences prefs;

    public LibraryPrefs(@NonNull Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public Set<String> getFavourites() {
        Set<String> set = prefs.getStringSet(KEY_FAVOURITES, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    @NonNull
    public Set<String> getRead() {
        Set<String> set = prefs.getStringSet(KEY_READ, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    public boolean toggleFavourite(@NonNull String id) {
        Set<String> set = getFavourites();
        boolean nowFav;
        if (set.contains(id)) {
            set.remove(id);
            nowFav = false;
        } else {
            set.add(id);
            nowFav = true;
        }
        prefs.edit().putStringSet(KEY_FAVOURITES, set).apply();
        return nowFav;
    }

    public boolean toggleRead(@NonNull String id) {
        Set<String> set = getRead();
        boolean nowRead;
        if (set.contains(id)) {
            set.remove(id);
            nowRead = false;
        } else {
            set.add(id);
            nowRead = true;
        }
        prefs.edit().putStringSet(KEY_READ, set).apply();
        return nowRead;
    }

    public boolean markAsRead(@NonNull String id) {
        Set<String> set = getRead();
        if (!set.contains(id)) {
            set.add(id);
            prefs.edit().putStringSet(KEY_READ, set).apply();
            return true;
        }
        return false;
    }
}
