package com.dyslexiasupport.ui.common;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.dyslexiasupport.databinding.ActivityPlaceholderBinding;

/** Temporary "coming soon" screen. Each later phase replaces one card target. */
public class PlaceholderActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "extra_title";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPlaceholderBinding binding = ActivityPlaceholderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        binding.placeholderTitle.setText(title);
        binding.btnBack.setOnClickListener(v -> finish());
    }
}
