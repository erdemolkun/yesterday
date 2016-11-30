package com.protel.yesterday.location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.protel.yesterday.R;
import com.protel.yesterday.location.model.LatLng;
import com.protel.yesterday.util.Alerts;
import com.protel.yesterday.util.L;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by erdemmac on 13/12/15.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationManager";

    private static final LocationManagerBuilder DEFAULT_BUILDER = new LocationManagerBuilder();

    private static final int DEFAULT_POLLING_FREQ = 1000 * 8;
    private static final int DEFAULT_FAST_POLLING_FREQ = 1000 * 4;

    /**
     * Id to identify a access fine location permission request.
     */
    private static final int REQUEST_PERMISSION_LOCATION = 5;
    /**
     * Used for android app settings page intent
     */
    private static final int REQUEST_APP_SETTINGS_PAGE = 6;
    /**
     * Called by play services api.
     */
    private static final int REQUEST_CHECK_GPS_SETTINGS = 7;
    /**
     * Custom intent to open gps intent
     */
    private static final int REQUEST_LOCATION_SETTINGS = 8;


    private static final int REQUEST_RECOVER_PLAY_SERVICES = 9;


    /**
     * Permissions required access location.
     */
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest = null;
    private Context context;
    private LocationCallbacks locationCallbacks;
    private boolean finishOnPlayServicesError;
    private boolean requestLocationsAfterReady = true;
    private Boolean hasPermissions = null;
    private LatLng location;
    private boolean onlyFirstLocation = true;
    int pollingInterval;
    int fastPollingInterval;
    private AtomicInteger state = new AtomicInteger(States.INITIAL);

    public LocationManager(@NonNull Context context) {
        this(context, DEFAULT_BUILDER);
    }

    public LocationManager(@NonNull Context context, LocationManagerBuilder locationManagerBuilder) {
        this.context = context;
        this.locationCallbacks = locationManagerBuilder.callback;
        this.finishOnPlayServicesError = locationManagerBuilder.finishOnPlayServicesError;
        this.requestLocationsAfterReady = locationManagerBuilder.requestLocationsAfterReady;
        this.onlyFirstLocation = locationManagerBuilder.onlyFirstLocation;
        this.pollingInterval = locationManagerBuilder.pollingInterval <= 0 ? DEFAULT_POLLING_FREQ : locationManagerBuilder.pollingInterval;
        this.fastPollingInterval = locationManagerBuilder.fastPollingInterval <= 0 ? DEFAULT_FAST_POLLING_FREQ : locationManagerBuilder.fastPollingInterval;
        this.locationRequest = locationManagerBuilder.locationRequest;
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(pollingInterval);
            locationRequest.setFastestInterval(fastPollingInterval);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    public void start() {
        start(false);
    }

    private boolean isRefresh = false;

    public boolean isManualRefreshMode() {
        return isRefresh;
    }

    /**
     * @param isRefresh true should be used when {@link #onlyFirstLocation} is true.
     */
    public void start(boolean isRefresh) {
        this.isRefresh = isRefresh;
        if (isRefresh && hasPermissions() && isClientConnected()) {
            requestLocationAfterSettingsControl(false);
        } else {

            if (onlyFirstLocation && LocationHolder.get() != null) {
                updateLocation(LocationHolder.get(), LocationTypes.MEMORY);
                return;
            }
            buildGoogleApiClient();
        }
    }

    public boolean hasPermissions() {
        if (hasPermissions == null) {
            // Verify that all required location permissions have been granted.
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermissions;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastKnownLocation != null) {
            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            updateLocation(latLng, LocationTypes.LAST_LOCATION);
        }
        if (location == null || !onlyFirstLocation) {
            requestLocationAfterSettingsControl(false);
        }
    }

    public void destroy() {
        if (location != null)
            LocationHolder.set(location);
        if (isClientConnected()) {
            dismissLocationRequest();
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        sendError();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        sendError();
    }

    private void dismissLocationRequest() {
        if (locationRequest != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            state.set(States.READY);
            locationRequest = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), LocationTypes.REQUEST_UPDATE_LOCATION);
        if (onlyFirstLocation) {
            dismissLocationRequest();
        }
    }

    private void updateLocation(LatLng location, int fetchLocationType) {
        this.location = location;
        LocationHolder.set(location);
        if (locationCallbacks != null) {
            locationCallbacks.onGotLocation(location, fetchLocationType);
        }
    }


    /**
     * Requests sdk location permissions. Using #Manifest.permission.ACCESS_FINE_LOCATION
     */
    private void requestLocationPermissions() {
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example, if the request has been denied previously.
                L.e(TAG,
                        "Displaying location permission rationale to provide additional context.");
                Alerts.dialog(activity).positive(R.string.ok).title(R.string.app_name).
                        message(R.string.permission_location_rationale).setCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                }).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Location permissions have not been granted yet. Request them directly.
                        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_PERMISSION_LOCATION);
                    }
                }).show();
            } else {
                // Location permissions have not been granted yet. Request them directly.
                ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_PERMISSION_LOCATION);
            }
        }

    }

    private synchronized void buildGoogleApiClient() {
        if (!checkPlayServices()) {
            sendError();
            return;
        }
        if (!checkSdkLocationPermissions()) {
            return;
        }

        if (googleApiClient != null && googleApiClient.isConnecting())
            return;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        state.set(States.API_CLIENT_CONNECT);
    }

    /**
     * @return true if play services is available. Otherwise tries to recover and returns false
     */
    private boolean checkPlayServices() {
        state.set(States.PLAY_SERVICES);
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (context instanceof Activity) {
                    final Activity activity = (Activity) context;
                    Dialog errDialog = googleApiAvailability.getErrorDialog(activity, resultCode, REQUEST_RECOVER_PLAY_SERVICES);
                    errDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (finishOnPlayServicesError) {
                                activity.finish();
                            }
                        }
                    });
                    errDialog.show();
                }
            }
            return false;
        }
        return true;
    }

    /**
     * @return true if location permissions are ready. Otherwise request permissions and returns
     * false
     */
    private boolean checkSdkLocationPermissions() {
        state.set(States.SDK_PERMISSION);
        // Verify that all required location permissions have been granted.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permissions have not been granted.
            if (context instanceof Activity) {
                L.e(TAG, "Location permissions has NOT been granted. Requesting permissions.");
                requestLocationPermissions();
            }
            return false;

        } else {
            L.i(TAG,
                    "Location permissions have already been granted. Start calling location.");
        }
        return true;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public LatLng getLatLng() {
        return location;
    }

    private void requestLocationAfterSettingsControl(final boolean explicitly) {

        state.set(States.LOCATION_SETTINGS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient
        LocationSettingsRequest locationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final Activity activity = (context != null && context instanceof Activity) ? (Activity) context : null;
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        requestLocationsIfNeeded();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            if (activity != null) {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        activity,
                                        REQUEST_CHECK_GPS_SETTINGS);
                            }
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            if (!explicitly) {
                                sendError();
                            } else {
                                activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SETTINGS);
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        if (!explicitly) {
                            sendError();
                        } else {
                            if (activity != null) {
                                activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SETTINGS);
                            }
                        }
                        break;
                }
            }

        });
    }

    private void requestLocationsIfNeeded() {
        state.set(States.READY);
        if (requestLocationsAfterReady) {
            requestUpdates();
        } else {
            if (locationCallbacks != null) {
                locationCallbacks.onReady();
            }
        }
    }

    private void sendError() {
        state.set(States.INITIAL);
        if (locationCallbacks != null) locationCallbacks.onLocationFetchError();
    }

    public void requestUpdates() {
        if (isClientConnected()) {
            state.set(States.REQUESTING_UPDATES);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    /**
     * @return true if {@link GoogleApiClient} is connected.
     */
    private boolean isClientConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    /**
     * Should be called inside Activity{@link #onActivityResult(int, int, Intent)}
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_SETTINGS_PAGE) {
            buildGoogleApiClient();
        } else if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (isClientConnected()) {
                requestLocationsIfNeeded();
            } else {
                buildGoogleApiClient();
            }
        } else if (requestCode == REQUEST_RECOVER_PLAY_SERVICES) {
            buildGoogleApiClient();
        } else if (requestCode == REQUEST_CHECK_GPS_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                buildGoogleApiClient();
            } else {
                sendError();
            }
        }
    }

    public void updatePollingIntervals(int fastPollingInterval, int pollingInterval) {
        this.pollingInterval = pollingInterval;
        this.fastPollingInterval = fastPollingInterval;
        if (state.get() == States.REQUESTING_UPDATES) {
            dismissLocationRequest();
        }
        locationRequest.setInterval(pollingInterval);
        locationRequest.setFastestInterval(fastPollingInterval);
        if (state.get() == States.READY) {
            requestUpdates();
        }
    }

    /**
     * Callback received when a permissions request has been completed. <br></>Should be called
     * inside Activity{@link #onRequestPermissionsResult(int, String[], int[])}
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_LOCATION) {

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                L.i(TAG, "LOCATION permission has now been granted. Requesting location.");
                hasPermissions = true;
                buildGoogleApiClient();
            } else {
                L.i(TAG, "LOCATION permission was NOT granted.");
                state.set(States.INITIAL);
                if (context instanceof Activity) {
                    final Activity activity = (Activity) context;
                    Snackbar.make(activity.findViewById(android.R.id.content), R.string.permission_location_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivityForResult(intent, REQUEST_APP_SETTINGS_PAGE);
                                }
                            }).show();
                }

            }

        }
    }


    static class LocationHolder {
        private static LatLng latLng;

        public static void set(LatLng location) {
            LocationHolder.latLng = location;
        }

        public static LatLng get() {
            return latLng;
        }
    }

    public interface LocationTypes {

        int MEMORY = 1;
        int LAST_LOCATION = 2;
        int REQUEST_UPDATE_LOCATION = 3;
    }

    interface States {
        int INITIAL = 0;
        int PLAY_SERVICES = 1;
        int SDK_PERMISSION = 2;
        int API_CLIENT_CONNECT = 3;
        int LOCATION_SETTINGS = 4;
        int READY = 5;
        int REQUESTING_UPDATES = 6;
    }

}
