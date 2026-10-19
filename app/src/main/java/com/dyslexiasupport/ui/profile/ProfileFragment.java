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

        setupLinkCodeSection();
        setupCloudSyncSection();
        setupLowEndModeSection();

        binding.btnLogout.setOnClickListener(v -> {
            session.signOut();
            LoginActivity.launchFresh(requireActivity());
        });
    }

    private void setupLinkCodeSection() {
        com.dyslexiasupport.data.model.Role role = session.getRole();
        if (role == com.dyslexiasupport.data.model.Role.PARENT || role == com.dyslexiasupport.data.model.Role.TEACHER) {
            binding.layoutLearnerCode.setVisibility(View.GONE);
            binding.layoutParentLink.setVisibility(View.VISIBLE);

            binding.textLinkedCount.setText(getString(R.string.link_code_linked_count, session.getLinkedCount()));

            binding.btnLinkStudent.setOnClickListener(v -> {
                binding.inputLayoutLinkCode.setError(null);
                String code = binding.editLinkCode.getText() != null ? binding.editLinkCode.getText().toString() : "";
                if (code.trim().isEmpty()) {
                    binding.inputLayoutLinkCode.setError(getString(R.string.link_code_error_invalid));
                    return;
                }

                com.dyslexiasupport.data.firebase.LinkCodeManager.linkStudentByCode(
                        requireContext(),
                        code,
                        new com.dyslexiasupport.data.firebase.LinkCodeManager.OnLinkListener() {
                            @Override
                            public void onSuccess(@NonNull String studentName) {
                                if (binding == null) return;
                                android.widget.Toast.makeText(requireContext(),
                                        getString(R.string.link_code_success) + " (" + studentName + ")",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                binding.editLinkCode.setText("");
                                binding.textLinkedCount.setText(getString(R.string.link_code_linked_count, session.getLinkedCount()));
                            }

                            @Override
                            public void onError(@NonNull String errorMsg) {
                                if (binding == null) return;
                                binding.inputLayoutLinkCode.setError(errorMsg);
                            }
                        }
                );
            });
        } else {
            binding.layoutLearnerCode.setVisibility(View.VISIBLE);
            binding.layoutParentLink.setVisibility(View.GONE);

            String code = session.getLinkCode();
            if (code == null || code.trim().isEmpty()) {
                code = com.dyslexiasupport.data.firebase.LinkCodeManager.generateLinkCode();
                session.setLinkCode(code);
            }
            binding.textLinkCode.setText(code);

            String finalCode = code;
            binding.btnCopyCode.setOnClickListener(v -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                        requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("link_code", finalCode));
                    android.widget.Toast.makeText(requireContext(), R.string.link_code_copied, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupCloudSyncSection() {
        binding.btnSyncNow.setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(), R.string.sync_status_syncing, android.widget.Toast.LENGTH_SHORT).show();
            com.dyslexiasupport.data.firebase.CloudSyncManager.syncAll(requireContext(), success -> {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        binding.textSyncStatus.setText(R.string.sync_status_synced);
                        android.widget.Toast.makeText(requireContext(), R.string.sync_status_synced, android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(requireContext(), R.string.error_generic, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void setupLowEndModeSection() {
        boolean isLowEnd = com.dyslexiasupport.system.LowEndModeManager.isLowEndMode(requireContext());
        binding.switchLowEnd.setChecked(isLowEnd);
        binding.switchLowEnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            com.dyslexiasupport.system.LowEndModeManager.setLowEndModeForced(requireContext(), isChecked);
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
