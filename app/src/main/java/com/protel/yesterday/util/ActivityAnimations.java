package com.protel.yesterday.util;

import android.app.Activity;
import android.support.annotation.IntDef;

import com.protel.yesterday.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by eolkun on 24.12.2014.
 */
public class ActivityAnimations {


    public static final int NONE = 0;
    public static final int FADE_IN = 1;
    public static final int FROM_BOTTOM = 2;
    public static final int SLIDE_LEFT_IN = 3;
    public static final int LIKE_VINE = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, FADE_IN, FROM_BOTTOM, SLIDE_LEFT_IN, LIKE_VINE})

    public @interface AnimationType {
    }

    public static void doAnimation(Activity activity, @AnimationType int animationType, boolean enter) {
        if (animationType == NONE) {
            activity.overridePendingTransition(-1, -1);
        } else if (animationType == FADE_IN) {
            doAnimFade(activity, enter);
        } else if (animationType == FROM_BOTTOM) {
            doAnimFromBottom(activity, enter);
        } else if (animationType == SLIDE_LEFT_IN) {
            doAnimSlideLeftIn(activity, enter);
        } else if (animationType == LIKE_VINE) {
            doAnimLikeVine(activity, enter);
        }

    }

    private static void doAnimLikeVine(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        } else {
            activity.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    private static void doAnimSlideLeftIn(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.activity_slide_scale_fade_left_in, R.anim.activity_slide_scale_fade_left_out);
        } else {
            activity.overridePendingTransition(R.anim.activity_slide_scale_fade_right_in, R.anim.activity_slide_scale_fade_right_out);
        }
    }

    private static void doAnimFade(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.no_animation_long);
        } else {
            activity.overridePendingTransition(R.anim.no_animation_long, R.anim.fade_out);
        }
    }

    private static void doAnimFromBottom(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.slide_bottom_in, R.anim.no_animation);
        } else {
            activity.overridePendingTransition(0, R.anim.slide_bottom_out);
        }
    }
}
