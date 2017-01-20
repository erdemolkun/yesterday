package com.protel.network;

import android.content.Context;

import com.protel.network.operators.INetworkOperator;

/**
 * Created by erdemmac on 02/02/16.
 */
public class CachePathProviderWExtension extends ICachePathProvider {
    @Override
    public String getCacheFilePath(Context context, Request request) {
        String rootPath = context.getCacheDir().getAbsolutePath();
        return rootPath + "/" + INetworkOperator.getCacheFileName(request) + ".cache";
    }

    @Override
    public String getCacheFilePath(Context context, String name) {
        String rootPath = context.getCacheDir().getAbsolutePath();
        return rootPath + "/" + name + ".cache";
    }
}
