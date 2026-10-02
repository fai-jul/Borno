package com.dyslexiasupport.ui.library;

import androidx.annotation.NonNull;

import com.dyslexiasupport.data.library.ReadingText;

import java.util.Objects;

/** A text plus the user's flags for it. Compared by value so DiffUtil can spot changes. */
public final class LibraryItem {

    public final ReadingText text;
    public final boolean favourite;
    public final boolean read;

    public LibraryItem(@NonNull ReadingText text, boolean favourite, boolean read) {
        this.text = text;
        this.favourite = favourite;
        this.read = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibraryItem)) return false;
        LibraryItem other = (LibraryItem) o;
        return favourite == other.favourite && read == other.read
                && text.id.equals(other.text.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text.id, favourite, read);
    }
}
