package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a completed reading session.
 */
@Entity(tableName = "reading_sessions")
public class ReadingSessionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String textId;

    public String textTitle;
    public long durationSeconds;
    public long timestamp;
    @NonNull
    public String ownerId = "shared";

    public ReadingSessionEntity(@NonNull String textId, String textTitle, long durationSeconds, long timestamp) {
        this.textId = textId;
        this.textTitle = textTitle;
        this.durationSeconds = durationSeconds;
        this.timestamp = timestamp;
    }
}
