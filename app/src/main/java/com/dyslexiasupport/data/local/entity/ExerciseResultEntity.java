package com.dyslexiasupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an exercise result.
 */
@Entity(tableName = "exercise_results")
public class ExerciseResultEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String exerciseType;

    public int score;
    public int totalQuestions;
    public long timestamp;
    @NonNull
    public String ownerId = "shared";

    public ExerciseResultEntity(@NonNull String exerciseType, int score, int totalQuestions, long timestamp) {
        this.exerciseType = exerciseType;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = timestamp;
    }
}
