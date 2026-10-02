package com.dyslexiasupport.ui.vocabulary;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.entity.WordEntity;
import com.dyslexiasupport.databinding.ActivityVocabularyBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.reader.BanglaText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Vocabulary Screen: displays words stored in Room database. */
public class VocabularyActivity extends BaseActivity implements VocabularyAdapter.OnWordActionListener {

    private ActivityVocabularyBinding binding;
    private VocabularyViewModel vm;
    private VocabularyAdapter adapter;
    private TextToSpeech tts;
    private boolean ttsReady;

    private List<WordEntity> allWords = new ArrayList<>();
    private String currentQuery = "";
    private int currentFilter = 0; // 0=All, 1=Saved, 2=Bangla, 3=English

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());

        vm = new ViewModelProvider(this).get(VocabularyViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddWord.setOnClickListener(v -> showAddWordDialog());

        setupTTS();
        setupList();
        setupFilterAndSearch();

        vm.getWords().observe(this, list -> {
            allWords = list != null ? list : new ArrayList<>();
            applyFilterAndSearch();
        });
    }

    private void setupList() {
        adapter = new VocabularyAdapter(this);
        binding.recyclerWords.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerWords.setAdapter(adapter);
    }

    private void setupFilterAndSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim().toLowerCase();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipSaved) {
                currentFilter = 1;
            } else if (id == R.id.chipBangla) {
                currentFilter = 2;
            } else if (id == R.id.chipEnglish) {
                currentFilter = 3;
            } else {
                currentFilter = 0;
            }
            applyFilterAndSearch();
        });
    }

    private void applyFilterAndSearch() {
        List<WordEntity> filtered = new ArrayList<>();
        for (WordEntity w : allWords) {
            // Filter mode
            if (currentFilter == 1 && !w.isSaved) continue;
            if (currentFilter == 2 && !"bn".equalsIgnoreCase(w.language)) continue;
            if (currentFilter == 3 && !"en".equalsIgnoreCase(w.language)) continue;

            // Search query
            if (!currentQuery.isEmpty()) {
                boolean matchWord = w.word.toLowerCase().contains(currentQuery);
                boolean matchDef = w.definition != null && w.definition.toLowerCase().contains(currentQuery);
                if (!matchWord && !matchDef) continue;
            }
            filtered.add(w);
        }

        adapter.submitList(filtered);
        binding.textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddWordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        EditText editWord = dialogView.findViewById(R.id.editWord);
        EditText editDefinition = dialogView.findViewById(R.id.editDefinition);
        EditText editSyllables = dialogView.findViewById(R.id.editSyllables);
        RadioGroup radioLang = dialogView.findViewById(R.id.radioLang);

        new AlertDialog.Builder(this)
                .setTitle("Add New Word")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String wordStr = editWord.getText().toString().trim();
                    if (wordStr.isEmpty()) {
                        Toast.makeText(this, "Word cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String defStr = editDefinition.getText().toString().trim();
                    String sylStr = editSyllables.getText().toString().trim();
                    String langStr = radioLang.getCheckedRadioButtonId() == R.id.radioBn ? "bn" : "en";

                    vm.addWord(wordStr, defStr, langStr, sylStr);
                    Toast.makeText(this, "Word added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupTTS() {
        tts = new TextToSpeech(getApplicationContext(), status -> {
            ttsReady = status == TextToSpeech.SUCCESS;
        });
    }

    @Override
    public void onToggleFavorite(@NonNull WordEntity word) {
        vm.toggleSaved(word);
    }

    @Override
    public void onListenWord(@NonNull WordEntity word) {
        if (!ttsReady || tts == null) {
            Toast.makeText(this, R.string.word_tts_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isBn = "bn".equalsIgnoreCase(word.language) || BanglaText.containsBangla(word.word);
        Locale locale = Locale.forLanguageTag(isBn ? "bn-BD" : "en-US");
        tts.setLanguage(locale);
        tts.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, "vocab");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        binding = null;
    }
}
