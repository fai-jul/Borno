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

    @Query("SELECT * FROM words ORDER BY id DESC")
    LiveData<List<WordEntity>> getAllWords();

    @Query("SELECT * FROM words WHERE isSaved = 1 ORDER BY id DESC")
    LiveData<List<WordEntity>> getSavedWords();

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<WordEntity>> searchWords(String query);

    @Query("SELECT COUNT(*) FROM words")
    int getWordCount();
}
