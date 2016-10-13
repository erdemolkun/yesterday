package com.protel.yesterday.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * Created by erdemmac on 09/09/15.
 */
public class LocalizationManager {
    public static final int LANG_TR = 0;
    public static final int LANG_EN = 1;

    private static final String LANG_CODE_TR = "tr";
    private static final String LANG_CODE_EN = "en";
    @LanguageCode
    private static int currentLang = LANG_EN;

    static {
        refresh();
    }

    public static void refresh() {
        String prefLocale = null;//Prefs.getLanguage();
        String localeCode;
        if (prefLocale == null) {
            localeCode = Locale.getDefault().getLanguage();
        } else {
            localeCode = prefLocale;
        }

        if (localeCode.equals(LANG_CODE_EN)) {
            currentLang = LANG_EN;
        } else if (localeCode.equals(LANG_CODE_TR)) {
            currentLang = LANG_TR;
        } else {
            currentLang = LANG_EN;
        }
    }


    public static String getCurrentLangCode() {
        if (currentLang == LANG_EN) {
            return LANG_CODE_EN;
        }
        return LANG_CODE_TR;
    }

    public static String getLangCode(int lang) {
        if (lang == LANG_TR) {
            return LANG_CODE_TR;
        }
        return LANG_CODE_EN;
    }

    @LanguageCode
    public static int getCurrentLang() {
        return currentLang;
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LANG_TR, LANG_EN})

    public @interface LanguageCode {
    }
}
