package com.protel.yesterday;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;

import com.protel.network.ErrorHandler;
import com.protel.network.ProNetwork;
import com.protel.network.ProNetworkBuilder;
import com.protel.network.Request;
import com.protel.network.UrlProvider;
import com.protel.network.adapter.SimpleLoggerAdapter;
import com.protel.network.interfaces.UILoadingManager;
import com.protel.yesterday.service.ServiceConstants;
import com.protel.yesterday.util.Alerts;
import com.protel.yesterday.util.L;

import java.util.HashMap;

/**
 * Created by erdemmac on 28/10/15.
 */
public class App extends Application implements UILoadingManager {
    private static final String API_KEY_WUNDERGROUND = "b2e60c85e184c3c1";
    private static final String PROPERTY_ID = "UA-58111264-2";
    private static App appInstance;
    private HashMap<TrackerName, Tracker> trackers = new HashMap<>();

    public static App getInstance() {
        return appInstance;
    }

    public static Context getContext() {
        if (appInstance == null) return null;
        return appInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        ProNetworkBuilder builder = new ProNetworkBuilder(getApplicationContext());
        builder.logger(new SimpleLoggerAdapter() {
            @Override
            public void log(String tag, String content) {
                L.e(tag, content);
            }

            @Override
            public void log(Exception ex) {
                L.ex(ex);
            }

            @Override
            public void logRequest(String method, String url, HashMap<String, String> headers, String body) {

            }
        }).urlProvider(new UrlProvider() {
            @Override
            public String getBaseUrl(int functionId, int urlType) {
                return ServiceConstants.WUNDERGROUND_BASE_API_URL + API_KEY_WUNDERGROUND + "/";
            }
        }).errorHandler(new ErrorHandler() {
            @Override
            public boolean onErrorOccured(Exception ex, Request request) {
                return false;
            }
        });
        builder.loading(this);
        builder.timeout(6000);
        ProNetwork.init(builder);
    }


    @Override
    public Dialog getLoading(Context context, int requestId, String loadingDialogTitle) {
        return Alerts.createLoadingDialog(context);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!trackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            GoogleAnalytics.getInstance(this).setDryRun(false);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) :
                    (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) : analytics.newTracker(R.xml.global_tracker);
            trackers.put(trackerId, t);
        }
        return trackers.get(trackerId);
    }

    public void sendTestAnalytics(Activity activity) {
        // Get tracker.
        Tracker tracker = App.getInstance().getTracker(
                App.TrackerName.APP_TRACKER);

        if (tracker == null) return;
//        // Enable Advertising Features.
//        tracker.enableAdvertisingIdCollection(true);


        String screenName = activity.getClass().getSimpleName();
        // Set screen name.
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        L.i("Analytics", "Screen name : " + screenName);

    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }
}
