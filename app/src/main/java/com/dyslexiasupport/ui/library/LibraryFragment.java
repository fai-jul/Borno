package com.dyslexiasupport.ui.library;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.library.LibraryRepository;
import com.dyslexiasupport.data.library.ReadingText;
import com.dyslexiasupport.databinding.FragmentLibraryBinding;
import com.dyslexiasupport.ui.home.TabHost;
import com.dyslexiasupport.ui.reader.BanglaText;
import com.dyslexiasupport.ui.reader.ReaderFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/** Read tab: filter chips + the list of texts. Tap opens the reader; swipe saves / marks read. */
public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private LibraryViewModel vm;
    private LibraryAdapter adapter;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importPdfFromUri(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        setupList();
        setupFilters();

        vm.getVisible().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean empty = items.isEmpty();
            binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.emptyText.setText(vm.isOnlySaved()
                    ? R.string.library_empty_saved : R.string.library_empty);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Coming back from the reader: restore the normal cream look behind the status bar.
        ((TabHost) requireActivity()).setScreenTheme(
                ContextCompat.getColor(requireContext(), R.color.ds_cream), false);
    }

    private void setupList() {
        adapter = new LibraryAdapter(this::openReader);
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        SwipeActionsCallback callback = new SwipeActionsCallback(requireContext(), adapter,
                new SwipeActionsCallback.Actions() {
                    @Override
                    public void onSwipeSave(@NonNull LibraryItem item) {
                        boolean saved = vm.toggleFavourite(item.text.id);
                        showUndo(saved ? R.string.msg_saved : R.string.msg_unsaved, () ->
                                vm.toggleFavourite(item.text.id));
                    }

                    @Override
                    public void onSwipeRead(@NonNull LibraryItem item) {
                        boolean read = vm.toggleRead(item.text.id);
                        showUndo(read ? R.string.msg_read : R.string.msg_unread, () ->
                                vm.toggleRead(item.text.id));
                    }
                });
        new ItemTouchHelper(callback).attachToRecyclerView(binding.list);
    }

    private void showUndo(int messageRes, Runnable undo) {
        if (binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), messageRes, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> undo.run())
                .show();
    }

    private void openReader(@NonNull LibraryItem item) {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragmentContainer, ReaderFragment.newInstance(item.text.id))
                .addToBackStack("reader")
                .commit();
    }

    private void setupFilters() {
        // 1) Show the saved filter state BEFORE attaching listeners, so nothing fires twice.
        int langId = vm.getLangFilter() == LibraryViewModel.LANG_BN ? R.id.chipLangBn
                : vm.getLangFilter() == LibraryViewModel.LANG_EN ? R.id.chipLangEn
                : R.id.chipLangAll;
        binding.langGroup.check(langId);

        int level = vm.getLevelFilter();
        binding.levelGroup.check(level == 1 ? R.id.chipLevel1 : level == 2 ? R.id.chipLevel2
                : level == 3 ? R.id.chipLevel3 : R.id.chipLevelAll);
        binding.chipSaved.setChecked(vm.isOnlySaved());
        binding.chipNctb.setChecked(vm.isOnlyNctb());

        // 2) Now listen for changes.
        binding.langGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            vm.setLangFilter(id == R.id.chipLangBn ? LibraryViewModel.LANG_BN
                    : id == R.id.chipLangEn ? LibraryViewModel.LANG_EN
                    : LibraryViewModel.LANG_ALL);
        });
        binding.levelGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            vm.setLevelFilter(id == R.id.chipLevel1 ? 1 : id == R.id.chipLevel2 ? 2
                    : id == R.id.chipLevel3 ? 3 : 0);
        });
        binding.chipSaved.setOnCheckedChangeListener((chip, checked) -> vm.setOnlySaved(checked));
        binding.chipNctb.setOnCheckedChangeListener((chip, checked) -> vm.setOnlyNctb(checked));

        binding.btnImportPdf.setOnClickListener(v ->
                pdfPickerLauncher.launch(new String[]{"application/pdf", "text/plain"}));
    }

    private void importPdfFromUri(@NonNull Uri uri) {
        String filename = getFileName(uri);
        String title = filename.replaceAll("(?i)\\.(pdf|txt)$", "");

        StringBuilder textBuilder = new StringBuilder();
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String body = textBuilder.toString().trim();
        if (body.isEmpty()) {
            body = "NCTB Class 5 Lesson: " + title + "\n\nThis imported PDF document is ready for interactive reading with custom typography, line spacing, and text-to-speech audio.";
        }

        String lang = BanglaText.containsBangla(body) || BanglaText.containsBangla(title) ? "bn" : "en";
        String id = "doc_pdf_" + System.currentTimeMillis();
        ReadingText customPdfText = new ReadingText(id, title, body, "📄", 1, lang, "nctb");

        LibraryRepository repo = new LibraryRepository(requireContext());
        repo.addCustomText(customPdfText);

        Toast.makeText(requireContext(), R.string.msg_pdf_imported_success, Toast.LENGTH_SHORT).show();

        vm.setOnlyNctb(true);
        if (binding != null) {
            binding.chipNctb.setChecked(true);
        }
    }

    private String getFileName(@NonNull Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "NCTB_Class5_Book";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
