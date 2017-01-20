package com.protel.network.interfaces;

import android.content.Context;

import com.protel.network.Request;
import com.protel.network.Response;

/**
 * Created by eolkun on 8.12.2014.
 */
public interface ResponseListener {

    Context getContext();

    void onResponse(Response response, Request request);

    void onErrorResponse(Request request, Exception e);

}
