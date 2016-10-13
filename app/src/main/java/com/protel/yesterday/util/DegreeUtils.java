package com.protel.yesterday.util;

/**
 * Created by erdemmac on 30/10/15.
 */
public class DegreeUtils {


    public static double celciusToFahrenheit(double celcius) {
        return 32 + (celcius * 9.0 / 5.0);
    }

    public static double fahrenheitToCelcius(double fahrenheit) {
        double celcius = ((fahrenheit - 32) * 5) / 9;
        return celcius;
    }

    public static double doubleConversion(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0.0d;
        }

    }

    public static int getCelciusTemp(String value) {
        double fahTempi = Double.parseDouble(value);
        double celcTempi = DegreeUtils.fahrenheitToCelcius(fahTempi);
        return (int) celcTempi;
    }


}
