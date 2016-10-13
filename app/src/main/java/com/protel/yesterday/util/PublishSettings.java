package com.protel.yesterday.util;

import com.protel.yesterday.BuildConfig;

/**
 * Created by eolkun on 10.12.2014.
 */
public class PublishSettings {

    public static boolean IS_DEVELOPER = BuildConfig.DEBUG;
    private static ReleaseTypes releaseType = ReleaseTypes.Alpha;


    public static ReleaseTypes getReleaseType() {
        return PublishSettings.releaseType;
    }

    public static void setReleaseType(ReleaseTypes releaseType) {
        PublishSettings.releaseType = releaseType;
    }

    public static boolean isAlphaAndDeveloper() {
        return releaseType == ReleaseTypes.Alpha && IS_DEVELOPER;
    }

    public static boolean isLive() {
        return releaseType == ReleaseTypes.Live && !IS_DEVELOPER;
    }

    public enum ReleaseTypes {
        Alpha, Beta, Live
    }
}
