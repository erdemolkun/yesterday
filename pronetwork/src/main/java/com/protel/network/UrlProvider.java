package com.protel.network;

/**
 * Created by eolkun on 26.1.2015.
 */
public abstract class UrlProvider {

    /**
     * Creates a url based on the function id of service..
     */
    public abstract String getBaseUrl(int functionId, int urlType);

}
