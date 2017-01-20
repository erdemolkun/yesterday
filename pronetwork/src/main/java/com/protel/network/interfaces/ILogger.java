package com.protel.network.interfaces;

import java.util.HashMap;

/**
 * Created by eolkun on 26.1.2015.
 */
public interface ILogger {

    void log(String tag, String content);

    void log(Exception ex);

    void logResponse(int code, String requestedURL, HashMap<String, String> headers, String body);

    void logErrorResponse(Exception e);

    void logRequest(String method, String url, HashMap<String, String> headers, String body);

}
