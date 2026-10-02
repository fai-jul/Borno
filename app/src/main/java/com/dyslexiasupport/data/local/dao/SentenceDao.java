package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dyslexiasupport.data.local.entity.SentenceEntity;

import java.util.List;

@Dao
public interface SentenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SentenceEntity sentence);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SentenceEntity> sentences);

    @Delete
    void delete(SentenceEntity sentence);

    @Query("SELECT * FROM sentences ORDER BY id DESC")
    LiveData<List<SentenceEntity>> getAllSentences();

    @Query("SELECT COUNT(*) FROM sentences")
    int getSentenceCount();
}
