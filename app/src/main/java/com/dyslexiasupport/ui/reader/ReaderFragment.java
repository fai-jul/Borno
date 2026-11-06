package com.dyslexiasupport.ui.reader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.library.ReadingText;
import com.dyslexiasupport.databinding.FragmentReaderBinding;
import com.dyslexiasupport.ui.home.TabHost;
import com.dyslexiasupport.ui.library.LibraryViewModel;

/** Reader: shows ONE text from the library with saved reading settings and Read Aloud controls. */
public class ReaderFragment extends Fragment implements ReaderForegroundService.ReadAloudListener {

    private static final String ARG_TEXT_ID = "text_id";

    private FragmentReaderBinding binding;
    private ReaderViewModel vm;
    private LibraryViewModel library;
    private float downX;
    private float downY;

    @Nullable private BackgroundColorSpan wordHighlight;
    @Nullable private BackgroundColorSpan sentenceHighlightSpan;
    @Nullable private BackgroundColorSpan spokenWordHighlightSpan;

    private ReaderForegroundService readerService;
    private boolean isBound = false;
    private ReadingText currentReadingText;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startReadAloud();
                } else {
                    Toast.makeText(requireContext(), "Notification permission is needed for read aloud", Toast.LENGTH_SHORT).show();
                }
            });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ReaderForegroundService.LocalBinder binder = (ReaderForegroundService.LocalBinder) service;
            readerService = binder.getService();
            isBound = true;
            readerService.setListener(ReaderFragment.this);
            if (currentReadingText != null) {
                readerService.initText(currentReadingText.title, currentReadingText.body);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            readerService = null;
        }
    };

    public static ReaderFragment newInstance(@NonNull String textId) {
        ReaderFragment f = new ReaderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT_ID, textId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReaderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);
        library = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        binding.btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        binding.btnSettings.setOnClickListener(v -> {
            if (getParentFragmentManager().findFragmentByTag("reader_settings") == null) {
                new ReaderSettingsSheet().show(getParentFragmentManager(), "reader_settings");
            }
        });

        binding.btnAssistant.setOnClickListener(v -> {
            if (currentReadingText != null) {
                com.dyslexiasupport.ui.assistant.AssistantActivity.launch(
                        requireContext(), currentReadingText.body, null);
            }
        });

        // Read Aloud Controls
        binding.btnPlayPause.setOnClickListener(v -> checkPermissionAndStartService());
        binding.btnStop.setOnClickListener(v -> stopReadAloud());
        binding.btnPrevSentence.setOnClickListener(v -> {
            if (isBound && readerService != null) readerService.previousSentence();
        });
        binding.btnNextSentence.setOnClickListener(v -> {
            if (isBound && readerService != null) readerService.nextSentence();
        });
        binding.btnSpeed.setOnClickListener(v -> cycleSpeed());

        // Tap a word
        binding.readerText.setOnTouchListener((v, e) -> {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                downX = e.getX();
                downY = e.getY();
            }
            return false;
        });

        binding.readerText.setOnClickListener(v -> onTextTapped(downX, downY));

        binding.guide.setOnBandTapListener((x, y) -> {
            if (binding == null) return;
            int[] guideLoc = new int[2];
            int[] textLoc = new int[2];
            binding.guide.getLocationOnScreen(guideLoc);
            binding.readerText.getLocationOnScreen(textLoc);
            float textX = x + guideLoc[0] - textLoc[0];
            float textY = y + guideLoc[1] - textLoc[1];
            onTextTapped(textX, textY);
        });

        vm.getSettings().observe(getViewLifecycleOwner(), s -> render());
        library.getAll().observe(getViewLifecycleOwner(), list -> render());

        // Bind Read Aloud service
        Intent intent = new Intent(requireContext(), ReaderForegroundService.class);
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private void checkPermissionAndStartService() {
        if (isBound && readerService != null) {
            if (readerService.isPlaying()) {
                readerService.pause();
            } else {
                readerService.play();
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        startReadAloud();
    }

    private void startReadAloud() {
        if (currentReadingText == null) return;
        Intent serviceIntent = new Intent(requireContext(), ReaderForegroundService.class);
        serviceIntent.setAction(ReaderForegroundService.ACTION_START);
        serviceIntent.putExtra(ReaderForegroundService.EXTRA_TEXT_TITLE, currentReadingText.title);
        serviceIntent.putExtra(ReaderForegroundService.EXTRA_TEXT_BODY, currentReadingText.body);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

    }

    private void stopReadAloud() {
        if (isBound && readerService != null) {
            readerService.stopSelfService();
        }
        clearSentenceHighlights();
    }

    private void cycleSpeed() {
        if (!isBound || readerService == null) return;
        float current = readerService.getSpeedRate();
        float next;
        if (current < 0.9f) next = 1.0f;
        else if (current < 1.1f) next = 1.25f;
        else if (current < 1.4f) next = 1.5f;
        else next = 0.75f;

        readerService.setSpeedRate(next);
        binding.btnSpeed.setText(String.format(java.util.Locale.US, "%.2fx", next));
    }

    @Override
    public void onStateChanged(boolean isPlaying, int sentenceIndex, float speedRate) {
        runOnUiThread(() -> {
            if (binding == null) return;
            binding.btnPlayPause.setText(isPlaying ? "⏸  Pause" : "▶  Read Aloud");
            binding.btnSpeed.setText(String.format(java.util.Locale.US, "%.2fx", speedRate));
        });
    }

    @Override
    public void onSentenceHighlighted(int sentenceIndex, int startOffset, int endOffset) {
        runOnUiThread(() -> {
            if (binding == null) return;
            CharSequence text = binding.readerText.getText();
            if (!(text instanceof Spannable)) return;
            Spannable sp = (Spannable) text;

            clearSentenceHighlights();

            if (startOffset >= 0 && endOffset <= text.length() && startOffset < endOffset) {
                int highlightColor = ContextCompat.getColor(requireContext(), R.color.ds_primary_container);
                sentenceHighlightSpan = new BackgroundColorSpan(ColorUtils.setAlphaComponent(highlightColor, 0x99));
                sp.setSpan(sentenceHighlightSpan, startOffset, endOffset, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Layout layout = binding.readerText.getLayout();
                if (layout != null) {
                    int line = layout.getLineForOffset(startOffset);
                    int y = layout.getLineTop(line);
                    binding.readerScroll.smoothScrollTo(0, y);
                }
            }
        });
    }

    @Override
    public void onWordHighlighted(int wordStartOffset, int wordEndOffset) {
        runOnUiThread(() -> {
            if (binding == null) return;
            CharSequence text = binding.readerText.getText();
            if (!(text instanceof Spannable)) return;
            Spannable sp = (Spannable) text;

            if (spokenWordHighlightSpan != null) {
                sp.removeSpan(spokenWordHighlightSpan);
            }

            if (wordStartOffset >= 0 && wordEndOffset <= text.length() && wordStartOffset < wordEndOffset) {
                int mangoColor = ContextCompat.getColor(requireContext(), R.color.ds_mango);
                spokenWordHighlightSpan = new BackgroundColorSpan(ColorUtils.setAlphaComponent(mangoColor, 0xCC));
                sp.setSpan(spokenWordHighlightSpan, wordStartOffset, wordEndOffset, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        });
    }

    @Override
    public void onReadingFinished() {
        runOnUiThread(() -> {
            if (binding == null || currentReadingText == null || !isAdded()) return;
            java.util.List<ComprehensionQuestion> questions =
                    ComprehensionQuestion.fromText(currentReadingText.body);
            showQuestion(questions, 0, new int[]{0});
        });
    }

    private void runOnUiThread(@NonNull Runnable action) {
        if (isAdded()) {
            requireActivity().runOnUiThread(action);
        }
    }

    private void showQuestion(java.util.List<ComprehensionQuestion> questions, int index, int[] score) {
        if (index >= questions.size()) {
            Toast.makeText(requireContext(),
                    getString(R.string.comprehension_result, score[0], questions.size()),
                    Toast.LENGTH_LONG).show();
            return;
        }
        ComprehensionQuestion q = questions.get(index);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.comprehension_title)
                .setMessage(q.prompt)
                .setSingleChoiceItems(q.answers, -1, (dialog, which) -> {
                    if (which == q.correctIndex) score[0]++;
                    dialog.dismiss();
                    showQuestion(questions, index + 1, score);
                })
                .show();
    }

    private void clearSentenceHighlights() {
        if (binding == null) return;
        CharSequence text = binding.readerText.getText();
        if (text instanceof Spannable) {
            Spannable sp = (Spannable) text;
            if (sentenceHighlightSpan != null) sp.removeSpan(sentenceHighlightSpan);
            if (spokenWordHighlightSpan != null) sp.removeSpan(spokenWordHighlightSpan);
        }
        sentenceHighlightSpan = null;
        spokenWordHighlightSpan = null;
    }

    /** x, y are in the TextView's own coordinates (scroll already included). */
    private void onTextTapped(float x, float y) {
        if (binding == null) {
            return;
        }
        TextView tv = binding.readerText;
        Layout layout = tv.getLayout();
        if (layout == null) {
            return;
        }
        int textY = Math.round(y - tv.getTotalPaddingTop());
        if (textY < 0 || textY > layout.getHeight()) {
            return;
        }
        int line = layout.getLineForVertical(textY);
        float textX = x - tv.getTotalPaddingLeft();
        if (textX > layout.getLineRight(line) + 12f * getResources().getDisplayMetrics().density) {
            return;
        }
        CharSequence text = tv.getText();
        int[] bounds = BanglaText.wordBoundsAt(text, layout.getOffsetForHorizontal(line, textX));
        if (bounds == null) {
            return;
        }
        highlightWord(bounds[0], bounds[1]);
        String word = text.subSequence(bounds[0], bounds[1]).toString();
        if (isBound && readerService != null) {
            readerService.repeatWord(word);
        }
        WordSheet.newInstance(word).show(getChildFragmentManager(), "word");
    }

    private void highlightWord(int start, int end) {
        CharSequence text = binding.readerText.getText();
        if (!(text instanceof Spannable)) {
            return;
        }
        Spannable sp = (Spannable) text;
        clearWordHighlight();
        int mango = ContextCompat.getColor(requireContext(), R.color.ds_mango);
        wordHighlight = new BackgroundColorSpan(ColorUtils.setAlphaComponent(mango, 0x80));
        sp.setSpan(wordHighlight, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void clearWordHighlight() {
        if (binding == null || wordHighlight == null) {
            return;
        }
        CharSequence text = binding.readerText.getText();
        if (text instanceof Spannable) {
            ((Spannable) text).removeSpan(wordHighlight);
        }
        wordHighlight = null;
    }

    private void render() {
        ReaderSettings s = vm.getSettings().getValue();
        ReadingText text = library.findById(requireArguments().getString(ARG_TEXT_ID));
        if (binding == null || s == null) {
            return;
        }
        int bg = ContextCompat.getColor(requireContext(), s.theme.bgRes);
        int fg = ContextCompat.getColor(requireContext(), s.theme.textRes);

        binding.readerRoot.setBackgroundColor(bg);
        binding.readerTitle.setTextColor(fg);
        binding.tapHint.setTextColor(ColorUtils.setAlphaComponent(fg, 0xB3));
        binding.btnBack.setTextColor(fg);
        ((TabHost) requireActivity()).setScreenTheme(bg, s.theme.dark);

        if (text == null) {
            return;
        }
        currentReadingText = text;
        library.markAsRead(text.id);

        java.util.List<ReadingText> allTexts = library.getAll().getValue();
        int total = (allTexts != null && !allTexts.isEmpty()) ? allTexts.size() : 1;
        int readCount = Math.min(library.getReadCount(), total);
        binding.readerProgress.setMax(total);
        binding.readerProgress.setProgress(readCount);
        binding.readerProgressText.setText(String.format(java.util.Locale.US, "%d/%d", readCount, total));

        binding.readerTitle.setText(text.title);
        wordHighlight = null;
        ReaderTextRenderer.apply(binding.readerText, text.body, s);

        if (isBound && readerService != null) {
            readerService.initText(text.title, text.body);
        }

        float density = getResources().getDisplayMetrics().density;
        float bandPx = s.guideLines * binding.readerText.getLineHeight() + 8f * density;
        int scrimColor = s.theme.dark
                ? ColorUtils.setAlphaComponent(ContextCompat.getColor(requireContext(), R.color.ds_cream), 0x99)
                : ColorUtils.setAlphaComponent(ContextCompat.getColor(requireContext(), R.color.ds_ink), 0x77);
        binding.guide.configure(s.guideMode, bandPx, scrimColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBound) {
            if (readerService != null) {
                readerService.setListener(null);
            }
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        binding = null;
    }
}
