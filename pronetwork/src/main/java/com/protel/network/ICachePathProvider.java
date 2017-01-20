package com.protel.network;

import android.content.Context;

/**
 * Created by eolkun on 26.1.2015.
 */
public abstract class ICachePathProvider {

    public abstract String getCacheFilePath(Context context, Request request);

    public abstract String getCacheFilePath(Context context, String name);

}
