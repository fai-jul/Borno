package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A text the user scanned (or typed) themselves. Shown in the library with a camera icon. */
@Entity(tableName = "documents")
public class DocumentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @NonNull
    public String body;

    /** "bn" or "en" */
    @NonNull
    public String lang;

    public long createdAt;
    @NonNull
    public String ownerId = "shared";

    public DocumentEntity(@NonNull String title, @NonNull String body,
                          @NonNull String lang, long createdAt) {
        this.title = title;
        this.body = body;
        this.lang = lang;
        this.createdAt = createdAt;
    }
}
