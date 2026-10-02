package com.dyslexiasupport.ui.common;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Parent of every screen in the app.
 *  1. Logs the Activity lifecycle (filter Logcat by "Lifecycle").
 *  2. Turns on edge-to-edge drawing with dark icons on our light theme.
 *  3. applySystemBarInsets() keeps content clear of system bars and the keyboard.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String LIFECYCLE_TAG = "Lifecycle";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SystemBarStyle transparentLight =
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT);
        EdgeToEdge.enable(this, transparentLight, transparentLight);
        super.onCreate(savedInstanceState);
        log("onCreate (savedState=" + (savedInstanceState != null) + ")");
    }

    /** Pads all sides (and the keyboard). Call after setContentView. */
    protected void applySystemBarInsets(@NonNull View root) {
        applySystemBarInsets(root, true);
    }

    /**
     * @param applyBottom false for screens whose bottom bar (e.g. BottomNavigationView)
     *                    handles the bottom inset itself.
     */
    protected void applySystemBarInsets(@NonNull View root, boolean applyBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, applyBottom ? bars.bottom : 0);
            return applyBottom ? WindowInsetsCompat.CONSUMED : windowInsets;
        });
    }

    @Override protected void onStart()   { super.onStart();   log("onStart"); }
    @Override protected void onResume()  { super.onResume();  log("onResume"); }
    @Override protected void onPause()   { super.onPause();   log("onPause"); }
    @Override protected void onStop()    { super.onStop();    log("onStop"); }
    @Override protected void onRestart() { super.onRestart(); log("onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); log("onDestroy"); }

    private void log(String event) {
        Log.d(LIFECYCLE_TAG, getClass().getSimpleName() + ": " + event);
    }
}
