package com.protel.yesterday.location;

import com.google.android.gms.location.LocationRequest;

import android.content.Context;

/**
 * Created by erdemmac on 15/11/2016.
 */

public class LocationManagerBuilder {

    boolean finishOnPlayServicesError;
    boolean requestLocationsAfterReady = true;
    boolean onlyFirstLocation = true;
    int pollingInterval;
    int fastPollingInterval;
    LocationRequest locationRequest;
    LocationCallbacks callback;

    LocationManagerBuilder() {
    }

    public static LocationManagerBuilder builder() {
        return new LocationManagerBuilder();
    }

    /**
     * If true stop requesting new location updates. Otherwise location updates are published until
     * {@link LocationManager destroy} method is called manually
     **/
    public LocationManagerBuilder onlyFirstLocation(boolean onlyFirstLocation) {
        this.onlyFirstLocation = onlyFirstLocation;
        return this;
    }

    public LocationManagerBuilder requestLocationsAfterReady(boolean requestLocationsAfterReady) {
        this.requestLocationsAfterReady = requestLocationsAfterReady;
        return this;
    }

    public LocationManagerBuilder finishOnPlayServicesError(boolean finishOnPlayServicesError) {
        this.finishOnPlayServicesError = finishOnPlayServicesError;
        return this;
    }

    /**
     * Add callback for location events.
     */
    public LocationManagerBuilder callback(LocationCallbacks callback) {
        this.callback = callback;
        return this;
    }

    public LocationManagerBuilder pollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    public LocationManagerBuilder fastPollingInterval(int fastPollingInterval) {
        this.fastPollingInterval = fastPollingInterval;
        return this;
    }

    public LocationManagerBuilder locationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
        return this;
    }

    public LocationManager build(Context context) {
        return new LocationManager(context, this);
    }

    public LocationManager start(Context context) {
        LocationManager locationManager = build(context);
        locationManager.start();
        return locationManager;
    }
}
