package com.protel.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.protel.network.interfaces.ResponseListener;
import com.protel.network.operators.INetworkOperator;

import org.json.JSONException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import okhttp3.Interceptor;

/**
 * Created by eolkun on 8.12.2014.
 */
public class Request {

    public static final int PARSE_TYPE_OBJECT = 0;
    public static final int PARSE_TYPE_ARRAY = 1;
    public static final int PARSE_TYPE_PLAIN_TEXT = 2;

    public static final int NO_LOADING = 0;
    public static final int START_ON_REQUEST = 1;
    public static final int FINISH_ON_RESPONSE = 2;
    public static final int FULL_CONTROL = 3;

    /**
     * No cache policy defined.
     */
    public static final int CACHE_POLICY_NONE = 0;

    /**
     * After fetching cache result real network request will be discarded.
     */
    public static final int CACHE_POLICY_FETCH_AND_LEAVE = 1;

    /**
     * After fetching cache result real network request will be called.
     */
    public static final int CACHE_POLICY_FETCH_AND_REQUEST = 2;

    /**
     * Cache result will be fethed only no network connection available.
     */
    public static final int CACHE_POLICY_ONLY_NO_NETWORK = 3;

    /**
     * Cache result won't be fetched.
     */
    public static final int CACHE_NONE = 0x00000000;

    /**
     * Memory cache result will be fetched.
     */
    public static final int CACHE_MEMORY = 0x00000001;

    /**
     * Disk cache result will be fetched.
     */
    public static final int CACHE_DISK = 0x00000002;
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static Gson gson;
    public ResponseListener responseListener;
    /**
     * Class type of response.
     */
    public Class responseClassType = null;
    public
    @LoadingIndicatorPolicy
    Integer loadingIndicatorPolicy = FULL_CONTROL;
    Integer method = Method.GET;
    private String contentType;
    @CachePolicy
    private Integer cachePolicy = CACHE_POLICY_NONE;
    private Integer cacheType = CACHE_NONE;
    @ParseType
    private Integer parseType = PARSE_TYPE_OBJECT;
    private Long timeout;
    private Integer mockDelayDuration;
    private Long diskCacheExpireTimeout;
    private Long memoryCacheExpireTimeout;
    private int tryCount = 1;

    private List<Interceptor> interceptors;
    private ArrayList<MultiPartInfo> multiPartInfos;
    private boolean supportsNullResponse = true;
    /**
     * Used to detect base url. Should be used with #UrlProvider.getBaseUrl
     */
    private Integer urlType = 0;
    private SSLContext sslContext;
    /**
     * ID of the service. Should be unique among various request types.
     */
    private int functionId;
    /**
     * Is service mocked. If true request will be operated in MockNetworkOperator
     */
    private boolean isMock = false;
    /**
     * URL of the service function.
     */
    private String baseUrl;
    /**
     * Data of the request placed in HTTP Post method.
     */
    private Object body = null;
    /**
     * Name of the service request. Url build as http://:baseurl/:name
     */
    private String name = null;
    /**
     * Loading dialog title text of the service request for dialog window.
     */
    private String loadingDialogTitle = null;
    /**
     * Service headers.
     */
    private Map<String, String> headers = null;
    /**
     * Service query parameters
     */
    private Map<String, String> params = null;
    private String cacheCode = null;
    private String tag;
    private INetworkOperator iNetworkOperator;

    /**
     * {@link RequestController cancels requests if isCancelable is null or true. If a loading
     * dialog appears only isCancelable true ones will be canceled. }
     */
    private Boolean isCancelable = null;
    private AtomicBoolean isCanceled = new AtomicBoolean(false);
    private HostnameVerifier hostnameVerifier;
    private String hostNameToPin;
    private ArrayList<String> pins;

    Request() {
        contentType = CONTENT_TYPE_JSON;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (interceptors == null) interceptors = new ArrayList<>();
        interceptors.add(interceptor);
    }

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public ArrayList<MultiPartInfo> getMultiPartInfos() {
        return multiPartInfos;
    }

    public void setMultiPartInfos(ArrayList<MultiPartInfo> multiPartInfos) {
        this.multiPartInfos = multiPartInfos;
    }

    public boolean canBeNull() {
        return supportsNullResponse;
    }

    public void nonNull() {
        supportsNullResponse = false;
    }

    public void setiNetworkOperator(INetworkOperator iNetworkOperator) {
        this.iNetworkOperator = iNetworkOperator;
    }

