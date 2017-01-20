package com.protel.network.interfaces;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by eolkun on 27.1.2015.
 */
public interface UILoadingManager {
    Dialog getLoading(Context context, int requestId, String loadingDialogTitle);
}
