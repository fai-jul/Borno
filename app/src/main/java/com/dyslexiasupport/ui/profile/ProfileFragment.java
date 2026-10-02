package com.dyslexiasupport.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.databinding.FragmentProfileBinding;
import com.dyslexiasupport.ui.auth.LoginActivity;

/** Me tab: who is signed in, language switch, log out. */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        binding.avatar.setText(session.getAvatar());
        binding.name.setText(session.getName());
        binding.role.setText(session.getRole().labelRes);
        binding.guestNote.setVisibility(session.isGuest() ? View.VISIBLE : View.GONE);

        // Set the initial choice BEFORE attaching the listener, or we would loop.
        boolean bangla = "bn".equals(currentLanguage());
        binding.langGroup.check(bangla ? R.id.chipBangla : R.id.chipEnglish);
        binding.langGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            String tag = checkedIds.get(0) == R.id.chipBangla ? "bn" : "en";
            if (!tag.equals(currentLanguage())) {
                // Recreates the activity with the new language (saved by AppCompat).
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            session.signOut();
            LoginActivity.launchFresh(requireActivity());
        });
    }

    private String currentLanguage() {
        String lang = requireContext().getResources().getConfiguration()
                .getLocales().get(0).getLanguage();
        return "bn".equals(lang) ? "bn" : "en";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
