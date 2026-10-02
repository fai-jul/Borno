package com.dyslexiasupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dyslexiasupport.data.local.AppDatabase;
import com.dyslexiasupport.data.local.dao.DocumentDao;
import com.dyslexiasupport.data.local.dao.ExerciseResultDao;
import com.dyslexiasupport.data.local.dao.NoteDao;
import com.dyslexiasupport.data.local.dao.ReadingSessionDao;
import com.dyslexiasupport.data.local.dao.SentenceDao;
import com.dyslexiasupport.data.local.dao.WordDao;
import com.dyslexiasupport.data.local.entity.DocumentEntity;
import com.dyslexiasupport.data.local.entity.ExerciseResultEntity;
import com.dyslexiasupport.data.local.entity.NoteEntity;
import com.dyslexiasupport.data.local.entity.ReadingSessionEntity;
import com.dyslexiasupport.data.local.entity.SentenceEntity;
import com.dyslexiasupport.data.local.entity.WordEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main repository executing Room DB operations on background threads using an ExecutorService.
 */
public class AppRepository {

    private final WordDao wordDao;
    private final SentenceDao sentenceDao;
    private final NoteDao noteDao;
    private final ReadingSessionDao readingSessionDao;
    private final ExerciseResultDao exerciseResultDao;
    private final DocumentDao documentDao;
    private final ExecutorService executor;

    public AppRepository(@NonNull Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.wordDao = db.wordDao();
        this.sentenceDao = db.sentenceDao();
        this.noteDao = db.noteDao();
        this.readingSessionDao = db.readingSessionDao();
        this.exerciseResultDao = db.exerciseResultDao();
        this.documentDao = db.documentDao();
        this.executor = Executors.newFixedThreadPool(4);
    }

    // --- Words ---
    public LiveData<List<WordEntity>> getAllWords() {
        return wordDao.getAllWords();
    }

    public LiveData<List<WordEntity>> getSavedWords() {
        return wordDao.getSavedWords();
    }

    public LiveData<List<WordEntity>> searchWords(String query) {
        return wordDao.searchWords(query);
    }

    public void insertWord(WordEntity word) {
        executor.execute(() -> wordDao.insert(word));
    }

    public void updateWord(WordEntity word) {
        executor.execute(() -> wordDao.update(word));
    }

    public void deleteWord(WordEntity word) {
        executor.execute(() -> wordDao.delete(word));
    }

    public void toggleWordSaved(WordEntity word) {
        executor.execute(() -> {
            word.isSaved = !word.isSaved;
            wordDao.update(word);
        });
    }

    // --- Sentences ---
    public LiveData<List<SentenceEntity>> getAllSentences() {
        return sentenceDao.getAllSentences();
    }

    public void insertSentence(SentenceEntity sentence) {
        executor.execute(() -> sentenceDao.insert(sentence));
    }

    public void deleteSentence(SentenceEntity sentence) {
        executor.execute(() -> sentenceDao.delete(sentence));
    }

    // --- Notes ---
    public LiveData<List<NoteEntity>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public void insertNote(NoteEntity note) {
        executor.execute(() -> noteDao.insert(note));
    }

    public void updateNote(NoteEntity note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> noteDao.delete(note));
    }

    // --- Reading Sessions ---
    public LiveData<List<ReadingSessionEntity>> getAllReadingSessions() {
        return readingSessionDao.getAllSessions();
    }

    public LiveData<Long> getTotalReadingTimeSeconds() {
        return readingSessionDao.getTotalReadingTimeSeconds();
    }

    public void insertReadingSession(ReadingSessionEntity session) {
        executor.execute(() -> readingSessionDao.insert(session));
    }

    // --- Exercise Results ---
    public LiveData<List<ExerciseResultEntity>> getAllExerciseResults() {
        return exerciseResultDao.getAllResults();
    }

    public void insertExerciseResult(ExerciseResultEntity result) {
        executor.execute(() -> exerciseResultDao.insert(result));
    }

    // --- Documents (scanned texts) ---
    public LiveData<List<DocumentEntity>> getAllDocuments() {
        return documentDao.getAllDocuments();
    }

    public void insertDocument(DocumentEntity document) {
        executor.execute(() -> documentDao.insert(document));
    }

    public void deleteDocumentById(long id) {
        executor.execute(() -> documentDao.deleteById(id));
    }
}
