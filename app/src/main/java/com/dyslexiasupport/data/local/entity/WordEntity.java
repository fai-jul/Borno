package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a vocabulary word.
 */
@Entity(tableName = "words")
public class WordEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String word;

    public String definition;
    public String language; // "bn" or "en"
    public String syllables;
    public boolean isSaved;
    public long createdAt;

    public WordEntity(@NonNull String word, String definition, String language, String syllables, boolean isSaved, long createdAt) {
        this.word = word;
        this.definition = definition;
        this.language = language;
        this.syllables = syllables;
        this.isSaved = isSaved;
        this.createdAt = createdAt;
    }
}
