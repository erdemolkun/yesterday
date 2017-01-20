package com.protel.network;

import android.app.Dialog;

import com.protel.network.exceptions.NoNetworkException;
import com.protel.network.interfaces.ResponseListener;
import com.protel.network.operators.DiskCacheNetworkOperator;
import com.protel.network.operators.INetworkOperator;
import com.protel.network.operators.MemoryCacheNetworkOperator;
import com.protel.network.util.LogUtils;
import com.protel.network.util.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by eolkun on 12.1.2015. <p> Controller class to manage request error dialogs and
 * resending requests. <p> Each activity should have a single controller to add requests..
 */
public class RequestController {
    private ArrayList<Request> lastRequests;
    private Dialog errorDialog;
    private SyncRequests syncRequests = new SyncRequests();
    private LoadingController loadingController;
    private CancelCallback cancelCallback;

    public RequestController(CancelCallback cancelCallback) {
        this.cancelCallback = cancelCallback;
        init();
    }

    public RequestController() {
        init();
    }

    private void init() {
        loadingController = new LoadingController(new LoadingController.CancelCallback() {
            @Override
            public void onCanceled() {
                cancel(false);
            }
        });
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(int id) {
        cancel(id, true);
    }

    public void cancel(int id, boolean isExplicit) {
        ArrayList<Request> canceledRequests = new ArrayList<>();
        if (syncRequests != null && syncRequests.requests != null) {
            for (Request request : syncRequests.requests) {
                if (request.getID() == id) {
                    if (request.cancel()) {
                        canceledRequests.add(request);
                        printCanceledLog(request);
                    }
                }
            }
            syncRequests.requests.removeAll(canceledRequests);
        }

        if (!isExplicit) {
            if (cancelCallback != null)
                cancelCallback.onCanceled(canceledRequests);
        }
    }

    private void cancel(boolean isExplicit) {
        ArrayList<Request> canceledRequests = new ArrayList<>();
        if (syncRequests != null && syncRequests.requests != null) {
            for (Request request : syncRequests.requests) {
                if (request.cancel()) {
                    canceledRequests.add(request);
                    printCanceledLog(request);
                }
            }
            syncRequests.requests.clear();
        }

        if (!isExplicit) {
            if (cancelCallback != null)
                cancelCallback.onCanceled(canceledRequests);
        }
    }

    private void printCanceledLog(Request request) {
        LogUtils.l(this.getClass().getSimpleName(),
                "Service call " + request.getName() + " " + request.getMethodLogName() + " canceled");
    }

    public LoadingController getLoadingController() {
        return loadingController;
    }

    private void addToLastRequests(Request request) {
        if (lastRequests == null)
            lastRequests = new ArrayList<>();

        ArrayList<Request> toRemove = new ArrayList<>();
        for (Request requestTemp : lastRequests) {
            if (requestTemp.getID() == request.getID()) {
                toRemove.add(requestTemp);
            }
        }
        lastRequests.removeAll(toRemove);

        if (lastRequests.size() > 2) {
            lastRequests.remove(lastRequests.size() - 1);
        }
        lastRequests.add(request);
    }

    public boolean resendRequest(int methodId, ResponseListener responseListener) {
        if (lastRequests != null) {
            for (Request request : lastRequests) {
                if (request != null && request.getID() == methodId) {
                    addToMainQueue(request, responseListener);
                    break;
                }
            }
        }
        return false;
    }

    public boolean addToMainQueue(Request request, ResponseListener responseListener) {
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
        request.responseListener = responseListener;
        boolean isSend = addRequest(request.responseListener, request);
        if (isSend) {
            addToLastRequests(request);
        } else {
            return false;
        }
        return true;
    }

    public synchronized boolean addRequest(final ResponseListener responseListener, final Request request) {

        final boolean isNetworkConnected = NetworkUtils.isConnected();
        final @Request.CachePolicy Integer cachePolicy = request.getCachePolicy();
        if (request.hasCache() && cachePolicy != Request.CACHE_POLICY_NONE &&
                !(cachePolicy == Request.CACHE_POLICY_ONLY_NO_NETWORK && isNetworkConnected)) {

            INetworkOperator.OnNetworkResult cacheNetworkResult =
                    new INetworkOperator.OnNetworkResult() {
                        @Override
                        public void onComplete(Response response) {
                            postRequest(request);
                            if (responseListener != null) {
                                response.fetchType = Response.DISK_CACHE;
                                responseListener.onResponse(response, request);
                            }
                            if (cachePolicy == Request.CACHE_POLICY_FETCH_AND_REQUEST) {
                                request.setLoadingIndicatorPolicy(Request.NO_LOADING);
                                boolean isConnected = NetworkUtils.isConnected();
                                if (!isConnected) {
                                    sendError(new NoNetworkException(), request, responseListener);
                                } else {
                                    callRealRequest(request, responseListener);
                                }
                            }
                        }

                        @Override
                        public void onError(Exception ex) {
                            boolean isConnected = NetworkUtils.isConnected();
                            if (!isConnected) {
                                sendError(new NoNetworkException(), request, responseListener);
                            } else {
                                callRealRequest(request, responseListener);
                            }
                        }
                    };
            final DiskCacheNetworkOperator diskCacheNetworkOperator = new DiskCacheNetworkOperator();
            diskCacheNetworkOperator.setOnNetworkResultListener(cacheNetworkResult);

            if (request.hasMemoryCacheType()) {
                INetworkOperator.OnNetworkResult memoryCacheNetworkResult =
                        new INetworkOperator.OnNetworkResult() {
                            @Override
                            public void onComplete(Response response) {
                                postRequest(request);
                                if (responseListener != null) {
                                    response.fetchType = Response.MEMORY_CACHE;
                                    responseListener.onResponse(response, request);
                                }
                                if (cachePolicy == Request.CACHE_POLICY_FETCH_AND_REQUEST) {
                                    request.setLoadingIndicatorPolicy(Request.NO_LOADING);
                                    boolean isConnected = NetworkUtils.isConnected();
                                    if (!isConnected) {
                                        sendError(new NoNetworkException(), request, responseListener);
                                    } else {
                                        callRealRequest(request, responseListener);
                                    }
                                }
                            }

                            @Override
                            public void onError(Exception ex) {
                                if (request.hasDiskCacheType()) {
                                    diskCacheNetworkOperator.start(request);
                                } else {
                                    boolean isConnected = NetworkUtils.isConnected();
                                    if (!isConnected) {
                                        sendError(new NoNetworkException(), request, responseListener);
                                    } else {
                                        callRealRequest(request, responseListener);
                                    }
                                }
                            }
                        };
                MemoryCacheNetworkOperator memoryCacheNetworkOperator = new MemoryCacheNetworkOperator();
                memoryCacheNetworkOperator.setOnNetworkResultListener(memoryCacheNetworkResult);
                memoryCacheNetworkOperator.start(request);

            } else if (request.hasDiskCacheType()) {
                diskCacheNetworkOperator.start(request);
            }
            return true;

        } else {
            if (!isNetworkConnected) {
                sendError(new NoNetworkException(), request, responseListener);
                return true;
            } else {
                return callRealRequest(request, responseListener);
            }
        }
    }

    private boolean callRealRequest(final Request request, final ResponseListener responseListener) {

        preRequest(request, responseListener);
        INetworkOperator.OnNetworkResult onNetworkResult = new INetworkOperator.OnNetworkResult() {
            @Override
            public void onComplete(Response response) {
                postRequest(request);
                if (responseListener != null) {
                    response.fetchType = Response.NETWORK;
                    responseListener.onResponse(response, request);
                }
            }

            @Override
            public void onError(Exception ex) {
                sendError(ex, request, responseListener);
            }
        };

        if (request.isMock()) {
            INetworkOperator mockNetworkOperator = ProNetwork.getSingleton().getMockManager().getMock();
            if (mockNetworkOperator != null) {
                mockNetworkOperator.setOnNetworkResultListener(onNetworkResult);
                mockNetworkOperator.start(request);
                return true;
            } else {
                return false;
            }
        }

        INetworkOperator networkOperator = INetworkOperator.getInstance();
        networkOperator.setOnNetworkResultListener(onNetworkResult);
        networkOperator.start(request);
        return true;
    }

    private synchronized void preRequest(Request request, ResponseListener responseListener) {
        if ((request.loadingIndicatorPolicy == Request.START_ON_REQUEST ||
                request.loadingIndicatorPolicy == Request.FULL_CONTROL
        ) && !getSyncRequests().hasRequestInsideOtherThanMe(request)) {
            getLoadingController().showLoading(responseListener, request);
        }
        getSyncRequests().add(request);
    }

    private synchronized void postRequest(Request request) {
        postRequest(request, false);
    }

    /**
     * Will be called after request call completed or an error case exists.
     */
    private synchronized void postRequest(Request request, boolean forceCloseDialog) {
        boolean shouldDismiss = ((request.loadingIndicatorPolicy == Request.FINISH_ON_RESPONSE ||
                request.loadingIndicatorPolicy == Request.FULL_CONTROL) && !getSyncRequests().hasRequestInsideOtherThanMe(request)) || forceCloseDialog;

        if (shouldDismiss) {
            getLoadingController().dismissLoading(false);
        }
        getSyncRequests().remove(request);
    }

    private void sendError(Exception ex, Request request, ResponseListener responseListener) {
        postRequest(request, true);
        LogUtils.ex(ex);
        if (responseListener != null) {
            responseListener.onErrorResponse(request, ex);
        }
    }

    public void setErrorDialog(Dialog dialog) {
        this.errorDialog = dialog;
    }

    public SyncRequests getSyncRequests() {
        return syncRequests;
    }

    public interface CancelCallback {
        void onCanceled(ArrayList<Request> canceledRequests);
    }

    public class SyncRequests {

        private ArrayList<Request> requests = new ArrayList<>();

        public boolean hasRequestInsideOtherThanMe(Request baseRequest) {
            if (requests == null || requests.size() < 1) return false;
            for (Request request : requests) {
                if ((request.loadingIndicatorPolicy == Request.FINISH_ON_RESPONSE ||
                        request.loadingIndicatorPolicy == Request.FULL_CONTROL) && !request.equals(baseRequest)) {
                    return true;
                }
            }
            return false;
        }

        public synchronized void add(Request request) {
            requests.add(request);
        }

        public synchronized void remove(Request request) {
            requests.remove(request);
        }
    }
}



