package com.dyslexiasupport.ui.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.dyslexiasupport.R;

/**
 * Swipe right = save (or unsave). Swipe left = mark read (or unread).
 * The row snaps back after the swipe; the list update then shows the new badge.
 */
public class SwipeActionsCallback extends ItemTouchHelper.SimpleCallback {

    public interface Actions {
        void onSwipeSave(@NonNull LibraryItem item);

        void onSwipeRead(@NonNull LibraryItem item);
    }

    private final LibraryAdapter adapter;
    private final Actions actions;
    private final Context context;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float radius;
    private final float padding;

    public SwipeActionsCallback(@NonNull Context context, @NonNull LibraryAdapter adapter,
                                @NonNull Actions actions) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.actions = actions;
        float density = context.getResources().getDisplayMetrics().density;
        radius = context.getResources().getDimension(R.dimen.ds_radius_lg);
        padding = 24f * density;
        textPaint.setTextSize(16f * context.getResources().getDisplayMetrics().scaledDensity);
        textPaint.setColor(ContextCompat.getColor(context, R.color.ds_ink));
        textPaint.setFakeBoldText(true);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.hind_siliguri));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // we never reorder
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        LibraryItem item = adapter.itemAt(position);
        if (direction == ItemTouchHelper.RIGHT) {
            actions.onSwipeSave(item);
        } else {
            actions.onSwipeRead(item);
        }
        adapter.notifyItemChanged(position); // put the row back in place
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                            @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
            View v = vh.itemView;
            int position = vh.getAdapterPosition();
            LibraryItem item = position == RecyclerView.NO_POSITION ? null : adapter.itemAt(position);
            boolean right = dX > 0;

            rect.set(right ? v.getLeft() : v.getRight() + dX, v.getTop(),
                    right ? v.getLeft() + dX : v.getRight(), v.getBottom());
            bgPaint.setColor(ContextCompat.getColor(context, right ? R.color.ds_mango : R.color.ds_leaf));
            c.drawRoundRect(rect, radius, radius, bgPaint);

            int labelRes;
            if (right) {
                labelRes = item != null && item.favourite ? R.string.swipe_unsave : R.string.swipe_save;
            } else {
                labelRes = item != null && item.read ? R.string.swipe_unread : R.string.swipe_read;
            }
            textPaint.setTextAlign(right ? Paint.Align.LEFT : Paint.Align.RIGHT);
            float x = right ? v.getLeft() + padding : v.getRight() - padding;
            float y = (v.getTop() + v.getBottom()) / 2f
                    - (textPaint.descent() + textPaint.ascent()) / 2f;
            c.save();
            c.clipRect(rect);
            c.drawText(context.getString(labelRes), x, y, textPaint);
            c.restore();
        }
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
    }
}
