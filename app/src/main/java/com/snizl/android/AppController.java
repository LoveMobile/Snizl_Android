package com.snizl.android;

import android.location.Location;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class AppController {
    public static Location mLocation = null;

    public static JSONObject oauth_token = null;
    public static JSONObject currentUser = null;
    public static JSONArray  businesses  = null;
    public static JSONArray  wallet      = null;
    public static JSONArray  offers      = null;

    public static boolean    updatedHub  = false;

//    Add Deal or Promo
    public static int add_type;
    public static int seleted_business_index;
    public static Uri selected_media_uri;
    public static String title, description;
    public static Calendar date;
    public static Time time;
    public static int days, hours, minutes;
    public static String strInstore, strWebsite, strPhone, strCustom;

}
