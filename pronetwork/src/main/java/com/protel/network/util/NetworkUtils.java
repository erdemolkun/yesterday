package com.protel.network.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.protel.network.ProNetwork;

/**
 * Created by eolkun on 25.12.2014.
 */
public class NetworkUtils {
    public static boolean isConnected() {
        Context context = ProNetwork.getSingleton().getContext();
        if (context == null) {
            return false;
        }
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