    public void setIsCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    public Boolean isCancelable() {
        return isCancelable;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * @return true if canceled
     */
    public boolean cancel() {
        if (isCancelable() != null && !isCancelable()) return false;
        if (iNetworkOperator != null)
            iNetworkOperator.cancel();
        responseListener = null;
        isCanceled.set(true);
        return true;
    }

    public boolean isCanceled() {
        return isCanceled.get();
    }

    public int getMethod() {
        return method;
    }

    public String getMethodLogName() {
        if (method == Method.POST) {
            return "POST";
        } else if (method == Method.GET) {
            return "GET";
        } else if (method == Method.DELETE) {
            return "DELETE";
        } else if (method == Method.PUT) {
            return "PUT";
        } else if (method == Method.PATCH) {
            return "PATCH";
        } else if (method == Method.HEAD) {
            return "HEAD";
        }
        return "GET";
    }

    public Integer getParseType() {
        return parseType;
    }

    public void setParseType(@ParseType Integer parseType) {
        this.parseType = parseType;
    }

    public Long getDiskCacheExpireTimeout() {
        return diskCacheExpireTimeout;
    }

    /**
     * @param expire_timeout Sets the default cache expire timeout for cache file objects. Values
     *                       must be between 1 and {@link Integer#MAX_VALUE} when converted to
     *                       milliseconds.
     * @see Request#expireAfter(long, TimeUnit)
     */
    public Request expireAfter(long expire_timeout) {
        return expireAfter(expire_timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param expire_timeout Sets the default cache expire timeout for cache file objects. Values
     *                       must be between 1 and {@link Integer#MAX_VALUE} when converted to
     *                       milliseconds.
     * @param unit           {@link TimeUnit} instance for time convertion
     * @see Request#expireAfter(long)
     */
    public Request expireAfter(long expire_timeout, TimeUnit unit) {
        if (expire_timeout < 0) throw new IllegalArgumentException("expire_timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(expire_timeout);
        if (millis == 0 && expire_timeout > 0)
            throw new IllegalArgumentException("Timeout too small.");
        diskCacheExpireTimeout = millis;
        return this;
    }

    public Integer getMockDelay() {
        return mockDelayDuration;
    }

    public void setMockDelay(int duration) {
        setMockDelay(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the mock delay duration.
     *
     * @see Request#setMockDelay(int)
     */
    public void setMockDelay(long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException("timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis == 0 && duration > 0) throw new IllegalArgumentException("Timeout too small.");
        mockDelayDuration = (int) millis;
    }

    public Integer getUrlType() {
        return urlType;
    }

    public void setUrlType(Integer urlType) {
        this.urlType = urlType;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public void addCertifiedPins(String hostname, String... certified_pins) {
        this.hostNameToPin = hostname;
        this.pins = new ArrayList<>();
        Collections.addAll(pins, certified_pins);
    }

    public String getHostNameToPin() {
        return hostNameToPin;
    }

    public ArrayList<String> getCertifiedPins() {
        return pins;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }


    public
    @CachePolicy
    Integer getCachePolicy() {
        return cachePolicy;
    }

    /**
     * @param cachePolicy on of #@CachePolicy values.
     */
    public void setCachePolicy(@CachePolicy Integer cachePolicy) {
        if (!this.hasCache() && cacheType == CACHE_NONE) {
            cacheType = CACHE_DISK;
        }
        this.cachePolicy = cachePolicy;
    }

    public Integer getCacheType() {
        return cacheType;
    }

    /**
     * @param cacheType on of #@CacheType values.
     */
    public void setCacheType(Integer cacheType) {
        this.cacheType = cacheType;
    }

    /**
     * Sets the default timeout for new connections. A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public Request timeout(int timeout) {
        return timeout(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the default timeout for new connections. A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public Request timeout(long timeout, TimeUnit unit) {
        if (timeout < 0) throw new IllegalArgumentException("timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(timeout);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException("Timeout too large.");
        if (millis == 0 && timeout > 0) throw new IllegalArgumentException("Timeout too small.");
        this.timeout = millis;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

    public Long getMemoryCacheExpireTimeout() {
        return memoryCacheExpireTimeout;
    }

    /**
     * @param expire_timeout Sets the default cache expire timeout for cache file objects. Values
     *                       must be between 1 and {@link Integer#MAX_VALUE} when converted to
     *                       milliseconds.
     * @see Request#expireAfter(long, TimeUnit)
     */
    public Request expireMemoryAfter(long expire_timeout) {
        return expireMemoryAfter(expire_timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param expire_timeout Sets the default cache expire timeout for cache file objects. Values
     *                       must be between 1 and {@link Integer#MAX_VALUE} when converted to
     *                       milliseconds.
     * @param unit           {@link TimeUnit} instance for time convertion
     * @see Request#expireAfter(long)
     */
    public Request expireMemoryAfter(long expire_timeout, TimeUnit unit) {
        if (expire_timeout < 0) throw new IllegalArgumentException("expire_timeout < 0");
        if (unit == null) throw new IllegalArgumentException("unit == null");
        long millis = unit.toMillis(expire_timeout);
        if (millis == 0 && expire_timeout > 0)
            throw new IllegalArgumentException("Timeout too small.");
        memoryCacheExpireTimeout = millis;
        return this;
    }

    public Object getBody() {
        return body;
    }

    private Request setBody(Object data) {
        this.body = data;
        return this;
    }

    public void baseUrl(String url) {
        this.baseUrl = url;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean isMock) {
        this.isMock = isMock;
    }

    public boolean hasCache() {
        return cacheType != CACHE_NONE;
    }

    public boolean hasDiskCacheType() {
        return (cacheType & CACHE_DISK) != 0;
    }

    public boolean hasMemoryCacheType() {
        return (cacheType & CACHE_MEMORY) != 0;
    }

    public boolean getIsBackground() {
        return loadingIndicatorPolicy == NO_LOADING;
    }

    /**
     * Use #setLoadingIndicatorPolicy instead.
     */
    @Deprecated
    public Request setIsBackground(boolean isBackground) {
        loadingIndicatorPolicy = isBackground ? NO_LOADING : FULL_CONTROL;
        return this;
    }

    public Request name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getLoadingDialogTitle() {
        return loadingDialogTitle;
    }

    public void setLoadingDialogTitle(String loadingDialogTitle) {
        this.loadingDialogTitle = loadingDialogTitle;
    }

    public Request id(int id) {
        this.functionId = id;
        return this;
    }

    private String getBaseUrl() {
        if (baseUrl != null) return baseUrl;
        UrlProvider urlProvider = ProNetwork.getSingleton().getUrlProvider();
        if (urlProvider != null) {
            return urlProvider.getBaseUrl(functionId, getUrlType());
        }
        return "";
    }

    public int getID() {
        return functionId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Request noLoading() {
        return setLoadingIndicatorPolicy(NO_LOADING);
    }

    public Request setLoadingIndicatorPolicy(@LoadingIndicatorPolicy Integer loadingIndicatorPolicy) {
        this.loadingIndicatorPolicy = loadingIndicatorPolicy;
        return this;
    }

    /**
     * Update all http request headers.
     */
    public Request headers(Map<String, String> headers) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Add a single http request header
     */
    public Request header(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
        return this;
    }

    /**
     * Update all url params.
     *
     * ex: url?key=value&key2=value2&...&...&...
     */
    public Request params(Map<String, String> params) {
        this.params = new HashMap<>();
        this.params.putAll(params);
        return this;
    }

    /**
     * Add a single url param
     *
     * ex: url?key=value
     */
    public Request param(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {
            params.put(key, value);
        }
        return this;
    }

    private String getUrl() {
        return getBaseUrl() + name;
    }

    public String buildUrl() {
        return getUrl() + buildParams();
    }

    public String bodyAsString() {
        if (body == null) return null;
        if (contentType.equals(CONTENT_TYPE_FORM)) {
            return body.toString();
        } else {
            Gson gson = getGson();
            return gson.toJson(body);
        }
    }

    public byte[] bodyAsBytes() {
        if (body == null) return new byte[]{};
        return bodyAsString().getBytes();
    }

    public void post(Object data) {
        setBody(data);
        method = Method.POST;
    }

    public void delete(Object data) {
        setBody(data);
        method = Method.DELETE;
    }

    public void patch(Object data) {
        setBody(data);
        method = Method.PATCH;
    }

    public void put(Object data) {
        setBody(data);
        method = Method.PUT;
    }

    public void head() {
        method = Method.HEAD;
    }

    public void get() {
        method = Method.GET;
    }

    @Nullable
    public Object parse(String response) throws Exception {
        if (parseType == PARSE_TYPE_ARRAY) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonRootElement = jsonParser.parse(response);
            if (jsonRootElement.isJsonArray()) {
                JsonArray jsonArray = jsonRootElement.getAsJsonArray();
                ArrayList arrayList = new ArrayList();
                for (JsonElement jsonElement : jsonArray) {
                    Object object = toObject(jsonElement.toString(), responseClassType);
                    arrayList.add(object);
                }
                return arrayList;
            } else {
                throw new JSONException("Parse Error");
            }
        } else if (parseType == PARSE_TYPE_PLAIN_TEXT) {
            return response;
        } else {
            return toObject(response, responseClassType);
        }
    }

    private Object toObject(String str, Class classType) throws Exception {
        if (classType == null) return null;
        return getGson().fromJson(str, classType);
    }

    public String buildParams() {
        if (params == null || params.size() < 0) {
            return "";
        }
        String ret = "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            ret += entry.getKey() + "=" + entry.getValue() + "&";
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    public String getCacheCode() {
        return cacheCode;
    }

    public void setCacheCode(String cacheCode) {
        this.cacheCode = cacheCode;
    }

    @Override
    public String toString() {
        return "url=" + buildUrl()
                + ",method=" + method;
    }

    /**
     * Supported request methods.
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int PATCH = 5;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PARSE_TYPE_OBJECT, PARSE_TYPE_ARRAY, PARSE_TYPE_PLAIN_TEXT})

    public @interface ParseType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NO_LOADING, START_ON_REQUEST, FINISH_ON_RESPONSE, FULL_CONTROL})
    public @interface LoadingIndicatorPolicy {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_POLICY_NONE, CACHE_POLICY_FETCH_AND_LEAVE, CACHE_POLICY_FETCH_AND_REQUEST, CACHE_POLICY_ONLY_NO_NETWORK})
    public @interface CachePolicy {
    }

    public static class Builder {
        private String name = null;
        private Integer id = null;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Request build() {
            Request request = new Request();
            if (id == null) throw new IllegalStateException("id parameter should be set");
            request.id(id);
            if (name == null) throw new IllegalStateException("name parameter should be set");
            request.name(name);
            return request;
        }
    }

}
