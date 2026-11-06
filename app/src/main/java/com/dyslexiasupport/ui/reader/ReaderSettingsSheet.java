package com.dyslexiasupport.ui.reader;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dyslexiasupport.R;
import com.dyslexiasupport.databinding.SheetReaderSettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bottom sheet with sliders, theme swatches and the reading guide. Changes apply live. */
public class ReaderSettingsSheet extends BottomSheetDialogFragment {

    private SheetReaderSettingsBinding binding;
    private ReaderViewModel vm;
    private final List<TextView> swatches = new ArrayList<>();
    /** true while WE move the controls, so their listeners don't save anything. */
    private boolean updatingUi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetReaderSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);

        buildSwatches();
        showValues(vm.getSettings().getValue());

        binding.sliderSize.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            ReaderSettings n = current();
            n.fontSizeSp = (int) value;
            commit(n);
        });
        binding.sliderLine.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            ReaderSettings n = current();
            n.lineSpacing = value / 10f;
            commit(n);
        });
        binding.sliderWord.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            ReaderSettings n = current();
            n.wordSpacingDp = (int) value;
            commit(n);
        });
        binding.sliderLetter.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            ReaderSettings n = current();
            n.letterSpacingEm = value / 100f;
            commit(n);
        });
        binding.guideGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingUi || ids.isEmpty()) return;
            int id = ids.get(0);
            ReaderSettings n = current();
            if (id == R.id.chipGuideRuler) {
                n.guideMode = ReadingGuideMode.RULER;
            } else if (id == R.id.chipGuideFocus) {
                n.guideMode = ReadingGuideMode.FOCUS;
            } else {
                n.guideMode = ReadingGuideMode.OFF;
            }
            commit(n);
        });
        binding.sliderGuide.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            ReaderSettings n = current();
            n.guideLines = (int) value;
            commit(n);
        });
        binding.switchSyllables.setOnCheckedChangeListener((button, checked) -> {
            if (updatingUi) return;
            ReaderSettings n = current();
            n.syllableColors = checked;
            commit(n);
        });
        binding.btnReset.setOnClickListener(v -> {
            vm.resetSettings();
            showValues(vm.getSettings().getValue());
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Defer expansion until the sheet has completed its first layout pass.
        // Expanding synchronously here can cause a relayout flicker and a lifecycle
        // crash when the sheet is opened while the reader is being resumed.
        if (getDialog() instanceof BottomSheetDialog && getView() != null) {
            getView().post(() -> {
                if (!isAdded() || !(getDialog() instanceof BottomSheetDialog)) return;
                BottomSheetBehavior<?> behavior = ((BottomSheetDialog) getDialog()).getBehavior();
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            });
        }
    }

    private ReaderSettings current() {
        ReaderSettings settings = vm.getSettings().getValue();
        return settings == null ? ReaderSettings.defaults() : settings.copy();
    }

    private void commit(ReaderSettings n) {
        vm.update(n);
        showLabelsAndSwatches(vm.getSettings().getValue());
    }

    /** Moves every control to match the given settings. */
    private void showValues(ReaderSettings s) {
        updatingUi = true;
        binding.sliderSize.setValue(s.fontSizeSp);
        binding.sliderLine.setValue(Math.round(s.lineSpacing * 10f));
        binding.sliderWord.setValue(s.wordSpacingDp);
        binding.sliderLetter.setValue(Math.round(s.letterSpacingEm * 100f));
        binding.sliderGuide.setValue(s.guideLines);
        binding.switchSyllables.setChecked(s.syllableColors);
        binding.guideGroup.check(s.guideMode == ReadingGuideMode.RULER ? R.id.chipGuideRuler
                : s.guideMode == ReadingGuideMode.FOCUS ? R.id.chipGuideFocus
                : R.id.chipGuideOff);
        updatingUi = false;
        showLabelsAndSwatches(s);
    }

    private void showLabelsAndSwatches(ReaderSettings s) {
        Locale l = Locale.getDefault();
        binding.valueSize.setText(String.format(l, "%d sp", s.fontSizeSp));
        binding.valueLine.setText(String.format(l, "%.1f×", s.lineSpacing));
        binding.valueWord.setText(String.format(l, "%d dp", s.wordSpacingDp));
        binding.valueLetter.setText(String.format(l, "%.2f em", s.letterSpacingEm));
        binding.valueGuide.setText(String.format(l, "%d", s.guideLines));
        binding.sliderGuide.setEnabled(s.guideMode != ReadingGuideMode.OFF);
        refreshSwatches(s.theme);
        updateLivePreview(s);
    }

    private void updateLivePreview(ReaderSettings s) {
        if (binding == null || getContext() == null) return;
        int bg = ContextCompat.getColor(requireContext(), s.theme.bgRes);
        binding.sampleCard.setCardBackgroundColor(bg);
        ReaderTextRenderer.apply(binding.sampleText, getString(R.string.setting_preview_sample), s);
    }

    private void buildSwatches() {
        float d = getResources().getDisplayMetrics().density;
        int size = Math.round(56 * d);
        for (ReaderTheme t : ReaderTheme.values()) {
            TextView sw = new TextView(requireContext());
            // themeRow is a wrapping (flow) layout, so all six themes are always visible.
            sw.setLayoutParams(new ViewGroup.MarginLayoutParams(size, size));
            sw.setGravity(Gravity.CENTER);
            sw.setText("Aa");
            sw.setTextSize(18);
            sw.setTextColor(ContextCompat.getColor(requireContext(), t.textRes));
            sw.setContentDescription(getString(t.nameRes));
            sw.setTag(t);
            sw.setOnClickListener(v -> {
                ReaderSettings n = current();
                n.theme = t;
                commit(n);
            });
            binding.themeRow.addView(sw);
            swatches.add(sw);
        }
    }

    private void refreshSwatches(ReaderTheme selected) {
        float d = getResources().getDisplayMetrics().density;
        for (TextView sw : swatches) {
            ReaderTheme t = (ReaderTheme) sw.getTag();
            boolean isSelected = t == selected;
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(ContextCompat.getColor(requireContext(), t.bgRes));
            g.setStroke(Math.round((isSelected ? 3f : 1.5f) * d),
                    ContextCompat.getColor(requireContext(),
                            isSelected ? R.color.ds_primary : R.color.ds_sand));
            sw.setBackground(g);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swatches.clear();
        binding = null;
    }
}
