package com.protel.network.exceptions;

/**
 * Created by eolkun on 2.2.2015.
 */
public class NoNetworkException extends Exception {
    public NoNetworkException() {
        super("Network connection not available!");
    }
}
