package com.protel.yesterday.data;


/**
 * Created by eolkun on 18.2.2015.
 */
public class LocationHolder {
    private static LatLng location;

    public static void set(LatLng location) {
        LocationHolder.location = location;
    }

    public static LatLng get() {
        return LocationHolder.location;
    }
}
