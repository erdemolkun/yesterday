package com.protel.yesterday.location;


import com.protel.yesterday.location.model.LatLng;

/**
 * Created by erdemmac on 16/11/2016.
 */

public interface LocationCallbacks {
    /**
     * @param locationFetchType refers to {@link LocationManager.LocationTypes}
     */
    void onGotLocation(LatLng latLng, int locationFetchType);

    /**
     * Any error for manager Todo Add exception types.
     */
    void onLocationFetchError();

    /**
     * {@link LocationManager} is ready to request location updates. Can be used to start location
     * updates if {@link LocationManagerBuilder#requestLocationsAfterReady(boolean)} is set to false
     * to be used manager manually
     */
    void onReady();
}