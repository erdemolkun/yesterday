package com.protel.yesterday.service.response;

import com.protel.yesterday.service.model.ForecastInfo;
import com.protel.yesterday.service.model.ResponseInfo;

import java.io.Serializable;

/**
 * Created by erdemmac on 28/10/15.
 */
public class ForecastResponse implements Serializable {
    public ResponseInfo response;
    public ForecastInfo forecast;
}
