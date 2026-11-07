package com.dyslexiasupport;

import android.app.Application;


public class DyslexiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        com.dyslexiasupport.system.DailyReminderScheduler.scheduleDailyReminder(this);
    }
}
