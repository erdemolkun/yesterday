package com.protel.yesterday.service.response;

import com.protel.yesterday.service.model.HistoryInfo;
import com.protel.yesterday.service.model.ResponseInfo;

import java.io.Serializable;

/**
 * Created by erdemmac on 28/10/15.
 */
public class HistoryResponse implements Serializable {
    public ResponseInfo response;
    public HistoryInfo history;
}
