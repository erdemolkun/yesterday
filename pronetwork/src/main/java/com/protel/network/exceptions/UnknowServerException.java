package com.protel.network.exceptions;

/**
 * Created by eolkun on 6.2.2015.
 */
public class UnknowServerException extends Exception {
    private int errorCode;

    public UnknowServerException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
