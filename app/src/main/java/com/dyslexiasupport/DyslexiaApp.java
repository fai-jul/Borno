package com.dyslexiasupport;

import android.app.Application;

/**
 * Application class: created once, before any Activity.
 * Later phases hook in here:
 *  - Phase 11: notification channels
 *  - Phase 12: Firebase initialisation
 */
public class DyslexiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        com.dyslexiasupport.system.DailyReminderScheduler.scheduleDailyReminder(this);
    }
}
