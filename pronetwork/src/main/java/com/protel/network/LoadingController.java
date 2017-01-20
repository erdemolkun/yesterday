package com.protel.network;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import com.protel.network.interfaces.ResponseListener;
import com.protel.network.interfaces.UILoadingManager;
import com.protel.network.util.LogUtils;


/**
 * Created by erdemmac on 12/08/15.
 */
public class LoadingController {
    private static final int MIN_LOADING_DURATION = 300;
    private Dialog loadingDialog;
    private Long loadingStartTime = 0L;
    private Handler handler;
    private Runnable dismissRunnable;
    private CancelCallback cancelCallback;

    public LoadingController(CancelCallback cancelCallback) {
        handler = new Handler(Looper.getMainLooper());
        this.cancelCallback = cancelCallback;
    }

    private Dialog getLoadingDialog(ResponseListener responseListener, int requestId, String loadingDialogTitle) {
        Dialog dialog;
        Context context = responseListener != null ? responseListener.getContext() : null;
        if (context == null) return null;
        if (responseListener instanceof UILoadingManager) {
            dialog = ((UILoadingManager) responseListener).getLoading(context, requestId, loadingDialogTitle);
        } else {
            dialog = ProNetwork.getSingleton().getDialog(context, requestId, loadingDialogTitle);
        }
        return dialog;
    }

    public void showLoading(ResponseListener responseListener, Request request) {
        dismissLoading(true);
        if (responseListener == null) {
            return;
        }
        Dialog tempDialog = getLoadingDialog(responseListener, request.getID(), request.getLoadingDialogTitle());
        if (tempDialog == null) {
            return;
        }
        loadingDialog = tempDialog;
        loadingDialog.setCancelable(request.isCancelable() != null && request.isCancelable());
        loadingDialog.setCanceledOnTouchOutside(false);
        try {
            if (dismissRunnable != null) {
                handler.removeCallbacks(dismissRunnable);
            }
            loadingDialog.show();
            if (request.isCancelable() != null && request.isCancelable()) {
                loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (cancelCallback != null) {
                            cancelCallback.onCanceled();
                        }
                    }
                });
            }
            loadingStartTime = System.currentTimeMillis();
        } catch (WindowManager.BadTokenException e) {
            LogUtils.ex(e);
        }
    }

    public void dismissLoading(boolean isImmediate) {
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
        }
        long diff = System.currentTimeMillis() - loadingStartTime;
        if (diff < MIN_LOADING_DURATION & !isImmediate) {

            dismissRunnable = new Runnable() {
                @Override
                public void run() {
                    dismissReal();
                }
            };
            handler.postDelayed(dismissRunnable, MIN_LOADING_DURATION - diff);
        } else {
            dismissReal();
        }
    }

    private void dismissReal() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.dismiss();
            } catch (Exception e) {
                LogUtils.ex(e);
            }
            loadingDialog = null;
        }
    }

    public interface CancelCallback {
        void onCanceled();
    }
}
