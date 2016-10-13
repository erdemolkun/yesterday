package com.protel.yesterday.service;

import com.protel.network.Request;
import com.protel.yesterday.data.LatLng;
import com.protel.yesterday.service.response.ForecastResponse;
import com.protel.yesterday.service.response.GeoCoderResponse;
import com.protel.yesterday.service.response.HistoryResponse;
import com.protel.yesterday.util.LocalizationManager;
import com.protel.yesterday.util.WundergroundUtils;

import java.util.Calendar;

/**
 * Created by erdemmac on 21/11/15.
 */
public class RequestGenerator {

    private static Calendar getCacheInvalidationDate() {

        Calendar calendarCache = Calendar.getInstance();
        calendarCache.set(Calendar.HOUR_OF_DAY, 0);
        calendarCache.set(Calendar.MINUTE, 1);
        calendarCache.set(Calendar.MILLISECOND, 0);
        return calendarCache;
    }

    private static void setCacheValidation(Request request) {
        long dayBeginMilis = getCacheInvalidationDate().getTimeInMillis();
        long nowMilis = Calendar.getInstance().getTimeInMillis();
        long diffMilis = nowMilis - dayBeginMilis;
        request.expireAfter(Math.max(1, diffMilis));
    }

    private static String buildLang() {
        String lang = "lang:xy";
        if (LocalizationManager.getCurrentLang() == LocalizationManager.LANG_TR) {
            lang = "lang:TR";
        }
        return lang;
    }

    private static String buildLocationCacheCode(LatLng latLng) {
        return String.format("%.3f", latLng.latitude) + "," + String.format("%.3f", latLng.longitude);

    }

    private static String buildCoordinates(LatLng location) {
        return location.latitude + "," + location.longitude; //"41.06964474,29.00953055";
    }

    public static Request historyRequest(LatLng location) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String nameHistory = "history_";
        nameHistory += WundergroundUtils.buildDateForUrl(calendar);
        String coordinatesStr = buildCoordinates(location);
        nameHistory += "/" + buildLang() + "/q/" + coordinatesStr + ".json";

        Request requestHistory = new Request.Builder().id(ServiceConstants.HISTORY).name(nameHistory).build();

        requestHistory.setLoadingIndicatorPolicy(Request.FULL_CONTROL);
        requestHistory.setCachePolicy(Request.CACHE_POLICY_FETCH_AND_LEAVE);
        requestHistory.responseClassType = HistoryResponse.class;
        setCacheValidation(requestHistory);
        requestHistory.setCacheCode(buildLocationCacheCode(location));
        requestHistory.setIsCancelable(true);


        return requestHistory;
    }

    public static Request forecastRequest(LatLng location) {
        String nameForecast = "forecast/" + buildLang() + "/q/" + buildCoordinates(location) + ".json";

        Request requestForecast = new Request.Builder().id(ServiceConstants.FORECAST).name(nameForecast).build();
        requestForecast.setCachePolicy(Request.CACHE_POLICY_FETCH_AND_LEAVE);
        setCacheValidation(requestForecast);
        requestForecast.setLoadingIndicatorPolicy(Request.FULL_CONTROL);
        requestForecast.responseClassType = ForecastResponse.class;
        requestForecast.setCacheCode(buildLocationCacheCode(location));
        requestForecast.setIsCancelable(true);
        return requestForecast;
    }

    public static Request getGeoEncoderRequest(Double lat, Double lng) {
        //https://maps.googleapis.com/maps/api/geocode/json?latlng=41,41&key=AIzaSyAQzczbVbw2dJeCEI89ONC8DlLSWomw5WQ
        //https:maps.googleapis.com/maps/api/geocode/json?address=&key=AIzaSyAQzczbVbw2dJeCEI89ONC8DlLSWomw5WQ
        String formatted = "geocode/json?latlng=%s,%s&key=AIzaSyDWcXMfpRe5GVAbPk5pbYtTmOoMwC0EEy8";
        Request request = new Request.Builder().id(ServiceConstants.GEO_ENCODER).name(String.format(formatted, lat, lng)).build();
        request.baseUrl("https://maps.googleapis.com/maps/api/");
        request.setCachePolicy(Request.CACHE_POLICY_FETCH_AND_LEAVE);
        request.setCacheCode(buildLocationCacheCode(new LatLng(lat, lng)));
        request.responseClassType = GeoCoderResponse.class;
        return request;
    }


}
