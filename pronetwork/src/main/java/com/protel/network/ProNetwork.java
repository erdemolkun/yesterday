package com.protel.network;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.protel.network.interfaces.ICacheStorage;
import com.protel.network.interfaces.ILogger;
import com.protel.network.interfaces.UILoadingManager;
import com.protel.network.operators.INetworkOperator;
import com.protel.network.util.DefaultLogger;

/**
 * Created by eolkun on 26.1.2015.
 */
public class ProNetwork {

    @SuppressLint("StaticFieldLeak")
    static volatile ProNetwork singleton;
    private MockManager mockManager;
    private UILoadingManager uiLoadingManager;
    private UrlProvider urlProvider;
    private ErrorHandler errorHandler;
    private ILogger logger;
    private Context context;
    private Integer timeout;
    private ICacheStorage diskCacheStorage;
    private ICacheStorage memoryCacheStorage;
    private ICachePathProvider diskCachePathProvider;
    private Long diskCacheExpireTimeout;
    private Long memoryCacheExpireTimeout;
    private Integer memoryCacheSize;

    ProNetwork(ProNetworkBuilder builder) {
        this.logger = builder.logger;
        this.urlProvider = builder.urlProvider;
        this.errorHandler = builder.errorHandler;
        this.uiLoadingManager = builder.uiLoadingManager;
        this.timeout = builder.timeout;
        this.diskCacheExpireTimeout = builder.diskCacheExpireTimeout;
        this.memoryCacheExpireTimeout = builder.memoryCacheExpireTimeout;
        this.context = builder.context != null ? builder.context.getApplicationContext() : null;
    }

    public static ProNetwork init(Context context) {
        ProNetworkBuilder builder = new ProNetworkBuilder(context);
        ProNetwork proNetwork = new ProNetwork(builder);
        with(proNetwork);
        return proNetwork;
    }

    public static ProNetwork init(ProNetworkBuilder builder) {
        ProNetwork proNetwork = new ProNetwork(builder);
        with(proNetwork);
        return proNetwork;
    }

    public static ProNetwork getSingleton() {
        return singleton();
    }

    private static void setSingleton(ProNetwork proNetwork) {
        singleton = proNetwork;
    }

    private static ProNetwork with(ProNetwork proNetwork) {
        synchronized (ProNetwork.class) {
            setSingleton(proNetwork);
        }
        return singleton;
    }

    private static ProNetwork singleton() {
        if (singleton == null) {
            throw new IllegalStateException("Must Initialize ProNetwork before using singleton()");
        } else {
            return singleton;
        }
    }

    public Long getDiskCacheExpireTimeout() {
        return diskCacheExpireTimeout;
    }


    public Long getMemoryCacheExpireTimeout() {
        return memoryCacheExpireTimeout;
    }

    public Integer getMemoryCacheSize() {
        return memoryCacheSize;
    }

    public void setMemoryCacheSize(int memoryCacheSize) {
        this.memoryCacheSize = memoryCacheSize;
    }

    private void logger(ILogger logger) {
        this.logger = logger;
    }

    public void mock(MockManager mockManager) {
        this.mockManager = mockManager;
    }

    public ICacheStorage getDiskCacheStorage() {
        if (diskCacheStorage == null) {
            diskCacheStorage = new DiskCacheStorage(); //new PreferencesCacheStorage();
        }
        return diskCacheStorage;
    }

    public void setDiskCacheStorage(ICacheStorage diskCacheStorage) {
        this.diskCacheStorage = diskCacheStorage;
    }

    public ICacheStorage getMemoryCacheStorage() {
        if (memoryCacheStorage == null) {
            memoryCacheStorage = new MemoryCacheStorage();
        }
        return memoryCacheStorage;
    }

    public void setMemoryCacheStorage(ICacheStorage memoryCacheStorage) {
        this.memoryCacheStorage = memoryCacheStorage;
    }

    public ICachePathProvider getDiskCachePathProvider() {
        if (diskCachePathProvider == null) {
            diskCachePathProvider = new DiskCachePathProvider();
        }
        return diskCachePathProvider;
    }

    public void setDiskCachePathProvider(ICachePathProvider diskCachePathProvider) {
        this.diskCachePathProvider = diskCachePathProvider;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @NonNull
    public ILogger getLogger() {
        if (logger == null) {
            logger = new DefaultLogger();
        }
        return logger;
    }

    public UrlProvider getUrlProvider() {
        return urlProvider;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Context getContext() {
        return context;
    }

    public Dialog getDialog(Context context, int requestId, String loadingDialogTitle) {
        if (uiLoadingManager != null) {
            return uiLoadingManager.getLoading(context, requestId, loadingDialogTitle);
        }
        return null;
    }

    public MockManager getMockManager() {
        return mockManager;
    }


    public interface MockManager {
        INetworkOperator getMock();
    }


}
