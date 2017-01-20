package com.protel.network.operators;

import com.protel.network.ErrorHandler;
import com.protel.network.ProNetwork;
import com.protel.network.ProtelExecuterService;
import com.protel.network.Request;
import com.protel.network.Response;
import com.protel.network.util.LogUtils;
import com.protel.network.util.SecurityUtils;

/**
 * Created by eolkun on 13.1.2015.
 */
public abstract class INetworkOperator {

    /**
     * Used in miliseconds
     */
    protected static final int DEFAULT_TIMEOUT = 15000;
    private static final String RESPONSE_FILE_PREFIX = "__temp_test";
    protected Request request;
    protected OnNetworkResult onNetworkResultListener;
    private Long startMilis;

    public INetworkOperator() {

    }

    public static INetworkOperator getInstance(Operators operator) {
        if (operator == Operators.DEFAULT) {
            // TODO  Select using Class.forname
            return new OkHttp3NetworkOperator();
        } else if (operator == Operators.HTTPCLIENT) {
            //return new DefaultNetworkOperator(request, onNetworkResultListener, context);
        } else if (operator == Operators.OKHTTP) {
            return new OkHttp3NetworkOperator();
        } else if (operator == Operators.VOLLEY) {
            //return new VolleyNetworkOperator(request, onNetworkResultListener, context);
        } else {
            throw new IllegalArgumentException("Non defined operator");
        }
        return new OkHttp3NetworkOperator();
    }

    public static INetworkOperator getInstance() {
        return getInstance(Operators.OKHTTP);
    }

    public static String getCacheFileName(Request request) {
        String cachePath = RESPONSE_FILE_PREFIX + request.getID();
        if (request.getCacheCode() != null) {
            cachePath += request.getCacheCode();
        }
        return SecurityUtils.encrypt(cachePath);
    }

    public abstract void cancel();

    public void setOnNetworkResultListener(OnNetworkResult onNetworkResultListener) {
        this.onNetworkResultListener = onNetworkResultListener;
    }

    protected long getTimeout() {

        if (request.getTimeout() != null)
            return request.getTimeout();

        Integer timeout = ProNetwork.getSingleton().getTimeout();
        if (timeout == null) {
            return DEFAULT_TIMEOUT;
        }
        return timeout;
    }

    protected boolean canWriteToCache() {
        return true;
    }


    protected void success(final Response response) {

        // Log for success service duration.
        if (startMilis != null) {
            long callDuration = System.currentTimeMillis() - startMilis;
            LogUtils.l(this.getClass().getSimpleName(), "Service call " + request.getName() + " " + request.getMethodLogName() + " completed in " + callDuration + " milis");
        }

        // Write to cache if operator is eligible for this and request is cacheable
        if (canWriteToCache()) {
            if (request.hasCache()) {
                ProtelExecuterService protelExecuterService = ProtelExecuterService.get();
                protelExecuterService.submit(new Runnable() {
                    @Override
                    public void run() {
                        ProNetwork.getSingleton().getDiskCacheStorage().save(request, response);
                    }
                });
                if (request.hasMemoryCacheType()) {
                    ProNetwork.getSingleton().getMemoryCacheStorage().save(request, response);
                }
            }
        }
        if (onNetworkResultListener != null)
            onNetworkResultListener.onComplete(response);
    }


    protected void error(Exception ex) {
        LogUtils.l(this.getClass().getSimpleName(), "Service call " + request.getName() + " " + request.getMethodLogName() + " error : " + (ex == null ? "null" : ex.toString()));
        if (request.isCanceled())
            return;
        // Exception handler at application level.
        ErrorHandler errorHandler = ProNetwork.getSingleton().getErrorHandler();
        if (errorHandler != null) {
            if (errorHandler.onErrorOccured(ex, request)) {
                return;
            }
        }
        //If handler has nothing to do then will be delivered to default response listener.
        if (onNetworkResultListener != null)
            onNetworkResultListener.onError(ex);
    }


    public void start(Request request) {
        this.request = request;
        this.request.setiNetworkOperator(this);
        startMilis = System.currentTimeMillis();
    }

    public enum Operators {
        DEFAULT, VOLLEY, OKHTTP, HTTPCLIENT
    }

    /*
    * Simple callback for operator success and failure results.
    * one
    */
    public interface OnNetworkResult {

        void onComplete(Response response);

        void onError(Exception ex);
    }

}
