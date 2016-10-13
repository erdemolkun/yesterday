package com.protel.yesterday.service.model;

import java.io.Serializable;

/**
 * Created by erdemmac on 28/10/15.
 */
public class Observation implements Serializable {
    public DateInfo date, utcdate;
    public String tempm;
    public String tempi;
    public String dewptm;
    public String dewpti;
    public String hum;
    public String icon;
    public String conds;
}
