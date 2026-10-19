package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dyslexiasupport.data.local.entity.ExerciseResultEntity;

import java.util.List;

@Dao
public interface ExerciseResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExerciseResultEntity result);

    @Query("SELECT * FROM exercise_results ORDER BY timestamp DESC")
    LiveData<List<ExerciseResultEntity>> getAllResults();

    @Query("SELECT * FROM exercise_results ORDER BY timestamp DESC")
    List<ExerciseResultEntity> getAllResultsSync();
}
