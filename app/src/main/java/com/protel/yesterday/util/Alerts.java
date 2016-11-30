package com.protel.yesterday.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.protel.yesterday.R;


/**
 * Created by eolkun on 6.3.2015.
 */
public class Alerts {

    public static DialogUtils.DialogBuilder dialog(Context context) {
        return DialogUtils.getDialogBuilder(context).icon(R.mipmap.ic_launcher).title(R.string.app_name);
    }


    public static void showDefaultSnackbar(Activity activity, @StringRes int resource) {
        Snackbar snackbar = getSnackBar(activity, resource);
        if (snackbar != null) getSnackBar(activity, resource).show();
    }

    public static Snackbar getSnackBar(Activity activity, @StringRes int resource) {
        if (activity == null) return null;
        View view = activity.findViewById(android.R.id.content);
        if (view == null) return null;
        return Snackbar.make(view, resource, Snackbar.LENGTH_SHORT);
    }

    public static Snackbar getSnackBar(Activity activity, String text) {
        if (activity == null) return null;
        View view = activity.findViewById(android.R.id.content);
        if (view == null) return null;
        return Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
    }


    public static Dialog createLoadingDialog(Context mContext) {

        Dialog d = new Dialog(mContext, R.style.Theme_AlertDialog);
        d.setContentView(R.layout.layout_material_progress);
        d.setCancelable(false);

        ((TextView) d.findViewById(R.id.tv_loading_text)).setText(R.string.loading_weather_info);

        return d;
    }


}
