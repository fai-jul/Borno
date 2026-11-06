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

    @Query("SELECT * FROM documents WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    LiveData<List<DocumentEntity>> getAllDocuments(String ownerId);

    @Query("DELETE FROM documents WHERE id = :id AND ownerId = :ownerId")
    void deleteById(long id, String ownerId);
}
