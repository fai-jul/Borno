package com.dyslexiasupport.ui.home;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;

/** What fragments can ask of the main shell. */
public interface TabHost {

    void selectTab(@IdRes int tabId);

    /**
     * Paints the area behind the system bars and picks light/dark status-bar icons.
     * The shell resets this to the normal cream look whenever the tab changes.
     */
    void setScreenTheme(@ColorInt int backgroundColor, boolean darkBackground);
}
