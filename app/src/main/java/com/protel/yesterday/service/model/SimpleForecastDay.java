package com.protel.yesterday.service.model;

import java.io.Serializable;

/**
 * Created by erdemmac on 28/10/15.
 */
public class SimpleForecastDay implements Serializable {
    public DateInfo date;
    public TempInfoForecast high;
    public TempInfoForecast low;
    public String icon;
    public String conditions;
}
