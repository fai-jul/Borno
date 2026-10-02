package com.dyslexiasupport.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.databinding.FragmentHomeBinding;

/** Home tab: greeting + four big entry cards + share. */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        binding.greeting.setText(getString(R.string.home_greeting_name, session.getName()));

        binding.cardRead.setOnClickListener(v -> tabs().selectTab(R.id.nav_read));
        binding.cardPractice.setOnClickListener(v -> tabs().selectTab(R.id.nav_practice));
        binding.cardWords.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.dyslexiasupport.ui.vocabulary.VocabularyActivity.class)));
        binding.cardScan.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.dyslexiasupport.ui.scan.ScanActivity.class)));
        binding.btnShare.setOnClickListener(v -> shareApp());
    }

    private TabHost tabs() {
        return (TabHost) requireActivity();
    }

    private void shareApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        startActivity(Intent.createChooser(send, getString(R.string.action_share)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // fragments outlive their views
    }
}
