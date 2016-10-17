package com.snizl.android;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;

public interface AppConfig {
    String TAG = "snizl_app";
    String APP_NAME = "Snizl";

//    Navigation Control
    String FROM    = "from";
    String SIGN_IN = "sign_in";
    String SIGN_UP = "sign_up";
    String SETTING = "setting";

//    SharedPreferences Key
    String OAUTH_TOKEN = "oauth_token";

//    Response Key
    String ACCESS_TOKEN = "access_token";
    String TOKEN_TYPE   = "token_type";
    String STATUS_CODE  = "status_code";
    String MESSAGE      = "message";
    String ERROR        = "error";
    String ERROR_DESCRIPTION = "error_description";

    String USER = "user";
    String FIRST_NAME = "first_name";
    String LAST_NAME  = "last_name";
    String EMAIL      = "email";
    String PICTURE = "picture";
    String FEED     = "feed";
    String DATES    = "dates";
    String KEY_URL = "url";
    String RANGE = "range";

    String ID = "id";
    String BUSINESSES  = "businesses";
    String BUSINESS    = "business";
    String ADDRESS     = "address";
    String CLAIM       = "claim";
    String CLAIM_WALK_IN = "claim_walk_in";
    String CLAIM_CALL_UP = "claim_call_up";
    String CLAIM_WEBSITE = "claim_website";
    String CLAIM_SPECIAL = "claim_special";
    String CALL_IN     = "call_in";
    String CALL_UP     = "call_up";
    String WEBSITE     = "website";
    String SPECIAL     = "special";
    String ENABLED     = "enabled";
    String LOGO        = "logo";
    String FEATUREIMAGE = "featureimage";
    String NAME        = "name";
    String TITLE       = "title";
    String DESCRIPTION = "description";
    String TIME        = "time";
    String DAYS        = "days";
    String HOURS       = "hours";
    String MINUTES     = "minutes";
    String FORMATTED   = "formatted";
    String TELEPHONE   = "telephone";
    String FOLLOWING   = "following";
    String TYPE        = "type";
    String START       = "start";
    String END         = "end";
    String HUB         = "hub";
    String HUBS        = "hubs";
    String CATEGORIES  = "categories";

    String WALLET = "wallet";

    String OFFER  = "offer";
    String OFFERS = "offers";
    String EVENT  = "event";
    String EVENTS = "events";
    String META   = "meta";
    String PAGINATION = "pagination";
    String TOTAL_PAGES  = "total_pages";
    String CURRENT_PAGE = "current_page";

    String LINKS  = "links";
    String NEXT   = "next";


//    Duration
    long FADE_DURATION = 500;
    long ALERT_TIME = 2000;

//    Picture Size
    int PICTURE_MAX_WIDTH  = 512;
    int PICTURE_MAX_HEIGHT = 512;

//    Permission Request Code
    int PERMISSIONS_REQUEST_CALL_PHONE       = 0;
    int PERMISSIONS_REQUEST_CAMERA           = 1;
    int PERMISSIONS_REQUEST_CAMERA_FOR_IMAGE = 2;
    int PERMISSIONS_REQUEST_CAMERA_FOR_VIDEO = 3;
    int PERMISSIONS_REQUEST_LOCATION         = 4;
}
