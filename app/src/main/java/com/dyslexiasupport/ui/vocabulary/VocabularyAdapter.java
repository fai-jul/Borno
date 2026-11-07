package com.dyslexiasupport.ui.vocabulary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.entity.WordEntity;
import com.dyslexiasupport.databinding.ItemVocabularyWordBinding;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends BaseAdapter {

    public interface OnWordActionListener {
        void onToggleFavorite(@NonNull WordEntity word);
        void onListenWord(@NonNull WordEntity word);
    }

    private final OnWordActionListener listener;
    private List<WordEntity> items = new ArrayList<>();

    public VocabularyAdapter(@NonNull OnWordActionListener listener) {
        this.listener = listener;
    }

    public void submitList(@Nullable List<WordEntity> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public WordEntity getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            ItemVocabularyWordBinding binding = ItemVocabularyWordBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            view = binding.getRoot();
            holder = new ViewHolder(binding);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        WordEntity item = getItem(position);
        Context context = parent.getContext();
        ItemVocabularyWordBinding b = holder.binding;

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

        return view;
    }

    private static class ViewHolder {
        final ItemVocabularyWordBinding binding;
        ViewHolder(@NonNull ItemVocabularyWordBinding binding) {
            this.binding = binding;
        }
    }
}
