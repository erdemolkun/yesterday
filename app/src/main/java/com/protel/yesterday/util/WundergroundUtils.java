package com.protel.yesterday.util;

import com.protel.yesterday.service.model.Observation;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by erdemmac on 04/11/15.
 */
public class WundergroundUtils {
    public static String mapIconIfNeeded(String iconName) {

        switch (iconName) {
            case "clear":
                return "sunny";
            case "mostlysunny":
                return "partlycloudy";
            case "sleet":
                return "snow";
            case "partlysunny":
                return "mostlycloudy";
            case "flurries":
                return "flurry";
            case "chanceflurries":
                return "chanceflurry";
            case "chancerain":
                return "chancerain";
            case "chancesleet":
                return "chancesleet";
            case "chancesnow":
                return "chancesnow";
            case "chancetstorms":
                return "chancestorms";
        }
        //fog , rain , hazy , cloudy , mostlycloudy , partlycloudy , unknown
        return iconName;
    }

    public static String buildDateForUrl(Calendar calendar) {
        String day = (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + calendar.get(Calendar.DAY_OF_MONTH);
        String month = ((calendar.get(Calendar.MONTH) + 1) < 10 ? "0" : "") + (calendar.get(Calendar.MONTH) + 1);
        return calendar.get(Calendar.YEAR) + "" + month + "" + day;
    }

    public static Observation getDayMax(ArrayList<Observation> observations) {
        int maxTemp = Integer.MIN_VALUE;
        Observation observationMax = null;
        for (Observation observation : observations) {
            int temp = DegreeUtils.getCelciusTemp(observation.tempi);
            if (temp > maxTemp) {
                maxTemp = temp;
                observationMax = observation;
            }
        }
        return observationMax;
    }

    public static Observation getDayMin(ArrayList<Observation> observations) {
        int minTemp = Integer.MAX_VALUE;
        Observation observationMin = null;
        for (Observation observation : observations) {
            int temp = DegreeUtils.getCelciusTemp(observation.tempi);
            if (temp < minTemp) {
                minTemp = temp;
                observationMin = observation;
            }
        }
        return observationMin;
    }

    public static Observation getObservationNow(ArrayList<Observation> observations) {
        Calendar calendarNow = Calendar.getInstance();
        int hourCurrent = calendarNow.get(Calendar.HOUR_OF_DAY);
        int index = (int) ((observations.size() / 24.0) * hourCurrent);
        if (index < 0) index = 0;
        if (index >= observations.size()) index = observations.size() - 1;
        return observations.get(index);
    }
}
