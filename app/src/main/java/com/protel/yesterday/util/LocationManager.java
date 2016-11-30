package com.protel.yesterday.util;

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
import com.protel.yesterday.data.LatLng;
import com.protel.yesterday.data.LocationHolder;

/**
 * Created by erdemmac on 13/12/15.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationManager";

    private static final int POLLING_FREQ = 1000 * 6;
    private static final int FASTEST_UPDATE_FREQ = 1000 * 3;

    /**
     * Id to identify a access fine location permission request.
     */
    private static final int REQUEST_PERMISSION_LOCATION = 0;
    private static final int REQUEST_PERMISSION_SETTINGS = 1;
    /**
     * Called by play services api.
     */
    private static final int REQUEST_CHECK_GPS_SETTINGS = 2;
    /**
     * Custom intent to open gps intent
     */
    private static final int REQUEST_LOCATION_SETTINGS = 3;


    private static final int REQUEST_RECOVER_PLAY_SERVICES = 4;


    /**
     * Permissions required access location.
     */
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest = null;
    private Activity activity;
    private LocationCallback locationCallback;
    private boolean finishOnPlayServicesError;
    private Boolean hasPermissions = null;
    private LatLng currentLocation;
    private boolean isRefresh = false;

    public LocationManager(@NonNull Activity activity, boolean finishOnPlayServicesError, @NonNull LocationCallback locationCallback) {
        this.activity = activity;
        this.locationCallback = locationCallback;
        this.finishOnPlayServicesError = finishOnPlayServicesError;
    }

    public boolean hasPermissions() {
        if (hasPermissions == null) {
            // Verify that all required location permissions have been granted.
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return hasPermissions != null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        if (lastKnownLocation != null) {
            currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }
        if (currentLocation == null) {
            currentLocation = LocationHolder.get();
        }
        if (currentLocation != null) {
            locationChanged(currentLocation);
            return;
        }


        fetchLocationAfterCheckSettings(false);

    }

    public void onDestroy() {
        if (currentLocation != null)
            LocationHolder.set(currentLocation);
        dismissLocationRequest();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        sendError();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        sendError();
    }

    private void dismissLocationRequest() {

        if (locationRequest != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            locationRequest = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
        if (location != null) {
            dismissLocationRequest();
        }
    }

    private void locationChanged(final LatLng location) {
        currentLocation = location;
        LocationHolder.set(currentLocation);
        if (locationCallback != null) {
            locationCallback.onGotLocation(currentLocation);
        }
    }


    /**
     * Callback received when a permissions request has been completed.
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


                Snackbar.make(activity.findViewById(android.R.id.content), R.string.permission_location_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                LocationManager.this.activity.startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
                            }
                        }).show();

            }

        }
    }

    public void start(boolean isRefresh) {
        this.isRefresh = isRefresh;
        if (locationCallback != null) {
            locationCallback.onPrepare();
        }

        if (isRefresh && googleApiClient != null && googleApiClient.isConnected()) {
            fetchLocationAfterCheckSettings(false);
        } else {
            startGoogleApiClientWithPermissionCheck();
        }
    }

    public void start() {
        start(false);
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    private void startGoogleApiClientWithPermissionCheck() {
        // Verify that all required location permissions have been granted.
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permissions have not been granted.
            L.e(TAG, "Location permissions has NOT been granted. Requesting permissions.");
            requestLocationPermissions();

        } else {
            L.i(TAG,
                    "Location permissions have already been granted. Start calling location.");
            buildGoogleApiClient();
        }
    }

    /**
     * Requests the Location permissions.
     */
    private void requestLocationPermissions() {
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

    protected synchronized void buildGoogleApiClient() {
        if (!checkPlayServices()) {
            return;
        }

        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)

                .build();
        googleApiClient.connect();

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
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
            return false;
        }
        return true;
    }

    public boolean hasLocation() {
        return currentLocation != null;
    }

    public LatLng getLatLng() {
        return currentLocation;
    }

    public void fetchLocationAfterCheckSettings(final boolean explicitly) {

        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(POLLING_FREQ);
            locationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient
        LocationSettingsRequest locationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationManager.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity,
                                    REQUEST_CHECK_GPS_SETTINGS);
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
                            activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SETTINGS);
                        }
                        break;
                }
            }
        });
    }

    private void sendError() {
        if (locationCallback != null) locationCallback.onLocationFetchError();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PERMISSION_SETTINGS) {
            startGoogleApiClientWithPermissionCheck();
        } else if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                buildGoogleApiClient();
            }
        } else if (requestCode == REQUEST_RECOVER_PLAY_SERVICES) {
            checkPlayServices();
        } else if (requestCode == REQUEST_CHECK_GPS_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                sendError();
            }
        }
    }

    public interface LocationCallback {
        void onGotLocation(LatLng latLng);

        void onLocationFetchError();

        void onPrepare();
    }

}
