package com.dyslexiasupport.ui.profile;

import android.content.Intent;
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
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.databinding.FragmentProfileBinding;
import com.dyslexiasupport.ui.onboarding.AssessmentActivity;
import com.dyslexiasupport.ui.auth.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.Toast;

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

        setAvatar();
        binding.avatar.setOnClickListener(v -> showAvatarSelectionDialog());
        binding.name.setText(session.getName());
        binding.role.setText(session.getRole().labelRes);
        binding.role.setOnClickListener(v -> showRoleSelectionDialog());
        binding.guestNote.setVisibility(session.isGuest() ? View.VISIBLE : View.GONE);

        Role role = session.getRole();
        if (role == Role.PARENT || role == Role.TEACHER) {
            binding.btnAssessment.setVisibility(View.GONE);
        } else {
            binding.btnAssessment.setVisibility(View.VISIBLE);
            binding.btnAssessment.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AssessmentActivity.class)));
        }

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
        setupThemeSection();
        setupPracticeControls();

        binding.btnShare.setOnClickListener(v -> shareApp());
        binding.btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.action_logout)
                    .setMessage(R.string.home_menu_logout_confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                        session.signOut();
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                        LoginActivity.launchFresh(requireActivity());
                    })
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && session != null) {
            setAvatar();
            binding.name.setText(session.getName());
            binding.role.setText(session.getRole().labelRes);
            binding.textLinkedCount.setText(getString(
                    R.string.link_code_linked_count, session.getLinkedCount()));
        }
    }

    private void showAvatarSelectionDialog() {
        String[] avatars = new String[]{"🐼", "🐯", "🐰", "🐶", "🐱", "🐸"};
        int currentSelection = 0;
        String currentAvatar = session.getAvatar();
        for (int i = 0; i < avatars.length; i++) {
            if (avatars[i].equals(currentAvatar)) {
                currentSelection = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.label_avatar)
                .setSingleChoiceItems(avatars, currentSelection, (dialog, which) -> {
                    String selectedAvatar = avatars[which];
                    dialog.dismiss();
                    if (!selectedAvatar.equals(session.getAvatar())) {
                        com.dyslexiasupport.data.model.User updatedUser = new com.dyslexiasupport.data.model.User(
                                session.getName().isEmpty() ? getString(R.string.guest_name) : session.getName(),
                                session.getEmail(),
                                session.getRole(),
                                selectedAvatar,
                                session.isGuest());
                        session.signIn(updatedUser);
                        com.dyslexiasupport.data.firebase.LinkCodeManager.ensureUserLinkCode(requireContext(), updatedUser);
                        binding.avatar.setText(selectedAvatar);
                        Toast.makeText(requireContext(), R.string.profile_role_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setAvatar() {
        String avatar = session.getAvatar();
        binding.avatar.setText(avatar);
        binding.avatar.setContentDescription(getString(R.string.profile_avatar_content_description));
    }

    private void setupThemeSection() {
        boolean dark = (requireContext().getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        binding.switchTheme.setChecked(dark);
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppCompatDelegate.setDefaultNightMode(isChecked
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO));
    }

    private void shareApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        startActivity(Intent.createChooser(send, getString(R.string.action_share)));
    }

    private void showRoleSelectionDialog() {
        com.dyslexiasupport.data.model.Role[] roles = com.dyslexiasupport.data.model.Role.values();
        String[] items = new String[roles.length];
        int currentSelection = 0;
        for (int i = 0; i < roles.length; i++) {
            items[i] = getString(roles[i].labelRes);
            if (roles[i] == session.getRole()) {
                currentSelection = i;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_change_role_title)
                .setSingleChoiceItems(items, currentSelection, (dialog, which) -> {
                    com.dyslexiasupport.data.model.Role selectedRole = roles[which];
                    dialog.dismiss();
                    if (selectedRole != session.getRole()) {
                        com.dyslexiasupport.data.model.User updatedUser = new com.dyslexiasupport.data.model.User(
                                session.getName().isEmpty() ? getString(R.string.guest_name) : session.getName(),
                                session.getEmail(),
                                selectedRole,
                                session.getAvatar(),
                                session.isGuest());
                        session.signIn(updatedUser);
                        com.dyslexiasupport.data.firebase.LinkCodeManager.ensureUserLinkCode(requireContext(), updatedUser);
                        binding.role.setText(selectedRole.labelRes);
                        setupLinkCodeSection();
                        Toast.makeText(requireContext(), R.string.profile_role_updated, Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().recreate();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setupLinkCodeSection() {
        com.dyslexiasupport.data.model.Role role = session.getRole();
        if (role == com.dyslexiasupport.data.model.Role.PARENT || role == com.dyslexiasupport.data.model.Role.TEACHER) {
            binding.layoutLearnerCode.setVisibility(View.GONE);
            binding.layoutParentLink.setVisibility(View.VISIBLE);

            if (role == com.dyslexiasupport.data.model.Role.PARENT) {
                binding.btnViewChildProgress.setVisibility(View.VISIBLE);
                binding.layoutTeacherActions.setVisibility(View.GONE);
                binding.btnViewChildProgress.setOnClickListener(v -> {
                    startActivity(new Intent(requireContext(), ParentDashboardActivity.class));
                });
            } else {
                binding.btnViewChildProgress.setVisibility(View.GONE);
                binding.layoutTeacherActions.setVisibility(View.VISIBLE);
                binding.btnViewChildProgress.setVisibility(View.VISIBLE);
                binding.btnViewChildProgress.setText(R.string.role_teacher_dashboard);
                binding.btnViewChildProgress.setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), ParentDashboardActivity.class)));

                binding.btnClassReportPdf.setOnClickListener(v -> {
                    exportClassReport();
                });

                binding.btnAssignNctb.setOnClickListener(v -> {
                    showNctbAssignmentDialog();
                });
            }

            binding.textLinkedCount.setText(getString(R.string.link_code_linked_count, session.getLinkedCount()));

            binding.btnLinkStudent.setOnClickListener(v -> {
                binding.inputLayoutLinkCode.setError(null);
                String code = binding.editLinkCode.getText() != null ? binding.editLinkCode.getText().toString() : "";
                if (code.trim().isEmpty()) {
                    binding.inputLayoutLinkCode.setError(getString(R.string.link_code_error_invalid));
                    return;
                }

                com.dyslexiasupport.data.firebase.LinkCodeManager.linkStudentByCode(
                        requireContext(), code,
                        new com.dyslexiasupport.data.firebase.LinkCodeManager.OnLinkListener() {
                            @Override
                            public void onSuccess(@NonNull String studentName) {
                                if (binding == null) return;
                                android.widget.Toast.makeText(requireContext(),
                                        getString(R.string.link_code_success) + " (" + studentName + ")",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                binding.editLinkCode.setText("");
                                binding.textLinkedCount.setText(getString(
                                        R.string.link_code_linked_count, session.getLinkedCount()));
                            }

                            @Override
                            public void onError(@NonNull String errorMsg) {
                                if (binding != null) binding.inputLayoutLinkCode.setError(errorMsg);
                            }
                        });
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

    private void setupPracticeControls() {
        binding.editDailyGoal.setText(String.valueOf(session.getDailyGoal()));
        binding.dailyGoalModeGroup.check(session.isGoalInExercises()
                ? R.id.chipGoalExercises : R.id.chipGoalMinutes);
        binding.switchAssistant.setChecked(session.isAssistantEnabled());
        binding.switchReadingOnly.setChecked(session.isReadingOnly());
        binding.editDailyGoal.setOnFocusChangeListener((v, focused) -> {
            if (!focused) saveDailyGoal();
        });
        binding.editDailyGoal.setOnEditorActionListener((v, actionId, event) -> {
            saveDailyGoal();
            return false;
        });
        binding.dailyGoalModeGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) session.setGoalInExercises(ids.get(0) == R.id.chipGoalExercises);
        });
        binding.switchAssistant.setOnCheckedChangeListener((button, checked) ->
                session.setAssistantEnabled(checked));
        binding.switchReadingOnly.setOnCheckedChangeListener((button, checked) ->
                session.setReadingOnly(checked));
    }

    private void saveDailyGoal() {
        try {
            String value = binding.editDailyGoal.getText() == null
                    ? "" : binding.editDailyGoal.getText().toString().trim();
            if (!value.isEmpty()) {
                session.setDailyGoal(Integer.parseInt(value));
                binding.inputLayoutDailyGoal.setError(null);
                android.widget.Toast.makeText(requireContext(), R.string.profile_goal_saved,
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            binding.inputLayoutDailyGoal.setError(getString(R.string.link_code_error_invalid));
        }
    }

    private void showNctbAssignmentDialog() {
        android.content.Context context = requireContext();
        android.app.Activity activity = requireActivity();
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<com.dyslexiasupport.data.library.ReadingText> nctb =
                    new com.dyslexiasupport.data.library.LibraryRepository(context).loadAll();
            nctb.removeIf(text -> !text.isNctb());
            activity.runOnUiThread(() -> {
                if (!isAdded()) return;
                if (nctb.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.parent_dashboard_empty,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] labels = new String[nctb.size()];
                for (int i = 0; i < nctb.size(); i++) labels[i] = nctb.get(i).title;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.btn_assign_nctb)
                        .setItems(labels, (dialog, which) -> assignNctb(nctb.get(which)))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            });
        });
    }

    private void assignNctb(@NonNull com.dyslexiasupport.data.library.ReadingText text) {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        com.google.firebase.firestore.FirebaseFirestore db =
                com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot -> {
            java.util.List<String> students = (java.util.List<String>) snapshot.get("linkedStudents");
            if (students == null || students.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), R.string.parent_dashboard_empty,
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.Map<String, Object> assignment = new java.util.HashMap<>();
            assignment.put("assignedTextId", text.id);
            assignment.put("assignedTextTitle", text.title);
            assignment.put("assignedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            com.google.android.gms.tasks.Task<?>[] writes = new com.google.android.gms.tasks.Task<?>[students.size()];
            for (int i = 0; i < students.size(); i++) {
                writes[i] = db.collection("users").document(students.get(i))
                        .set(assignment, com.google.firebase.firestore.SetOptions.merge());
            }
            com.google.android.gms.tasks.Tasks.whenAll(writes).addOnSuccessListener(v ->
                    android.widget.Toast.makeText(requireContext(), R.string.nctb_assigned,
                            android.widget.Toast.LENGTH_SHORT).show());
        });
    }

    private void exportClassReport() {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        com.google.firebase.firestore.FirebaseFirestore db =
                com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot -> {
            java.util.List<String> students = (java.util.List<String>) snapshot.get("linkedStudents");
            if (students == null || students.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), R.string.parent_dashboard_empty,
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.List<com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>> tasks =
                    new java.util.ArrayList<>();
            for (String student : students) {
                tasks.add(db.collection("users").document(student).collection("reading_sessions").get());
                tasks.add(db.collection("users").document(student).collection("exercise_results").get());
            }
            com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks).addOnSuccessListener(values -> {
                java.util.List<com.dyslexiasupport.data.local.entity.ReadingSessionEntity> sessions =
                        new java.util.ArrayList<>();
                java.util.List<com.dyslexiasupport.data.local.entity.ExerciseResultEntity> results =
                        new java.util.ArrayList<>();
                int words = 0;
                for (int i = 0; i < values.size(); i += 2) {
                    com.google.firebase.firestore.QuerySnapshot reading =
                            (com.google.firebase.firestore.QuerySnapshot) values.get(i);
                    com.google.firebase.firestore.QuerySnapshot exercise =
                            (com.google.firebase.firestore.QuerySnapshot) values.get(i + 1);
                    for (com.google.firebase.firestore.DocumentSnapshot doc : reading) {
                        sessions.add(new com.dyslexiasupport.data.local.entity.ReadingSessionEntity(
                                doc.getString("textId") == null ? "class" : doc.getString("textId"),
                                doc.getString("textTitle"),
                                doc.getLong("durationSeconds") == null ? 0 : doc.getLong("durationSeconds"),
                                doc.getLong("timestamp") == null ? 0 : doc.getLong("timestamp")));
                    }
                    for (com.google.firebase.firestore.DocumentSnapshot doc : exercise) {
                        results.add(new com.dyslexiasupport.data.local.entity.ExerciseResultEntity(
                                doc.getString("exerciseType") == null ? "practice" : doc.getString("exerciseType"),
                                doc.getLong("score") == null ? 0 : doc.getLong("score").intValue(),
                                doc.getLong("totalQuestions") == null ? 0 : doc.getLong("totalQuestions").intValue(),
                                doc.getLong("timestamp") == null ? 0 : doc.getLong("timestamp")));
                    }
                }
                java.io.File pdf = com.dyslexiasupport.ui.progress.PdfReportExporter.generateReport(
                        requireContext(), sessions, results, words);
                if (pdf != null) com.dyslexiasupport.ui.progress.PdfReportExporter.shareReport(
                        requireContext(), pdf);
            });
        });
    }

    private void setupCloudSyncSection() {
        binding.btnSyncNow.setOnClickListener(v -> {
            if (session.isGuest() || com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser() == null) {
                Toast.makeText(requireContext(), R.string.profile_guest_note,
                        Toast.LENGTH_SHORT).show();
                return;
            }
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
