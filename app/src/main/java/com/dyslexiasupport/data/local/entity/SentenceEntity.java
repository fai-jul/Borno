package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a practice sentence.
 */
@Entity(tableName = "sentences")
public class SentenceEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String sentence;

    public String translation;
    public String language; // "bn" or "en"
    public long createdAt;

    public SentenceEntity(@NonNull String sentence, String translation, String language, long createdAt) {
        this.sentence = sentence;
        this.translation = translation;
        this.language = language;
        this.createdAt = createdAt;
    }
}
