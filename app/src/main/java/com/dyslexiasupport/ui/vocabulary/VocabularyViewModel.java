package com.dyslexiasupport.ui.vocabulary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dyslexiasupport.data.local.entity.WordEntity;
import com.dyslexiasupport.data.repository.AppRepository;

import java.util.List;

public class VocabularyViewModel extends AndroidViewModel {

    private final AppRepository repository;

    public VocabularyViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<List<WordEntity>> getWords() {
        return repository.getAllWords();
    }

    public void toggleSaved(WordEntity word) {
        repository.toggleWordSaved(word);
    }

    public void addWord(@NonNull String word, String definition, String language, String syllables) {
        WordEntity entity = new WordEntity(
                word,
                definition,
                language,
                syllables,
                true,
                System.currentTimeMillis()
        );
        repository.insertWord(entity);
    }

    public void deleteWord(WordEntity word) {
        repository.deleteWord(word);
    }
}
