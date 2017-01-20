package com.protel.network;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * Created by erdemmac on 19/08/15.
 */
public class Response {
    public static final int NETWORK = 0;
    public static final int DISK_CACHE = 1;
    public static final int MEMORY_CACHE = 2;

    /**
     * Real response parsed data.
     */
    @Nullable
    public Object data;

    /**
     * Raw request response. Only valid for #NETWORK response type.
     */
    public String rawData;

    /**
     * Response headers. Only valid for #NETWORK response type.
     */
    public Map<String, String> headers;

    /**
     * Time in milliseconds response received.
     */
    public Long resultMillis;

    /**
     * Type of response. Can be one of {@link FetchType} values.
     */
    public
    @FetchType
    int fetchType;


    public Response(Object data) {
        this.data = data;
        fetchType = NETWORK;
    }

    public Response(Object data, String rawData, Long resultMillis, @FetchType int fetchType, Map<String, String> headers) {
        this(data);
        this.rawData = rawData;
        this.resultMillis = resultMillis;
        this.fetchType = fetchType;
        this.headers = headers;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NETWORK, DISK_CACHE, MEMORY_CACHE})
    public @interface FetchType {
    }
}
