package com.dyslexiasupport.ui.reader;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dyslexiasupport.R;
import com.dyslexiasupport.databinding.SheetWordBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

/** Sheet for a tapped word: listen, see its syllables, and decode joined letters. */
public class WordSheet extends BottomSheetDialogFragment {

    private static final String ARG_WORD = "word";

    private SheetWordBinding binding;
    private TextToSpeech tts;
    private boolean ttsReady;
    private String word = "";
    private boolean bangla;

    public static WordSheet newInstance(@NonNull String word) {
        WordSheet sheet = new WordSheet();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetWordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        word = requireArguments().getString(ARG_WORD, "");
        bangla = BanglaText.containsBangla(word);

        binding.wordText.setText(word);
        showBreakdown();
        binding.btnListen.setOnClickListener(v -> speak());

        binding.btnSaveWord.setOnClickListener(v -> {
            com.dyslexiasupport.data.repository.AppRepository repo =
                    new com.dyslexiasupport.data.repository.AppRepository(requireContext());
            com.dyslexiasupport.data.local.entity.WordEntity entity =
                    new com.dyslexiasupport.data.local.entity.WordEntity(
                            word,
                            "",
                            bangla ? "bn" : "en",
                            binding.syllableText != null ? binding.syllableText.getText().toString() : "",
                            true,
                            System.currentTimeMillis()
                    );
            repo.insertWord(entity);
            Toast.makeText(requireContext(), "Saved to My Words", Toast.LENGTH_SHORT).show();
        });

        // Application context: the engine must not hold on to this sheet.
        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (binding == null) {
                return;
            }
            ttsReady = status == TextToSpeech.SUCCESS;
            if (ttsReady) {
                configureVoice();
            } else {
                showVoiceProblem(R.string.word_tts_unavailable, false);
            }
        });
    }

    private void configureVoice() {
        Locale locale = Locale.forLanguageTag(bangla ? "bn-BD" : "en-US");
        int result = tts.isLanguageAvailable(locale);
        if (bangla && (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
            locale = Locale.forLanguageTag("bn-IN"); // some devices only ship this one
            result = tts.isLanguageAvailable(locale);
        }
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            ttsReady = false;
            showVoiceProblem(bangla ? R.string.word_no_bangla_voice : R.string.word_tts_unavailable,
                    bangla);
            return;
        }
        tts.setLanguage(locale);
    }

    private void showVoiceProblem(@StringRes int message, boolean offerInstall) {
        binding.ttsMessage.setText(message);
        binding.ttsMessage.setVisibility(View.VISIBLE);
        binding.btnInstallVoice.setVisibility(offerInstall ? View.VISIBLE : View.GONE);
        binding.btnInstallVoice.setOnClickListener(v -> {
            try {
                startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), R.string.word_tts_unavailable,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speak() {
        if (ttsReady && tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word");
        } else if (binding.ttsMessage.getVisibility() != View.VISIBLE) {
            Toast.makeText(requireContext(), R.string.word_tts_unavailable,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Syllable line + list of joined letters (Bangla words only). */
    private void showBreakdown() {
        if (!bangla) {
            binding.partsSection.setVisibility(View.GONE);
            binding.joinedSection.setVisibility(View.GONE);
            return;
        }
        List<String> parts = BanglaText.aksharaStrings(word);
        int ink = ContextCompat.getColor(requireContext(), R.color.ds_ink);
        int accent = ContextCompat.getColor(requireContext(), R.color.reader_accent_light);
        int soft = ContextCompat.getColor(requireContext(), R.color.ds_ink_soft);

        SpannableStringBuilder line = new SpannableStringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                int s = line.length();
                line.append("  ·  ");
                line.setSpan(new ForegroundColorSpan(soft), s, line.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            int s = line.length();
            line.append(parts.get(i));
            line.setSpan(new ForegroundColorSpan(i % 2 == 0 ? ink : accent), s, line.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.syllableText.setText(line);

        boolean anyJoined = false;
        for (String part : parts) {
            if (!BanglaText.isConjunct(part)) {
                continue;
            }
            anyJoined = true;
            List<String> pieces = BanglaText.conjunctParts(part);
            SpannableStringBuilder row = new SpannableStringBuilder(part);
            row.setSpan(new RelativeSizeSpan(1.5f), 0, row.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            row.setSpan(new StyleSpan(Typeface.BOLD), 0, row.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            row.append("   =   ").append(String.join("  +  ", pieces));

            TextView tv = new TextView(requireContext());
            tv.setTextAppearance(R.style.Ds_Text_Body);
            tv.setTextSize(20);
            tv.setTextColor(ink);
            tv.setText(row);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Math.round(10 * getResources().getDisplayMetrics().density);
            binding.joinedList.addView(tv, lp);
        }
        binding.joinedSection.setVisibility(anyJoined ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetBehavior<?> behavior = ((BottomSheetDialog) getDialog()).getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment parent = getParentFragment();
        if (parent instanceof ReaderFragment) {
            ((ReaderFragment) parent).clearWordHighlight();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        binding = null;
    }
}
