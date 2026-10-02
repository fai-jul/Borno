package com.dyslexiasupport.ui.vocabulary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.entity.WordEntity;
import com.dyslexiasupport.databinding.ItemVocabularyWordBinding;

public class VocabularyAdapter extends ListAdapter<WordEntity, VocabularyAdapter.WordHolder> {

    public interface OnWordActionListener {
        void onToggleFavorite(@NonNull WordEntity word);
        void onListenWord(@NonNull WordEntity word);
    }

    private static final DiffUtil.ItemCallback<WordEntity> DIFF = new DiffUtil.ItemCallback<WordEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull WordEntity oldItem, @NonNull WordEntity newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WordEntity oldItem, @NonNull WordEntity newItem) {
            return oldItem.id == newItem.id
                    && oldItem.isSaved == newItem.isSaved
                    && oldItem.word.equals(newItem.word)
                    && (oldItem.definition == null ? newItem.definition == null : oldItem.definition.equals(newItem.definition));
        }
    };

    private final OnWordActionListener listener;

    public VocabularyAdapter(@NonNull OnWordActionListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyWordBinding binding = ItemVocabularyWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WordHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class WordHolder extends RecyclerView.ViewHolder {

        private final ItemVocabularyWordBinding b;

        WordHolder(@NonNull ItemVocabularyWordBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(@NonNull WordEntity item) {
            Context context = b.getRoot().getContext();
            b.textWord.setText(item.word);

            if (item.syllables != null && !item.syllables.trim().isEmpty()) {
                b.textSyllables.setText(item.syllables);
                b.textSyllables.setVisibility(View.VISIBLE);
            } else {
                b.textSyllables.setVisibility(View.GONE);
            }

            if (item.definition != null && !item.definition.trim().isEmpty()) {
                b.textDefinition.setText(item.definition);
                b.textDefinition.setVisibility(View.VISIBLE);
            } else {
                b.textDefinition.setVisibility(View.GONE);
            }

            boolean isBn = "bn".equalsIgnoreCase(item.language);
            b.badgeLang.setText(context.getString(isBn ? R.string.lang_bangla : R.string.lang_english));

            int starRes = item.isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off;
            b.btnFavorite.setImageResource(starRes);

            b.btnFavorite.setOnClickListener(v -> listener.onToggleFavorite(item));
            b.btnListen.setOnClickListener(v -> listener.onListenWord(item));
        }
    }
}
