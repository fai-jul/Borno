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
import com.dyslexiasupport.data.local.SessionManager;

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
    private final String ownerId;

    public AppRepository(@NonNull Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.wordDao = db.wordDao();
        this.sentenceDao = db.sentenceDao();
        this.noteDao = db.noteDao();
        this.readingSessionDao = db.readingSessionDao();
        this.exerciseResultDao = db.exerciseResultDao();
        this.documentDao = db.documentDao();
        this.executor = Executors.newFixedThreadPool(4);
        this.ownerId = new SessionManager(context).getOwnerId();
    }

    // --- Words ---
    public LiveData<List<WordEntity>> getAllWords() {
        return wordDao.getAllWords(ownerId);
    }

    public LiveData<List<WordEntity>> getSavedWords() {
        return wordDao.getSavedWords(ownerId);
    }

    public LiveData<List<WordEntity>> searchWords(String query) {
        return wordDao.searchWords(query, ownerId);
    }

    public void insertWord(WordEntity word) {
        executor.execute(() -> {
            word.ownerId = ownerId;
            wordDao.insert(word);
        });
    }

    public void updateWord(WordEntity word) {
        executor.execute(() -> {
            word.ownerId = ownerId;
            wordDao.update(word);
        });
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
        return sentenceDao.getAllSentences(ownerId);
    }

    public void insertSentence(SentenceEntity sentence) {
        executor.execute(() -> {
            sentence.ownerId = ownerId;
            sentenceDao.insert(sentence);
        });
    }

    public void deleteSentence(SentenceEntity sentence) {
        executor.execute(() -> sentenceDao.delete(sentence));
    }

    // --- Notes ---
    public LiveData<List<NoteEntity>> getAllNotes() {
        return noteDao.getAllNotes(ownerId);
    }

    public void insertNote(NoteEntity note) {
        executor.execute(() -> {
            note.ownerId = ownerId;
            noteDao.insert(note);
        });
    }

    public void updateNote(NoteEntity note) {
        executor.execute(() -> {
            note.ownerId = ownerId;
            noteDao.update(note);
        });
    }

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> noteDao.delete(note));
    }

    // --- Reading Sessions ---
    public LiveData<List<ReadingSessionEntity>> getAllReadingSessions() {
        return readingSessionDao.getAllSessions(ownerId);
    }

    public LiveData<Long> getTotalReadingTimeSeconds() {
        return readingSessionDao.getTotalReadingTimeSeconds(ownerId);
    }

    public void insertReadingSession(ReadingSessionEntity session) {
        executor.execute(() -> {
            session.ownerId = ownerId;
            readingSessionDao.insert(session);
        });
    }

    // --- Exercise Results ---
    public LiveData<List<ExerciseResultEntity>> getAllExerciseResults() {
        return exerciseResultDao.getAllResults(ownerId);
    }

    public void insertExerciseResult(ExerciseResultEntity result) {
        executor.execute(() -> {
            result.ownerId = ownerId;
            exerciseResultDao.insert(result);
        });
    }

    // --- Documents (scanned texts) ---
    public LiveData<List<DocumentEntity>> getAllDocuments() {
        return documentDao.getAllDocuments(ownerId);
    }

    public void insertDocument(DocumentEntity document) {
        executor.execute(() -> {
            document.ownerId = ownerId;
            documentDao.insert(document);
        });
    }

    public void deleteDocumentById(long id) {
        executor.execute(() -> documentDao.deleteById(id, ownerId));
    }
}
