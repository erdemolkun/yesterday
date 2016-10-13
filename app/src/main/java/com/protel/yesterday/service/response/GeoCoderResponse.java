package com.protel.yesterday.service.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by erdemmac on 19/12/15.
 */
public class GeoCoderResponse implements Serializable {
    public String status;
    public ArrayList<Component> results;

    public static class Component implements Serializable {
        public ArrayList<AddressComponent> address_components;
        public String formatted_address;
    }

    public static class AddressComponent implements Serializable {
        public String long_name;
        public String short_name;
        public ArrayList<String> types;
    }
}
