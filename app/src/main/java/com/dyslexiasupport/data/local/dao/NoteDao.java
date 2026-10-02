package com.dyslexiasupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dyslexiasupport.data.local.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NoteEntity note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NoteEntity> notes);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    LiveData<List<NoteEntity>> getAllNotes();
}
