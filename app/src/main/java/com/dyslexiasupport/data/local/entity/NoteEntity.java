package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing user notes.
 */
@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @NonNull
    public String content;

    public long createdAt;
    public long updatedAt;
    @NonNull
    public String ownerId = "shared";

    public NoteEntity(@NonNull String title, @NonNull String content, long createdAt, long updatedAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
