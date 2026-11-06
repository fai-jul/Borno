package com.dyslexiasupport.ui.library;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.library.ReadingText;
import com.dyslexiasupport.databinding.ItemReadingTextBinding;

/**
 * RecyclerView adapter for the library. ListAdapter + DiffUtil animates only what changed.
 * One ViewHolder is reused for many rows, so bind() must set EVERY field every time.
 */
public class LibraryAdapter extends ListAdapter<LibraryItem, LibraryAdapter.ItemHolder> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull LibraryItem item);
    }

    private static final DiffUtil.ItemCallback<LibraryItem> DIFF =
            new DiffUtil.ItemCallback<LibraryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull LibraryItem a, @NonNull LibraryItem b) {
                    return a.text.id.equals(b.text.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull LibraryItem a, @NonNull LibraryItem b) {
                    return a.equals(b);
                }
            };

    private final OnItemClickListener listener;

    public LibraryAdapter(@NonNull OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public LibraryItem itemAt(int position) {
        return getItem(position);
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReadingTextBinding b = ItemReadingTextBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        private final ItemReadingTextBinding b;
        private LibraryItem item;

        ItemHolder(@NonNull ItemReadingTextBinding binding) {
            super(binding.getRoot());
            b = binding;
            b.getRoot().setOnClickListener(v -> {
                if (item != null) {
                    listener.onItemClick(item);
                }
            });
        }

        void bind(@NonNull LibraryItem libraryItem) {
            item = libraryItem;
            ReadingText t = libraryItem.text;
            Context c = b.getRoot().getContext();

            b.itemEmoji.setText(t.emoji);
            b.itemTitle.setText(t.title);

            GradientDrawable badge = new GradientDrawable();
            badge.setShape(GradientDrawable.OVAL);
            badge.setColor(ColorUtils.blendARGB(
                    ContextCompat.getColor(c, levelColor(t.level)),
                    ContextCompat.getColor(c, R.color.ds_surface), 0.6f));
            b.itemEmoji.setBackground(badge);

            String lang = c.getString(t.isBangla() ? R.string.lang_bn_label : R.string.lang_en_label);
            String durationStr = t.timeFormatted(c);
            b.itemMeta.setText(c.getString(R.string.library_meta, lang, stars(t.level), durationStr));

            // Both flags can be on, so build the badge text from scratch each time.
            StringBuilder flags = new StringBuilder();
            if (libraryItem.favourite) flags.append("⭐");
            if (libraryItem.read) flags.append(flags.length() > 0 ? " ✓" : "✓");
            b.itemBadge.setText(flags);
        }
    }

    private static int levelColor(int level) {
        if (level <= 1) return R.color.ds_leaf;
        if (level == 2) return R.color.ds_mango;
        return R.color.ds_coral;
    }

    static String stars(int level) {
        int n = Math.max(1, Math.min(3, level));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            sb.append(i <= n ? '★' : '☆');
        }
        return sb.toString();
    }
}
