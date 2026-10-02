package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dyslexiasupport.data.local.entity.DocumentEntity;

import java.util.List;

@Dao
public interface DocumentDao {

    @Insert
    long insert(DocumentEntity document);

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    LiveData<List<DocumentEntity>> getAllDocuments();

    @Query("DELETE FROM documents WHERE id = :id")
    void deleteById(long id);
}
