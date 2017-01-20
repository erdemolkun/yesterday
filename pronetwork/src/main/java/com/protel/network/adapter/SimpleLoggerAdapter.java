package com.protel.network.adapter;

import com.protel.network.interfaces.ILogger;

import java.util.HashMap;

/**
 * Created by erdemmac on 23/11/2016.
 */

public class SimpleLoggerAdapter implements ILogger{
    @Override
    public void log(String tag, String content) {

    }

    @Override
    public void log(Exception ex) {

    }

    @Override
    public void logResponse(int code, String requestedURL, HashMap<String, String> headers, String body) {

    }

    @Override
    public void logErrorResponse(Exception e) {

    }

    @Override
    public void logRequest(String method, String url, HashMap<String, String> headers, String body) {

    }
}
