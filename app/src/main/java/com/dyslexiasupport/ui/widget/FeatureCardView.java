package com.dyslexiasupport.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.dyslexiasupport.R;
import com.dyslexiasupport.databinding.ViewFeatureCardBinding;
import com.google.android.material.card.MaterialCardView;

/**
 * Reusable big rounded card: emoji badge, title, subtitle, tinted by an accent colour.
 * In XML use app:featureTitle, featureSubtitle, featureEmoji and featureAccent.
 */
public class FeatureCardView extends MaterialCardView {

    private final ViewFeatureCardBinding binding;

    public FeatureCardView(Context context) {
        this(context, null);
    }

    public FeatureCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewStyle);
    }

    public FeatureCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = ViewFeatureCardBinding.inflate(LayoutInflater.from(context), this, true);

        setRadius(getResources().getDimension(R.dimen.ds_radius_lg));
        setCardElevation(0f);
        setClickable(true);
        setFocusable(true);

        int defaultAccent = ContextCompat.getColor(context, R.color.ds_primary);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FeatureCardView);
        try {
            setTitle(a.getString(R.styleable.FeatureCardView_featureTitle));
            setSubtitle(a.getString(R.styleable.FeatureCardView_featureSubtitle));
            setEmoji(a.getString(R.styleable.FeatureCardView_featureEmoji));
            int image = a.getResourceId(R.styleable.FeatureCardView_featureImage, 0);
            if (image != 0) {
                binding.featureEmoji.setVisibility(INVISIBLE);
                binding.featureIconContainer.setBackground(null);
                binding.featureImage.setImageResource(image);
                binding.featureImage.setVisibility(VISIBLE);
            }
            setAccentColor(a.getColor(R.styleable.FeatureCardView_featureAccent, defaultAccent));
        } finally {
            a.recycle();
        }
    }

    public void setTitle(@Nullable CharSequence title) {
        binding.featureTitle.setText(title);
        setContentDescription(title);
    }

    public void setSubtitle(@Nullable CharSequence subtitle) {
        binding.featureSubtitle.setText(subtitle);
    }

    public void setEmoji(@Nullable CharSequence emoji) {
        binding.featureEmoji.setText(emoji);
    }

    /** Card background becomes a soft pastel of the accent. */
    public void setAccentColor(@ColorInt int accent) {
        int surface = ContextCompat.getColor(getContext(), R.color.ds_surface);
        setCardBackgroundColor(ColorUtils.blendARGB(accent, surface, 0.18f));
        setStrokeColor(ColorUtils.setAlphaComponent(accent, 20));
        setStrokeWidth(0);
        setRippleColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(accent, 36)));
        binding.featureArrow.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.blendARGB(accent, surface, 0.45f)));
        binding.featureEmoji.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.blendARGB(accent, surface, 0.45f)));
    }
}
