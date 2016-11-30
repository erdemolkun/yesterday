package com.protel.yesterday;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.protel.network.Request;
import com.protel.network.RequestController;
import com.protel.network.Response;
import com.protel.network.interfaces.ResponseListener;
import com.protel.yesterday.service.response.ParseImagesResponse;
import com.protel.yesterday.util.Alerts;
import com.protel.yesterday.util.L;
import com.protel.yesterday.util.LocalizationManager;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by erdemmac on 04/11/15.
 */
public class SplashActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, ResponseListener {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int MIN_SPLASH_DURATION = 500;

    /**
     * Id to identify a access fine location permission request.
     */
    private static final int REQUEST_FINE_LOCATION = 0;
    private static final int REQUEST_PERMISSION_SETTINGS = 1;

    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    ParseImagesResponse parseImagesResponse = null;
    int imageIndex = 0;
    private View vRoot;
    private RequestController requestController;
    private long startTime;
    private boolean locationPermissionDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startTime = System.currentTimeMillis();
        LocalizationManager.refresh();
        vRoot = findViewById(R.id.v_splash_root);
        requestController = new RequestController();


        Request request = new Request.Builder().name("Images").id(3).build();
        request.baseUrl("https://api.parse.com/1/classes/");
        request.responseClassType = ParseImagesResponse.class;
        request.expireAfter(1, TimeUnit.DAYS);
        request.setCachePolicy(Request.CACHE_POLICY_FETCH_AND_LEAVE);
        request.setLoadingIndicatorPolicy(Request.NO_LOADING);
        request.header("X-Parse-REST-API-Key", "a1Mr32H8hWzn9oy2n4iahv3szA3d3rucFoLhv7z6");
        request.header("X-Parse-Application-Id", "QjzdphLWuAUmUUJzDQtT3cWEUGeLuKPZgCupaIfQ");
        requestController.addToMainQueue(request, this);

    }

    private void startApp() {
        long systemMilis = System.currentTimeMillis();
        if (systemMilis - startTime < MIN_SPLASH_DURATION) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startApp();
                }
            }, MIN_SPLASH_DURATION - (systemMilis - startTime) + 10);
            return;
        }
        Intent homeIntent = new Intent(this, HomeActivity.class);
        if (parseImagesResponse != null) {
            homeIntent.putExtra(HomeActivity.EXTRA_PARSE_IMAGES, parseImagesResponse);
            homeIntent.putExtra(HomeActivity.EXTRA_PARSE_IMAGE_INDEX, imageIndex);
        }
        startActivity(homeIntent);
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        YesterdayApp.getInstance().sendTestAnalytics(this);
        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        // Verify that all required location permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Contacts permissions have not been granted.
            L.e(TAG, "Location permissions has NOT been granted. Requesting permissions.");
            requestLocationPermissions();

        } else {

            L.e(TAG, "Location permissions have already been granted.");
            if (parseImagesResponse != null) {
                startApp();
            } else {
                locationPermissionDone = true;
            }
        }
    }

    /**
     * Requests the Location permissions.
     */
    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            L.e(TAG,
                    "Displaying location permission rationale to provide additional context.");

            Alerts.dialog(this).positive(R.string.ok).title(R.string.app_name).
                    message(R.string.permission_location_rationale).setCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            }).listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Location permissions have not been granted yet. Request them directly.
                    ActivityCompat.requestPermissions(SplashActivity.this, PERMISSIONS_LOCATION, REQUEST_FINE_LOCATION);
                }
            }).show();
        } else {
            // Location permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_FINE_LOCATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTINGS) {
            checkPermissionsAndStart();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FINE_LOCATION) {

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                L.i(TAG, "LOCATION permission has now been granted. Requesting location.");
                if (parseImagesResponse != null) {
                    startApp();
                }
                locationPermissionDone = true;
            } else {
                L.i(TAG, "LOCATION permission was NOT granted.");
                Snackbar.make(vRoot, R.string.permission_location_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
                            }
                        }).show();

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onResponse(Response response, Request request) {
        parseImagesResponse = (ParseImagesResponse) response.data;
        if (locationPermissionDone) {
            Random r = new Random();
            r.setSeed(Calendar.getInstance().getTimeInMillis());
            imageIndex = r.nextInt(parseImagesResponse.results.size());
            // Todo cache background image at startup.
            startApp();
        }
    }

    @Override
    public void onErrorResponse(Request request, Exception e) {
        startApp();
//        ServiceErrorHandler serviceErrorHandler = new ServiceErrorHandler(true);
//        serviceErrorHandler.handle(request, e, requestController, new ServiceErrorHandler.DetailedErrorHandler() {
//            @Override
//            public void errorHandled(boolean forceQuit) {
//                finish();
//            }
//        });
    }
}
