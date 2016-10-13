package com.protel.yesterday.service.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by erdemmac on 28/10/15.
 */
public class HistoryInfo implements Serializable {
    public DateInfo date;
    public DateInfo utcdate;
    public ArrayList<Observation> observations;
//    public SummaryInfo dailysummary;
}
