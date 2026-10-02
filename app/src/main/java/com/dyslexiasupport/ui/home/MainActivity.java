package com.dyslexiasupport.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dyslexiasupport.R;
import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.databinding.ActivityMainBinding;
import com.dyslexiasupport.ui.auth.LoginActivity;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ui.common.ComingSoonFragment;
import com.dyslexiasupport.ui.library.LibraryFragment;
import com.dyslexiasupport.ui.profile.ProfileFragment;

/** App shell: one fragment container + bottom navigation. */
public class MainActivity extends BaseActivity implements TabHost {

    private ActivityMainBinding binding;

    /** Opens the shell and clears the back stack (used after login/register). */
    public static void launchFresh(Activity from) {
        Intent intent = new Intent(from, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safety net: never show the app without a session.
        if (!new SessionManager(this).isSignedIn()) {
            LoginActivity.launchFresh(this);
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Bottom nav pads itself for the gesture/nav bar.
        applySystemBarInsets(binding.getRoot(), false);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        // Tapping the tab you are already on goes back to that tab's first screen
        // (for Read: from an open text back to the library).
        binding.bottomNav.setOnItemReselectedListener(item ->
                getSupportFragmentManager().popBackStack());

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Back: 1) close an open text, 2) go to Home, 3) leave the app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (binding.bottomNav.getSelectedItemId() != R.id.nav_home) {
                    binding.bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public void selectTab(@IdRes int tabId) {
        binding.bottomNav.setSelectedItemId(tabId);
    }

    @Override
    public void setScreenTheme(@ColorInt int backgroundColor, boolean darkBackground) {
        if (binding == null) {
            return;
        }
        binding.getRoot().setBackgroundColor(backgroundColor);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), binding.getRoot());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!darkBackground);
        }
    }

    private void showTab(@IdRes int id) {
        // Every tab starts with the normal look; the Reader re-applies its own theme.
        setScreenTheme(ContextCompat.getColor(this, R.color.ds_cream), false);

        Fragment fragment;
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_read) {
            fragment = new LibraryFragment();
        } else if (id == R.id.nav_practice) {
            fragment = ComingSoonFragment.newInstance(R.string.nav_practice, "\uD83C\uDFAF");
        } else if (id == R.id.nav_progress) {
            fragment = ComingSoonFragment.newInstance(R.string.nav_progress, "\uD83D\uDCCA");
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else {
            return;
        }
        // Leaving a tab also forgets any text that was open inside it.
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
