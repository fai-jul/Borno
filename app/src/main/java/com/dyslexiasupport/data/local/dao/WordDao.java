package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dyslexiasupport.data.local.entity.WordEntity;

import java.util.List;

@Dao
public interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WordEntity word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WordEntity> words);

    @Update
    void update(WordEntity word);

    @Delete
    void delete(WordEntity word);

    @Query("SELECT * FROM words WHERE ownerId = :ownerId OR ownerId = 'shared' ORDER BY id DESC")
    LiveData<List<WordEntity>> getAllWords(String ownerId);

    @Query("SELECT * FROM words WHERE isSaved = 1 AND (ownerId = :ownerId OR ownerId = 'shared') ORDER BY id DESC")
    LiveData<List<WordEntity>> getSavedWords(String ownerId);

    @Query("SELECT * FROM words WHERE (ownerId = :ownerId OR ownerId = 'shared') AND (word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%') ORDER BY id DESC")
    LiveData<List<WordEntity>> searchWords(String query, String ownerId);

    @Query("SELECT COUNT(*) FROM words WHERE ownerId = :ownerId OR ownerId = 'shared'")
    int getWordCount(String ownerId);

    @Query("SELECT * FROM words WHERE ownerId = :ownerId ORDER BY id DESC")
    List<WordEntity> getAllWordsSync(String ownerId);

    @Query("SELECT * FROM words WHERE id = :id AND (ownerId = :ownerId OR ownerId = 'shared') LIMIT 1")
    WordEntity getWordByIdSync(long id, String ownerId);

    @Query("SELECT * FROM words WHERE ownerId = :ownerId OR ownerId = 'shared' ORDER BY RANDOM() LIMIT 1")
    WordEntity getRandomWordSync(String ownerId);

    @Query("DELETE FROM words WHERE id = :id AND ownerId = :ownerId")
    int deleteByIdSync(long id, String ownerId);
}
