package com.dyslexiasupport.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.data.local.entity.ExerciseResultEntity;
import com.dyslexiasupport.data.local.entity.ReadingSessionEntity;
import com.dyslexiasupport.data.local.entity.WordEntity;
import com.dyslexiasupport.data.model.Role;
import com.dyslexiasupport.data.repository.AppRepository;
import com.dyslexiasupport.databinding.FragmentHomeBinding;
import com.dyslexiasupport.ui.auth.LoginActivity;
import com.dyslexiasupport.ui.profile.ParentDashboardActivity;
import com.dyslexiasupport.ui.library.LibraryItem;
import com.dyslexiasupport.ui.library.LibraryViewModel;
import com.dyslexiasupport.ui.reader.ReaderFragment;

import java.util.ArrayList;
import java.util.List;

/** Home tab: greeting + four big entry cards + share. */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AppRepository repository;
    private List<ReadingSessionEntity> sessions = new ArrayList<>();
    private List<ExerciseResultEntity> results = new ArrayList<>();
    private List<WordEntity> words = new ArrayList<>();
    private LibraryViewModel libraryViewModel;
    @Nullable private com.dyslexiasupport.data.library.ReadingText continueText;

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
        repository = new AppRepository(requireContext());
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        configureRoleHome(session.getRole());
        setupHomeMenu(session);
        binding.greeting.setText(
                getString(R.string.home_greeting_name, session.getName())
        );
        observeLearningData();
        observeContinueLearning();
        binding.btnPlanAction.setOnClickListener(v -> openPlanAction());
        binding.planCard.setOnClickListener(v -> openPlanAction());
        binding.continueLearningSeeAll.setOnClickListener(v -> tabs().selectTab(R.id.nav_read));
        binding.badgesSeeAll.setOnClickListener(v -> showAllBadges());
        binding.cardVoice.setOnClickListener(v -> openTalk());

        binding.cardRead.setOnClickListener(v ->
                tabs().selectTab(R.id.nav_read));

        binding.cardPractice.setOnClickListener(v ->
                tabs().selectTab(R.id.nav_practice));

        binding.cardWords.setOnClickListener(v ->
                startActivity(new Intent(
                        requireContext(),
                        com.dyslexiasupport.ui.vocabulary.VocabularyActivity.class
                )));

        binding.cardScan.setOnClickListener(v ->
                startActivity(new Intent(
                        requireContext(),
                        com.dyslexiasupport.ui.scan.ScanActivity.class
                )));

        binding.cardVoice.setOnClickListener(v ->
                startActivity(new Intent(
                        requireContext(),
                        com.dyslexiasupport.ui.voice.VoiceActivity.class
                )));

        binding.cardWrite.setOnClickListener(v ->
                startActivity(new Intent(
                        requireContext(),
                        com.dyslexiasupport.ui.write.WriteActivity.class
                )));

        binding.cardAsk.setOnClickListener(v -> {
            if (new SessionManager(requireContext()).isAssistantEnabled()) {
                com.dyslexiasupport.ui.assistant.AssistantActivity.launch(
                        requireContext(), "", null);
            } else {
                android.widget.Toast.makeText(requireContext(),
                        R.string.profile_assistant_disabled, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void observeContinueLearning() {
        libraryViewModel.getVisible().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty() || binding == null) return;
            LibraryItem candidate = null;
            for (LibraryItem item : items) {
                if (!item.read) {
                    candidate = item;
                    break;
                }
            }
            if (candidate == null) candidate = items.get(0);
            continueText = candidate.text;
            binding.continueLearningEmoji.setText(candidate.text.emoji);
            binding.planTitle.setText(candidate.text.title);
            String language = getString(candidate.text.isBangla()
                    ? R.string.lang_bn_label : R.string.lang_en_label);
            binding.planSubtitle.setVisibility(View.VISIBLE);
            binding.planSubtitle.setText(getString(R.string.home_continue_topic_meta,
                    language, candidate.text.timeFormatted(requireContext())));
        });
    }

    private void setupHomeMenu(@NonNull SessionManager session) {
                binding.btnHomeMenu.setOnClickListener(anchor -> {
                    androidx.appcompat.widget.PopupMenu popup =
                            new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);
                    popup.inflate(R.menu.home_menu);

                    boolean hasDashboard = session.getRole() == Role.PARENT
                            || session.getRole() == Role.TEACHER;
                    popup.getMenu().findItem(R.id.home_menu_dashboard).setVisible(hasDashboard);
                    boolean hasAssessments = session.getRole() == Role.TEACHER
                            || session.getRole() == Role.CHILD;
                    popup.getMenu().findItem(R.id.home_menu_assessments).setVisible(hasAssessments);
                    if (hasDashboard) {
                        popup.getMenu().findItem(R.id.home_menu_dashboard).setTitle(
                                session.getRole() == Role.TEACHER
                                        ? R.string.role_teacher_dashboard
                                        : R.string.role_parent_dashboard);
                    }

                    popup.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.home_menu_progress) {
                            tabs().selectTab(R.id.nav_progress);
                            return true;
                        }
                        if (id == R.id.home_menu_profile) {
                            tabs().selectTab(R.id.nav_profile);
                            return true;
                        }
                        if (id == R.id.home_menu_dashboard) {
                            startActivity(new Intent(requireContext(), ParentDashboardActivity.class));
                            return true;
                        }
                        if (id == R.id.home_menu_assessments) {
                            startActivity(new Intent(requireContext(),
                                    com.dyslexiasupport.ui.profile.AssessmentHubActivity.class));
                            return true;
                        }
                        if (id == R.id.home_menu_share) {
                            shareApp();
                            return true;
                        }
                        if (id == R.id.home_menu_logout) {
                            confirmLogout(session);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }

    private void shareApp() {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                startActivity(Intent.createChooser(share, getString(R.string.action_share)));
            }

    private void confirmLogout(@NonNull SessionManager session) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.action_logout)
                        .setMessage(R.string.home_menu_logout_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                            session.signOut();
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                            LoginActivity.launchFresh(requireActivity());
                        })
                        .show();
    }

    private void showAllBadges() {
        String[] badges = badgeLabels();
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.home_badges)
                .setItems(badges, null)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String[] badgeLabels() {
        String[] names = {
                getString(R.string.badge_first_lesson),
                getString(R.string.badge_streak),
                getString(R.string.badge_math_star),
                getString(R.string.badge_curious_mind),
                getString(R.string.badge_bookworm),
                getString(R.string.badge_focus),
                getString(R.string.badge_rising_star),
                getString(R.string.badge_goal_keeper)
        };
        boolean[] earned = badgeStatus();
        for (int i = 0; i < names.length; i++) {
            names[i] = getString(earned[i] ? R.string.badge_earned : R.string.badge_locked,
                    names[i]);
        }
        return names;
    }

    private boolean[] badgeStatus() {
        boolean hasActivity = !sessions.isEmpty() || !results.isEmpty();
        int practiceCount = results.size();
        int wordCount = words.size();
        int readingCount = sessions.size();
        int streak = LearningPlan.currentStreak(
                requireContext(), sessions, results, System.currentTimeMillis());
        boolean mathStar = false;
        for (ExerciseResultEntity result : results) {
            String type = result.exerciseType == null ? "" : result.exerciseType.toLowerCase(java.util.Locale.US);
            if (type.contains("math") && result.totalQuestions > 0
                    && result.score * 100 >= result.totalQuestions * 80) {
                mathStar = true;
                break;
            }
        }
        boolean goalKeeper = dailyGoalReached();
        return new boolean[] {
                hasActivity,
                streak >= 5,
                mathStar,
                wordCount >= 5,
                readingCount >= 5,
                totalReadingSeconds() >= 30 * 60,
                practiceCount + readingCount >= 3,
                goalKeeper
        };
    }

    private long totalReadingSeconds() {
        long total = 0;
        for (ReadingSessionEntity session : sessions) {
            total += Math.max(0, session.durationSeconds);
        }
        return total;
    }

    private boolean dailyGoalReached() {
        SessionManager settings = new SessionManager(requireContext());
        java.util.Calendar day = java.util.Calendar.getInstance();
        day.set(java.util.Calendar.HOUR_OF_DAY, 0);
        day.set(java.util.Calendar.MINUTE, 0);
        day.set(java.util.Calendar.SECOND, 0);
        day.set(java.util.Calendar.MILLISECOND, 0);
        long start = day.getTimeInMillis();
        long end = start + 24 * 60 * 60 * 1000L;
        if (settings.isGoalInExercises()) {
            int count = 0;
            for (ExerciseResultEntity result : results) {
                if (result.timestamp >= start && result.timestamp < end) count++;
            }
            return count >= settings.getDailyGoal();
        }
        long seconds = 0;
        for (ReadingSessionEntity session : sessions) {
            if (session.timestamp >= start && session.timestamp < end) {
                seconds += Math.max(0, session.durationSeconds);
            }
        }
        return seconds >= settings.getDailyGoal() * 60L;
    }

    private void openTalk() {
        if (!com.dyslexiasupport.ui.voice.SpeechInputController.isAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.voice_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(requireContext(),
                com.dyslexiasupport.ui.voice.VoiceActivity.class));
    }

    private void configureRoleHome(Role role) {
        if (role == Role.CHILD) {
            return;
        }

        binding.streakCard.setVisibility(View.GONE);
        binding.planCard.setVisibility(View.GONE);
        binding.cardPractice.setVisibility(View.GONE);
        binding.cardWords.setVisibility(View.GONE);
        binding.cardScan.setVisibility(View.GONE);
        binding.cardVoice.setVisibility(View.GONE);
        binding.cardWrite.setVisibility(View.GONE);
        binding.btnPlanAction.setVisibility(View.GONE);

        if (role == Role.PARENT) {
            binding.cardRead.setVisibility(View.GONE);
        }
    }

    private void observeLearningData() {
        repository.getAllReadingSessions().observe(getViewLifecycleOwner(), value -> {
            sessions = value != null ? value : new ArrayList<>();
            updatePlan();
        });
        repository.getAllExerciseResults().observe(getViewLifecycleOwner(), value -> {
            results = value != null ? value : new ArrayList<>();
            updatePlan();
        });
        repository.getAllWords().observe(getViewLifecycleOwner(), value -> {
            words = value != null ? value : new ArrayList<>();
            updatePlan();
        });
    }

    private void updatePlan() {
        if (binding == null) {
            return;
        }
        LearningPlan plan = LearningPlan.forToday(
                requireContext(), sessions, results, words, System.currentTimeMillis());
        updateStreak();
        updateBadgePreview();
        binding.planProgress.setProgress(plan.completedCount);
        int percent = Math.round(plan.completedCount * 100f / 3f);
        binding.planSteps.setText(getString(R.string.home_plan_percent, percent));

        if (plan.completedCount == 3) {
            binding.planTitle.setText(R.string.home_plan_title_complete);
            binding.planSubtitle.setText(R.string.home_plan_sub_complete);
            binding.btnPlanAction.setText(R.string.home_plan_continue);
            return;
        }

        switch (plan.nextAction) {
            case PRACTICE:
                binding.planTitle.setText(R.string.home_plan_sub_practice);
                binding.planSubtitle.setText(R.string.home_plan_sub_practice);
                break;
            case WORDS:
                binding.planTitle.setText(R.string.home_plan_sub_words);
                binding.planSubtitle.setText(R.string.home_plan_sub_words);
                break;
            case READ:
            default:
                if (continueText != null) {
                    binding.planTitle.setText(continueText.title);
                    String language = getString(continueText.isBangla()
                            ? R.string.lang_bn_label : R.string.lang_en_label);
                    binding.planSubtitle.setVisibility(View.VISIBLE);
                    binding.planSubtitle.setText(getString(R.string.home_continue_topic_meta,
                            language, continueText.timeFormatted(requireContext())));
                } else {
                    binding.planTitle.setText(R.string.home_plan_sub_read);
                    binding.planSubtitle.setVisibility(View.GONE);
                }
                break;
        }
        binding.btnPlanAction.setText(R.string.home_plan_next);
    }

    private void updateBadgePreview() {
        boolean[] earned = badgeStatus();
        int[] tiles = {
                R.id.badgeFirstLesson, R.id.badgeStreak, R.id.badgeMathStar,
                R.id.badgeCuriousMind, R.id.badgeBookworm, R.id.badgeFocus,
                R.id.badgeRisingStar, R.id.badgeGoalKeeper
        };
        for (int i = 0; i < tiles.length; i++) {
            View tile = binding.getRoot().findViewById(tiles[i]);
            tile.setAlpha(earned[i] ? 1f : 0.48f);
            tile.setOnClickListener(v -> showAllBadges());
        }
    }

    private void updateStreak() {
        int streak = LearningPlan.currentStreak(
                requireContext(), sessions, results, System.currentTimeMillis());
        binding.streakTitle.setText(String.valueOf(streak));

        boolean[] week = LearningPlan.activityDaysThisWeek(
                requireContext(), sessions, results, System.currentTimeMillis());
        TextView[] dots = {
                binding.streakSunDot, binding.streakMonDot, binding.streakTueDot,
                binding.streakWedDot, binding.streakThuDot, binding.streakFriDot,
                binding.streakSatDot
        };
        java.util.Calendar today = java.util.Calendar.getInstance();
        int todayIndex = today.get(java.util.Calendar.DAY_OF_WEEK) - 1;
        for (int index = 0; index < dots.length; index++) {
            boolean isToday = index == todayIndex;
            boolean marked = week[index];
            dots[index].setText(isToday
                    ? String.valueOf(today.get(java.util.Calendar.DAY_OF_MONTH))
                    : marked ? "✓" : "");
            dots[index].setBackgroundResource(
                    marked ? R.drawable.bg_streak_done
                            : isToday ? R.drawable.bg_streak_current : R.drawable.bg_streak_empty);
            dots[index].setTextColor(marked || isToday
                    ? android.graphics.Color.WHITE : getResources().getColor(R.color.ds_ink_soft));
        }
    }

    private void openPlanAction() {
        LearningPlan plan = LearningPlan.forToday(
                requireContext(), sessions, results, words, System.currentTimeMillis());
        if (plan.nextAction == LearningPlan.Action.PRACTICE) {
            tabs().selectTab(R.id.nav_practice);
        } else if (plan.nextAction == LearningPlan.Action.WORDS) {
            LearningPlan.markWordsReviewed(requireContext(), System.currentTimeMillis());
            startActivity(new Intent(requireContext(),
                    com.dyslexiasupport.ui.vocabulary.VocabularyActivity.class));
        } else {
            if (continueText != null) {
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragmentContainer, ReaderFragment.newInstance(continueText.id))
                        .addToBackStack("reader")
                        .commit();
            } else {
                tabs().selectTab(R.id.nav_read);
            }
        }
    }
    private TabHost tabs() {
        return (TabHost) requireActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // fragments outlive their views
    }
}
