package com.protel.network;

import com.protel.network.interfaces.ICacheStorage;
import com.protel.network.operators.INetworkOperator;
import com.protel.network.util.MemoryCacheHolder;

/**
 * Created by mesutbeyaztas on 04/02/16.
 */
public class MemoryCacheStorage implements ICacheStorage {

    @Override
    public void save(Request request, Response response) {
        MemoryCacheHolder.putResponse(getPath(request), response.data, response.resultMillis);
    }

    @Override
    public Object getCache(Request request) {
        return MemoryCacheHolder.getResponse((getPath(request)));
    }

    @Override
    public Object getCache(String cacheName) {
        return MemoryCacheHolder.getResponse((cacheName));
    }

    @Override
    public void updateCacheDate(Request request, long resultMillis) {

    }

    @Override
    public Long getResultMillis(Request request) {
        return MemoryCacheHolder.getResultMillis(getPath(request));
    }

    @Override
    public Long getResultMillis(String cacheName) {
        return MemoryCacheHolder.getResultMillis(cacheName);
    }

    private String getPath(Request request) {
        return INetworkOperator.getCacheFileName(request);
    }

}
