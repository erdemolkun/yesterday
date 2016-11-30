package com.protel.yesterday.service;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.protel.network.HttpStatus;
import com.protel.network.Request;
import com.protel.network.RequestController;
import com.protel.network.exceptions.UnAuthoritedException;
import com.protel.network.exceptions.UnknowServerException;
import com.protel.yesterday.R;
import com.protel.yesterday.App;
import com.protel.yesterday.util.DialogUtils;

/**
 * Created by eolkun on 9.3.2015.
 */
public class ServiceErrorHandler {

    private boolean showErrorEvenInSlientRequest = false;

    public ServiceErrorHandler() {
    }

    public ServiceErrorHandler(boolean showErrorEvenInSlientRequest) {
        this.showErrorEvenInSlientRequest = showErrorEvenInSlientRequest;
    }

    private String getString(Integer resId) {
        return App.getContext().getString(resId);
    }

    public void handle(final Request request, Exception e, final RequestController requestController, final DetailedErrorHandler errorHandlerListener) {

        Context context = request.responseListener.getContext();
        int errorCodeTemp = -1;
        String messageToPrint = null;
        boolean isRetryEnabled = true;
        if (e instanceof UnAuthoritedException || e instanceof UnknowServerException) {
            if (e instanceof UnknowServerException) {
                UnknowServerException unknowServerException = (UnknowServerException) e;
                if (unknowServerException.getErrorCode() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                    isRetryEnabled = false;
                }
            }
        }


        if (TextUtils.isEmpty(messageToPrint)) {
            messageToPrint = ServiceHelper.getServiceErrorMessage(e);
        }

        if (request.loadingIndicatorPolicy != Request.NO_LOADING || showErrorEvenInSlientRequest) {

            final int errorCode = errorCodeTemp;
            String title = ServiceHelper.getServiceErrorTitle(e);
            DialogUtils.DialogBuilder errorDialogBuilder = DialogUtils.getDialogBuilder(context);
            errorDialogBuilder.title(title);
            errorDialogBuilder.message(messageToPrint);
            errorDialogBuilder.positive(R.string.close).negative(isRetryEnabled ? getString(R.string.retry) : null);
            errorDialogBuilder.listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        requestController.resendRequest(request.getID(), request.responseListener);
                    } else {
                        if (!errorHandlerListener.canHandle(errorCode)) {
                            errorHandlerListener.errorHandled(false);
                        }
                    }
                }
            });

//                if (e instanceof NoNetworkException) {
//                    Drawable wrappedDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_no_connection));
//                    wrappedDrawable = wrappedDrawable.mutate();
//                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.quick_red));
//                    errorDialogBuilder.icon(wrappedDrawable);
//                }
            Dialog errorDialog = errorDialogBuilder.show();
            errorDialog.setCancelable(true);
            errorDialog.setCanceledOnTouchOutside(false);
            errorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!errorHandlerListener.canHandle(errorCode)) {
                        errorHandlerListener.errorHandled(false);
                    }
                }
            });
            requestController.setErrorDialog(errorDialog);
        }


    }

    public static abstract class DetailedErrorHandler {
        public boolean canHandle(int errorCode) {
            return false;
        }

        public abstract void errorHandled(boolean forceQuit);
    }
}
