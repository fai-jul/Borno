package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dyslexiasupport.data.local.entity.ReadingSessionEntity;

import java.util.List;

@Dao
public interface ReadingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReadingSessionEntity session);

    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC")
    LiveData<List<ReadingSessionEntity>> getAllSessions();

    @Query("SELECT SUM(durationSeconds) FROM reading_sessions")
    LiveData<Long> getTotalReadingTimeSeconds();

    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC")
    List<ReadingSessionEntity> getAllSessionsSync();
}
