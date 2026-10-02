package com.dyslexiasupport.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.dyslexiasupport.databinding.FragmentComingSoonBinding;

/** Placeholder tab content. Replaced tab by tab in later phases. */
public class ComingSoonFragment extends Fragment {

    private static final String ARG_TITLE = "title_res";
    private static final String ARG_EMOJI = "emoji";

    private FragmentComingSoonBinding binding;

    public static ComingSoonFragment newInstance(@StringRes int titleRes, String emoji) {
        ComingSoonFragment f = new ComingSoonFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, titleRes);
        args.putString(ARG_EMOJI, emoji);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentComingSoonBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        binding.emoji.setText(args.getString(ARG_EMOJI, ""));
        binding.title.setText(args.getInt(ARG_TITLE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
