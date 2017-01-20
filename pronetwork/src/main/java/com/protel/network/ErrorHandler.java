package com.protel.network;

/**
 * Created by eolkun on 11.2.2015.
 */
public abstract class ErrorHandler {
    /**
     * Return true if you handle the error
     */
    public abstract boolean onErrorOccured(Exception ex, Request request);
}
