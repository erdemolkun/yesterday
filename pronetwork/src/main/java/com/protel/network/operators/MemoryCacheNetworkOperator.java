package com.protel.network.operators;

import android.os.SystemClock;

import com.protel.network.ProNetwork;
import com.protel.network.Request;
import com.protel.network.Response;
import com.protel.network.interfaces.ICacheStorage;

import java.util.Calendar;

/**
 * Created by mesutbeyaztas on 05.02.2016.
 */
public class MemoryCacheNetworkOperator extends INetworkOperator {
    private boolean isCanceled = false;
    private Object result;

    public MemoryCacheNetworkOperator() {
        super();
    }

    public static Long getCacheTimeMillis(Request request) {
        Long cacheTimeMillis = ProNetwork.getSingleton().getMemoryCacheStorage().getResultMillis(request);
        if (cacheTimeMillis != null) {
            return cacheTimeMillis;
        }
        return null;
    }

    public static Long getCacheTimeMillis(String key) {
        return ProNetwork.getSingleton().getMemoryCacheStorage().getResultMillis(key);
    }

    @Override
    public void cancel() {
        isCanceled = true;
    }

    @Override
    protected boolean canWriteToCache() {
        return false;
    }

    @Override
    public void start(final Request request) {
        super.start(request);
        if (request.isCanceled()) return;
        ICacheStorage cacheStorage = ProNetwork.getSingleton().getMemoryCacheStorage();

        result = cacheStorage.getCache(request);

        if (isCanceled)
            return;

        Integer mockDuration = request.getMockDelay();
        if (mockDuration != null) {
            SystemClock.sleep(mockDuration);
        }

        if (isCanceled)
            return;

        if (result != null) {
            if (!isFileValid()) {
                error(new Exception("Memory cache result expired"));
                return;
            }
        }

        if (result != null) {
            Long resultMillis = cacheStorage.getResultMillis(request);
            Response response = new Response(result);
            response.resultMillis = resultMillis;
            success(response);
        } else error(new Exception("Unknown read error"));
    }

    /**
     * Check file validation by file expire duration.
     */
    private boolean isFileValid() {
        Long cacheExpriceTimeout = request.getMemoryCacheExpireTimeout();
        if (cacheExpriceTimeout == null) {
            cacheExpriceTimeout = ProNetwork.getSingleton().getMemoryCacheExpireTimeout();
        }
        if (cacheExpriceTimeout == null) return true;
        Long cacheTime = getCacheTimeMillis(request);
        if (cacheTime != null) {
            Long dateNow = Calendar.getInstance().getTimeInMillis();
            long differentInMilis = dateNow - cacheTime;
            if (differentInMilis < cacheExpriceTimeout) {
                return true;
            }
        }
        return false;
    }

}
