package com.protel.network;

import com.protel.network.interfaces.ICacheStorage;
import com.protel.network.util.LogUtils;
import com.protel.network.util.SerializeUtils;

import java.io.File;

/**
 * Created by erdemmac on 02/06/15.
 */
public class DiskCacheStorage implements ICacheStorage {
    @Override
    public void save(Request request, Response response) {
        refreshCache(response.data, request);
    }

    @Override
    public Object getCache(Request request) {
        return SerializeUtils.readCacheResponse(getPath(request));
    }

    @Override
    public Object getCache(String cacheName) {
        return SerializeUtils.readCacheResponse(getPath(cacheName));
    }

    @Override
    public void updateCacheDate(Request request, long modifiedDate) {
        try {
            File file = new File(getPath(request));
            boolean isUpdated = file.setLastModified(modifiedDate);
        } catch (Exception ex) {
            LogUtils.ex(ex);
        }
    }

    private void refreshCache(Object result, Request request) {
        String filePath = getPath(request);
        if (result == null) {
            try {
                File file = new File(filePath);
                //if(file.exists()) {
                file.delete();
                //}
                return;
            } catch (Exception ex) {
                LogUtils.ex(ex);
            }
        }
        SerializeUtils.writeToFile(result, filePath);
    }

    @Override
    public Long getResultMillis(Request request) {
        File file = new File(getPath(request));
        return file.lastModified();
    }

    @Override
    public Long getResultMillis(String cacheName) {
        File file = new File(cacheName);
        return file.lastModified();
    }

    private String getPath(Request request) {
        ProNetwork proNetwork = ProNetwork.getSingleton();
        return proNetwork.getDiskCachePathProvider().getCacheFilePath(proNetwork.getContext(), request);
    }

    private String getPath(String cacheName) {
        ProNetwork proNetwork = ProNetwork.getSingleton();
        return proNetwork.getDiskCachePathProvider().getCacheFilePath(proNetwork.getContext(), cacheName);
    }

}
