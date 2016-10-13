package com.protel.yesterday.service;

import com.protel.network.exceptions.NoNetworkException;
import com.protel.yesterday.R;
import com.protel.yesterday.YesterdayApp;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Created by eolkun on 27.1.2015.
 */
public class ServiceHelper {

    public static String getServiceErrorTitle(Exception e) {
        String message = YesterdayApp.getInstance().getString(R.string.error);
        Throwable th;
        if (e.getCause() != null)
            th = e.getCause();
        else {
            th = e;
        }
        if (th instanceof NoNetworkException) {
            message = YesterdayApp.getInstance().getString(R.string.check_network_connection_title);
        }
        return message;
    }

    public static String getServiceErrorMessage(Exception e) {
        String message = YesterdayApp.getInstance().getString(R.string.unknown_service_error);
        Throwable th;
        if (e.getCause() != null)
            th = e.getCause();
        else {
            th = e;
        }
        if (th instanceof NoNetworkException) {
            message = YesterdayApp.getInstance().getString(R.string.check_network_connection);
        } else if (th instanceof SocketTimeoutException) {
            message = YesterdayApp.getInstance().getString(R.string.timeout_service);
        } else if (th instanceof ConnectException) {
            message = YesterdayApp.getInstance().getString(R.string.unknown_service_error);
        }
        return message;
    }

}
