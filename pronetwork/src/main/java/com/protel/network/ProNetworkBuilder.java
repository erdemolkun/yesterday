package com.protel.network;

import android.content.Context;

import com.protel.network.interfaces.ILogger;
import com.protel.network.interfaces.UILoadingManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by erdemmac on 21/11/2016.
 */

public class ProNetworkBuilder {
    ErrorHandler errorHandler;
    Context context;
    UrlProvider urlProvider;
    ILogger logger;
    UILoadingManager uiLoadingManager;
    Integer timeout;
    Long diskCacheExpireTimeout, memoryCacheExpireTimeout;

    public ProNetworkBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        } else {
            this.context = context.getApplicationContext();
        }
    }

    public ProNetworkBuilder errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public ProNetworkBuilder urlProvider(UrlProvider urlProvider) {
        this.urlProvider = urlProvider;
        return this;
    }

    public ProNetworkBuilder logger(ILogger logger) {
        this.logger = logger;
        return this;
    }

    public ProNetworkBuilder loading(UILoadingManager uiLoadingManager) {
        this.uiLoadingManager = uiLoadingManager;
        return this;
    }

    public ProNetworkBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @param timeout Sets the default cache expire timeout for cache file objects. Values must be
     *                between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     * @see #cacheExpireTimeout(long, TimeUnit)
     */
    public void cacheExpireTimeout(long timeout) {
        cacheExpireTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param timeout Sets the default cache expire timeout for cache file objects. Values must be
     *                between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     * @param unit    {@link TimeUnit} instance for time convertion
     * @see #cacheExpireTimeout(long)
     */
    public void cacheExpireTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(timeout);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException("Timeout too large.");
        if (millis == 0 && timeout > 0) throw new IllegalArgumentException("Timeout too small.");
        diskCacheExpireTimeout = millis;
    }


    /**
     * @param timeout Sets the default cache expire timeout for in memory objects. Values must be
     *                between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     * @see #memoryCacheExpireTimeout(long, TimeUnit)
     */
    public void memoryCacheExpireTimeout(long timeout) {
        memoryCacheExpireTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param timeout Sets the default cache expire timeout for in memory objects. Values must be
     *                between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     * @param unit    {@link TimeUnit} instance for time convertion
     * @see #memoryCacheExpireTimeout(long)
     */
    public void memoryCacheExpireTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(timeout);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException("Timeout too large.");
        if (millis == 0 && timeout > 0) throw new IllegalArgumentException("Timeout too small.");
        memoryCacheExpireTimeout = millis;
    }

    public ProNetwork build() {
        return new ProNetwork(this);
    }

}