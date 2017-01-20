# pronetwork

Android network library implementation to minimize boilerplate code and time creating restful mobile applications. 


## Initialization

Build instance first.

by using builder

```java
ProNetworkBuilder builder = new ProNetworkBuilder(getApplicationContext());
        builder.logger(new Logger() {
            @Override
            public void log(String tag, String content) {
                // Log logic here
            }

            @Override
            public void log(Exception ex) {
                // Log exception logic here
            }
        }).urlProvider(new UrlProvider() {
            @Override
            public String getBaseUrl(int functionId, int urlType) {
                // Give me a base url. 
            }
        }).errorHandler(new ErrorHandler() {
            @Override
            public boolean onErrorOccured(Exception ex, Request request) {
                // Return true if you handle error. 
            }
        });
        ProNetwork network = ProNetwork.init(builder);
```

or

```java
    ProNetwork.init(getApplicationContext)
```

## Add Request

In your activity , fragment or any custom class define implement ResponseListener interface

then 

```java
RequestController requestController = new RequestController();
Request request = new Request();
requestController.addToMainQueue(request,this);
```

## Loading Dialog

Set a default loading dialog.

```java
builder.loading(new UILoadingManager() {
            @Override
            public Dialog getLoading(Context context, int requestId) {
                return dialog;
            }
        });
```
## Timeout

Change default timeout for all requests

```java
builder.timeout(5000);
```

Also each request can have its own timeout duration
```java
Request request = ...
request.timeout(5000);
```


## Cache mechanism

Response classes will be cached if cache type is set other than CACHE_NONE and cache policy is set other than CACHE_POLICY_NONE.
Default cache type is CACHE_DISK.

Disk cache serialize response into file stream. 
Memory cache save response into the LRU cache.
Response classes should implement Serializable.

You can use setCacheType(int cacheType) method to define a cache type. 
Can be set of the values below.

```java
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
    
```

You can use setCachePolicy(int cachePolicy) method to define a cache policy. 
Can be set of the values below.

```java
    /**
     * No cache policy defined.
     **/
    public static final int CACHE_POLICY_NONE = 0;

    /**
     * After fetching cache result real network request will be discarded.
     */
    public static final int CACHE_POLICY_FETCH_AND_LEAVE = 1;

    /**
     * After fetching cache result real network request will be called.
     */
    public static final int CACHE_POLICY_FETCH_AND_REQUEST = 2;

```

## Memory cache size

Change memory cache size. It will be considered as the kilobyte.

```java
proNetwork.setMemoryCacheSize(1024);
```
    
## Cancelation

Each request can be canceled by **Request.cancel()**  or **RequestController.cancel()** for canceling all requests



