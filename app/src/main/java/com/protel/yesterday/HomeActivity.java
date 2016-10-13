package com.protel.yesterday;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.protel.network.Request;
import com.protel.network.RequestController;
import com.protel.network.Response;
import com.protel.network.interfaces.ResponseListener;
import com.protel.yesterday.data.AppData;
import com.protel.yesterday.data.LatLng;
import com.protel.yesterday.service.RequestGenerator;
import com.protel.yesterday.service.ServiceConstants;
import com.protel.yesterday.service.ServiceErrorHandler;
import com.protel.yesterday.service.model.Observation;
import com.protel.yesterday.service.model.SimpleForecastDay;
import com.protel.yesterday.service.response.ForecastResponse;
import com.protel.yesterday.service.response.GeoCoderResponse;
import com.protel.yesterday.service.response.HistoryResponse;
import com.protel.yesterday.service.response.ParseImagesResponse;
import com.protel.yesterday.ui.WeatherView;
import com.protel.yesterday.util.ActivityAnimations;
import com.protel.yesterday.util.DegreeUtils;
import com.protel.yesterday.util.L;
import com.protel.yesterday.util.LocalizationManager;
import com.protel.yesterday.util.LocationManager;
import com.protel.yesterday.util.WundergroundUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class HomeActivity extends AppCompatActivity implements ResponseListener, ActivityCompat.OnRequestPermissionsResultCallback {


    public static final String EXTRA_PARSE_IMAGES = "EXTRA_PARSE_IMAGES";
    public static final String EXTRA_PARSE_IMAGE_INDEX = "EXTRA_PARSE_IMAGE_INDEX";

    private static final int MIN_SNACKBAR_DURATION = 700;

    private ParseImagesResponse parseImagesResponse;
    private int parseImageIndex = -1;
    HistoryResponse historyResponse;
    ForecastResponse forecastResponse;

    private RequestController requestController;

    private WeatherView vTodayRow, vYesterdayRow, vTomorrow;
    private ImageView ivRefresh;
    private TextView tvAddress;


    /**
     * Root of the layout of this Activity.
     */

    private Snackbar snackbarWaitingGps;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        ActivityAnimations.doAnimation(this, ActivityAnimations.FADE_IN, true);
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_PARSE_IMAGES)) {
                parseImagesResponse = (ParseImagesResponse) getIntent().getExtras().getSerializable(EXTRA_PARSE_IMAGES);
            }
            if (bundle.containsKey(EXTRA_PARSE_IMAGE_INDEX)) {
                parseImageIndex = getIntent().getExtras().getInt(EXTRA_PARSE_IMAGE_INDEX, -1);
            }
        }

        setContentView(R.layout.activity_home);
        changeBackground();

        ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        vTodayRow = (WeatherView) findViewById(R.id.v_weather_today);
        vYesterdayRow = (WeatherView) findViewById(R.id.v_weather_yesterday);
        vTomorrow = (WeatherView) findViewById(R.id.v_weather_tomorrow);

        requestController = new RequestController(new RequestController.CancelCallback() {
            @Override
            public void onCanceled(ArrayList<Request> canceledRequests) {
                finish();
            }
        });
        vTodayRow.setInfo(getString(R.string.today));
        vYesterdayRow.setInfo(getString(R.string.yesterday));
        vTomorrow.setInfo(getString(R.string.tomorrow));


        vYesterdayRow.addFancyAnim();
        vTodayRow.addFancyAnim();
        vTomorrow.addFancyAnim();
        refreshConversionUI();
        findViewById(R.id.tv_degree_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppData.flipType();
                refreshViews();
                refreshConversionUI();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void refreshConversionUI() {
        if (AppData.isFahrenheit()) {
            ((TextView) findViewById(R.id.tv_degree_type)).setText(getString(R.string.degree_sign) + "C");
        } else {
            ((TextView) findViewById(R.id.tv_degree_type)).setText(getString(R.string.degree_sign) + "F");
        }
    }

    private void changeBackground() {
        int index = -1;
        if (parseImageIndex < 0) {
            Random r = new Random();
            r.setSeed(Calendar.getInstance().getTimeInMillis());
            if (parseImagesResponse != null) {
                index = r.nextInt(parseImagesResponse.results.size());
            }
        } else {
            index = parseImageIndex;
        }
        if (index >= 0) {
            Glide.with(this).load(parseImagesResponse.results.get(index).file_name.url).diskCacheStrategy(DiskCacheStrategy.ALL).animate(R.anim.fade_in).
                    into((ImageView) findViewById(R.id.iv_home_root));
        }

    }


    private LocationManager locationManager;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        YesterdayApp.getInstance().sendTestAnalytics(this);
        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.start(true);
            }
        });
        locationManager = new LocationManager(this, true, new LocationManager.LocationCallback() {
            @Override
            public void onGotLocation(LatLng latLng) {
                ivRefresh.setEnabled(true);
                long gpsWatingDiff = System.currentTimeMillis() - waitingGpsStartTime;
                if (gpsWatingDiff > MIN_SNACKBAR_DURATION) {
                    if (snackbarWaitingGps != null) snackbarWaitingGps.dismiss();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (snackbarWaitingGps != null) snackbarWaitingGps.dismiss();
                        }
                    }, MIN_SNACKBAR_DURATION - gpsWatingDiff);
                }
                callWeatherRequests(false);
            }

            @Override
            public void onLocationFetchError() {
                if (!locationManager.isRefresh()) {
                    finishWithMessage(null);
                } else {
                    ivRefresh.setEnabled(true);
                }
            }

            @Override
            public void onPrepare() {
                ivRefresh.setEnabled(false);
            }
        });
        locationManager.start();
        snackbarWaitingGps = Snackbar.make(findViewById(android.R.id.content), R.string.waiting_location_info, Snackbar.LENGTH_INDEFINITE);
        snackbarWaitingGps.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.refresh_progress_1));
        snackbarWaitingGps.show();
        waitingGpsStartTime = System.currentTimeMillis();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onResponse(Response response, Request request) {
        if (request.getID() == ServiceConstants.HISTORY) {
            historyResponse = (HistoryResponse) response.data;
            refreshViews(response.fetchType == Response.DISK_CACHE);
        } else if (request.getID() == ServiceConstants.FORECAST) {
            forecastResponse = (ForecastResponse) response.data;
            refreshViews(response.fetchType == Response.DISK_CACHE);
        } else if (request.getID() == ServiceConstants.GEO_ENCODER) {
            GeoCoderResponse geoCoderResponse = (GeoCoderResponse) response.data;
            if (geoCoderResponse == null) return;
            if (geoCoderResponse.results != null && geoCoderResponse.results.size() > 0) {
                tvAddress.setText(geoCoderResponse.results.get(0).formatted_address);
            }
        }
    }

    private void refreshViews() {
        if (historyResponse != null) {
            Observation observationMax = WundergroundUtils.getDayMax(historyResponse.history.observations);
            Observation observationMin = WundergroundUtils.getDayMin(historyResponse.history.observations);
            int dayMax = DegreeUtils.getCelciusTemp(observationMax.tempi);
            int dayMin = DegreeUtils.getCelciusTemp(observationMin.tempi);
            int dayNow = DegreeUtils.getCelciusTemp(WundergroundUtils.getObservationNow(historyResponse.history.observations).tempi);
            String weatherInfo = " - ";
            if (LocalizationManager.getCurrentLang() == LocalizationManager.LANG_TR) {
                int resid = getResources().getIdentifier(observationMax.icon, "string", getPackageName());
                if (resid != 0) {
                    weatherInfo = getString(resid);
                }
            } else {
                weatherInfo = observationMax.conds;
            }
            vYesterdayRow.setInfo(dayMin, dayMax, dayNow, true, observationMax.icon, weatherInfo);
        }
        if (forecastResponse != null) {
            SimpleForecastDay simpleForecastToday = forecastResponse.forecast.simpleforecast.forecastday.get(0);
            SimpleForecastDay simpleForecastTomorrow = forecastResponse.forecast.simpleforecast.forecastday.get(1);

            vTodayRow.setInfo(DegreeUtils.doubleConversion(simpleForecastToday.low.celsius),
                    DegreeUtils.doubleConversion(simpleForecastToday.high.celsius),
                    DegreeUtils.doubleConversion(simpleForecastToday.high.celsius), true, simpleForecastToday.icon,
                    simpleForecastTomorrow.conditions);

            vTomorrow.setInfo(DegreeUtils.doubleConversion(simpleForecastTomorrow.low.celsius),
                    DegreeUtils.doubleConversion(simpleForecastTomorrow.high.celsius),
                    DegreeUtils.doubleConversion(simpleForecastTomorrow.high.celsius), true,
                    simpleForecastTomorrow.icon, simpleForecastTomorrow.conditions);
        }
    }

    private void refreshViews(boolean isCacheResult) {

        refreshViews();
        if (historyResponse != null && forecastResponse != null) {

            findViewById(R.id.v_home_content).setVisibility(View.VISIBLE);
            if (!isCacheResult) {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
                findViewById(R.id.v_home_content).startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                vYesterdayRow.applyFancyAnim();
                                vTodayRow.applyFancyAnim();
                                vTomorrow.applyFancyAnim();
                            }
                        }, 200);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vYesterdayRow.applyFancyAnim();
                        vTodayRow.applyFancyAnim();
                        vTomorrow.applyFancyAnim();
                    }
                }, 320);
            }
        }
    }


    @Override
    public void onErrorResponse(Request request, Exception e) {
        ServiceErrorHandler serviceErrorHandler = new ServiceErrorHandler(false);
        serviceErrorHandler.handle(request, e, requestController, new ServiceErrorHandler.DetailedErrorHandler() {
            @Override
            public void errorHandled(boolean forceQuit) {
                finish();
            }
        });
    }


    private long waitingGpsStartTime = 0;


    private void finishWithMessage(String message) {
        if (message == null) {
            message = getString(R.string.access_location_rationale);
        }
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void finish() {
        requestController.cancel();
        super.finish();
    }

    private void callWeatherRequests(boolean isSwipe) {

        Request requestHistory = RequestGenerator.historyRequest(locationManager.getLatLng());
        requestHistory.setLoadingIndicatorPolicy(isSwipe ? Request.NO_LOADING : Request.FULL_CONTROL);
        requestHistory.setCachePolicy(isSwipe ? Request.CACHE_POLICY_NONE : Request.CACHE_POLICY_FETCH_AND_LEAVE);
        requestController.addToMainQueue(requestHistory, this);


        Request requestForecast = RequestGenerator.forecastRequest(locationManager.getLatLng());
        requestForecast.setCachePolicy(isSwipe ? Request.CACHE_POLICY_NONE : Request.CACHE_POLICY_FETCH_AND_LEAVE);
        requestForecast.setLoadingIndicatorPolicy(isSwipe ? Request.NO_LOADING : Request.FULL_CONTROL);
        requestController.addToMainQueue(requestForecast, this);

        tvAddress.setText("");
        Request requestAddress = RequestGenerator.getGeoEncoderRequest(locationManager.getLatLng().latitude, locationManager.getLatLng().longitude);
        requestAddress.noLoading();
        requestController.cancel(ServiceConstants.GEO_ENCODER);
        requestController.addToMainQueue(requestAddress, this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locationManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        locationManager.onDestroy();
        super.onDestroy();
    }

    private Uri baseUri;

    private Uri getBaseUri() {
        if (baseUri == null) {
            String packageName = getApplicationContext().getPackageName();
            baseUri = Uri.parse("android-app://" + packageName + "/myapp/");
        }
        return baseUri;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Yesterday Weather App",
                null,
                getBaseUri()
        );
        PendingResult<Status> result = AppIndex.AppIndexApi.start(client, viewAction);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    L.d(TAG, "App Indexing API: Recorded  view end successfully.");
                } else {
                    L.e(TAG, "App Indexing API: There was an error recording the recipe view."
                            + status.toString());
                }
            }
        });
    }

    String TAG = "HomeActivity";

    @Override
    public void onStop() {
        super.onStop();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Yesterday Weather App",
                null,
                getBaseUri()
        );
        PendingResult<Status> result = AppIndex.AppIndexApi.end(client, viewAction);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    L.d(TAG, "App Indexing API: Recorded  view end successfully.");
                } else {
                    L.e(TAG, "App Indexing API: There was an error recording the recipe view."
                            + status.toString());
                }
            }
        });
        client.disconnect();
    }
}
