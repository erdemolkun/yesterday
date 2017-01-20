package com.protel.network.operators;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import com.protel.network.HttpStatus;
import com.protel.network.MultiPartInfo;
import com.protel.network.ProNetwork;
import com.protel.network.Request;
import com.protel.network.exceptions.UnAuthoritedException;
import com.protel.network.exceptions.UnknowServerException;
import com.protel.network.util.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by erdemmac on 17/01/16.
 */
class OkHttp3NetworkOperator extends INetworkOperator {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Call call;

    @Override
    public void start(final Request request) {
        super.start(request);
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            long timeoutInMilis = getTimeout();
            if (timeoutInMilis >= 0) {
                builder.connectTimeout(timeoutInMilis, TimeUnit.MILLISECONDS); // connect timeout
                builder.readTimeout(timeoutInMilis, TimeUnit.MILLISECONDS);    // socket timeout
                builder.writeTimeout(timeoutInMilis, TimeUnit.MILLISECONDS); // write timeout
            }
            SSLContext sslContext = request.getSslContext();
            if (sslContext != null && sslContext.getSocketFactory() != null) {
                builder.sslSocketFactory(sslContext.getSocketFactory());
            }
            HostnameVerifier hostnameVerifier = request.getHostnameVerifier();
            if (hostnameVerifier != null) {
                builder.hostnameVerifier(hostnameVerifier);
            }
            builder.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    okhttp3.Request requestOkhttp = chain.request();

                    // try the request
                    Response response = chain.proceed(requestOkhttp);

                    int tryCount = 1;
                    while (!response.isSuccessful() && tryCount < OkHttp3NetworkOperator.this.request.getTryCount()) {

                        LogUtils.l(this.getClass().getSimpleName(),
                                "Service call " + request.getName() + " " + request.getMethodLogName() + " retrying " + tryCount);
                        tryCount++;

                        // retry the request
                        response = chain.proceed(requestOkhttp);
                    }

                    // otherwise just pass the original response on
                    return response;
                }
            });
            List<Interceptor> interceptors = request.getInterceptors();
            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    builder.interceptors().add(interceptor);
                }
            }

            String hostedNamePin = request.getHostNameToPin();
            if (!TextUtils.isEmpty(hostedNamePin)) {
                ArrayList<String> validPinsList = request.getCertifiedPins();
                String[] validPins = new String[validPinsList.size()];
                validPinsList.toArray(validPins);
                builder.certificatePinner(new CertificatePinner.Builder()
                        .add(hostedNamePin, validPins)
                        .build());
            }

            okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
            requestBuilder.url(request.buildUrl());
            if (request.getMultiPartInfos() != null && request.getMultiPartInfos().size() > 0) {
                RequestBody requestBodyMultiPart;
                MultipartBody.Builder multiPartBodyBuilder = new MultipartBody.Builder();
                multiPartBodyBuilder.setType(MultipartBody.FORM);


                for (MultiPartInfo multiPartInfo : request.getMultiPartInfos()) {

                    if (multiPartInfo.body != null) {
                        MediaType mediaType = MediaType.parse(multiPartInfo.mediaType);
                        multiPartBodyBuilder.addFormDataPart(multiPartInfo.name, multiPartInfo.fileName,
                                RequestBody.create(mediaType, multiPartInfo.body));
                    } else {
                        multiPartBodyBuilder.addFormDataPart(multiPartInfo.name, multiPartInfo.fileName);
                    }

                }
                requestBodyMultiPart = multiPartBodyBuilder.build();
                requestBuilder.post(requestBodyMultiPart);
            } else if (request.getMethod() == Request.Method.GET) {
                requestBuilder.get();
            } else if (request.getMethod() == Request.Method.POST) {
                if (request.getContentType().equals(Request.CONTENT_TYPE_FORM)) {
                    Object data = request.getBody();
                    if (data != null && data instanceof HashMap) {
                        FormBody.Builder form_builder = new FormBody.Builder();
                        for (Map.Entry<String, String> val : ((HashMap<String, String>) data).entrySet()) {
                            form_builder.add(val.getKey(), val.getValue());
                        }
                        RequestBody formBody = form_builder.build();
                        requestBuilder.post(formBody);
                    }
                } else {
                    RequestBody body = RequestBody.create(JSON, request.bodyAsString());
                    requestBuilder.post(body);
                }

            } else if (request.getMethod() == Request.Method.PUT) {
                RequestBody body = RequestBody.create(JSON, request.bodyAsString());
                requestBuilder.put(body);
            } else if (request.getMethod() == Request.Method.DELETE) {
                RequestBody body = RequestBody.create(JSON, request.bodyAsString());
                requestBuilder.delete(body);
            } else if (request.getMethod() == Request.Method.PATCH) {
                RequestBody body = RequestBody.create(JSON, request.bodyAsString());
                requestBuilder.patch(body);
            } else if (request.getMethod() == Request.Method.HEAD) {
                requestBuilder.head();
            }

            requestBuilder.header("Content-Encoding", "gzip");

            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            OkHttpClient client = builder.build();
            final okhttp3.Request okhttpRequest = requestBuilder.build();
            ProNetwork.getSingleton().getLogger().logRequest(okhttpRequest.method(),
                    okhttpRequest.url().toString(), headersToMap(okhttpRequest.headers()), request.bodyAsString());


            call = client.newCall(okhttpRequest);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    Integer mockDuration = request.getMockDelay();
                    if (mockDuration != null) {
                        SystemClock.sleep(mockDuration);
                    }
                    Context context = ProNetwork.getSingleton().getContext();
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ProNetwork.getSingleton().getLogger().logErrorResponse(e);
                            error(e);
                        }
                    });
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    final String body = response.body().string();
                    response.body().close();
                    ProNetwork.getSingleton().getLogger().logResponse(response.code(),
                            response.request().url().toString(), headersToMap(response.headers()), body);

                    Integer mockDuration = request.getMockDelay();
                    if (mockDuration != null) {
                        SystemClock.sleep(mockDuration);
                    }
                    if (request.isCanceled()) {
                        return;
                    }

                    Context context = ProNetwork.getSingleton().getContext();
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            HttpStatus httpStatus = HttpStatus.valueOf(response.code());

                            if (httpStatus == HttpStatus.OK || httpStatus == HttpStatus.CREATED) {
                                Map<String, String> headers = new HashMap<>();
                                for (String name : response.headers().names()) {
                                    headers.put(name, response.header(name));
                                }
                                Object object;

                                try {
                                    object = request.parse(body);
                                } catch (Exception ex) {
                                    error(ex);
                                    return;
                                }
                                if (!request.canBeNull() && object == null) {
                                    error(new UnknowServerException("", HttpStatus.UNPROCESSABLE_ENTITY.value()));
                                    return;
                                }
                                com.protel.network.Response callbackResponse = new com.protel.network.Response(object);
                                callbackResponse.rawData = body;
                                callbackResponse.resultMillis = System.currentTimeMillis();
                                callbackResponse.headers = headers;
                                success(callbackResponse);

                            } else if (httpStatus == HttpStatus.UNAUTHORIZED) {
                                error(new UnAuthoritedException(body));
                            } else if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
                                error(new UnknowServerException(body, httpStatus.value()));
                            } else {
                                error(new UnknowServerException(body, httpStatus.value()));
                            }
                        }
                    });
                }
            });

        } catch (Exception ex) {
            error(ex);
        }
    }


    @Override
    public void cancel() {
        if (request != null) {
            if (call != null) {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
        }
    }

    private HashMap<String, String> headersToMap(Headers headers) {
        HashMap<String, String> hashMap = new HashMap<>();
        for (String name : headers.names()) {
            hashMap.put(name, headers.get(name));
        }
        return hashMap;
    }
}
