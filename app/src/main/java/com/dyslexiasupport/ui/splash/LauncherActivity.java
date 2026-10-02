package com.dyslexiasupport.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.dyslexiasupport.data.local.SessionManager;
import com.dyslexiasupport.ui.auth.LoginActivity;
import com.dyslexiasupport.ui.home.MainActivity;

/** Only LAUNCHER activity: shows the splash, then routes by login state. */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this); // before super.onCreate()
        super.onCreate(savedInstanceState);

        boolean signedIn = new SessionManager(this).isSignedIn();
        Class<?> next = signedIn ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, next));
        finish();
    }
}
