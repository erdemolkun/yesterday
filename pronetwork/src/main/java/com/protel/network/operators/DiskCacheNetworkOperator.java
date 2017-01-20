package com.protel.network.operators;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.protel.network.ProNetwork;
import com.protel.network.ProtelExecuterService;
import com.protel.network.Request;
import com.protel.network.Response;
import com.protel.network.interfaces.ICacheStorage;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by eolkun on 2.2.2015.
 */
public class DiskCacheNetworkOperator extends INetworkOperator {
    private boolean isCanceled = false;

    public DiskCacheNetworkOperator() {
        super();
    }

    public static Long getCacheTime(Request request) {
        ProNetwork proNetwork = ProNetwork.getSingleton();
        String cacheFilePath = proNetwork.getDiskCachePathProvider().getCacheFilePath(proNetwork.getContext(), request);

        File file = new File(cacheFilePath);
        if (file.exists()) {
            return file.lastModified();
        }
        return null;
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
        if (request.isCanceled())return;
        ProtelExecuterService protelExecuterService = ProtelExecuterService.get();
        protelExecuterService.submit(new Runnable() {
            @Override
            public void run() {
                Context context = ProNetwork.getSingleton().getContext();
                final ICacheStorage cacheStorage = ProNetwork.getSingleton().getDiskCacheStorage();

                final Object result = cacheStorage.getCache(request);

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
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                error(new Exception("Cache result expired"));
                            }
                        });
                        return;
                    }
                }
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            Response response = new Response(result);
                            response.resultMillis = getCacheTime(request);
                            success(response);
                        } else error(new Exception("Unknown read error"));
                    }
                });
            }
        });
    }

    @Override
    protected void success(Response response) {
        super.success(response);
        if (request.hasMemoryCacheType()) {
            ProNetwork.getSingleton().getMemoryCacheStorage().save(request, response);
        }
    }

    /**
     * Check file validation by file expire duration.
     */
    private boolean isFileValid() {
        Long diskCacheExpireTimeout = request.getDiskCacheExpireTimeout();
        if (diskCacheExpireTimeout == null) {
            diskCacheExpireTimeout = ProNetwork.getSingleton().getDiskCacheExpireTimeout();
        }
        if (diskCacheExpireTimeout == null) return true;
        Long cacheTime = getCacheTime(request);
        if (cacheTime != null) {
            Date dateNow = Calendar.getInstance().getTime();
            long diffInMilis = dateNow.getTime() - cacheTime;
            if (diffInMilis < diskCacheExpireTimeout) {
                return true;
            }
        }
        return false;
    }

}
