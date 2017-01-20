package com.protel.network.interfaces;

import com.protel.network.Request;
import com.protel.network.Response;

/**
 * Created by erdemmac on 02/06/15.
 */
public interface ICacheStorage {
    void save(Request request, Response response);

    Object getCache(Request request);

    Object getCache(String cacheName);

    void updateCacheDate(Request request, long modifiedDate);

    Long getResultMillis(Request request);

    Long getResultMillis(String cacheName);
}
