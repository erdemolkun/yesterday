package com.protel.yesterday.util;

import android.util.Log;


/**
 * Created by eolkun on 10.12.2014.
 */
public class L {


    private static final String LOG_PREFIX = "YESTERDAY ";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }


    public static void d(String tag, String msg) {
        if (PublishSettings.IS_DEVELOPER)
            Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (PublishSettings.IS_DEVELOPER)
            Log.v(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (PublishSettings.IS_DEVELOPER)
            Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (PublishSettings.IS_DEVELOPER)
            Log.i(tag, msg);
    }

    public static void wtf(String tag, String msg) {
        if (PublishSettings.IS_DEVELOPER)
            Log.wtf(tag, msg);
    }

    public static void ex(Exception ex) {
        ex(ex, false);
    }

    public static void ex(Exception ex, boolean send) {
        if (PublishSettings.IS_DEVELOPER && send) {
            // TODO add hockeyapp..
            //ExceptionHandler.saveException(new MyCaughtException(ex), null);
            ex.printStackTrace();
        }
    }
}
