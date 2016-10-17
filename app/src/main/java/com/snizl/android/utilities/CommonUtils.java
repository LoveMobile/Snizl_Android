package com.snizl.android.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.snizl.android.AppConfig;
import com.snizl.android.AppController;
import com.snizl.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils implements AppConfig {
    private static CommonUtils commonUtils = null;

    public static CommonUtils getInstance() {
        if (commonUtils == null) {
            commonUtils = new CommonUtils();
        }

        return commonUtils;
    }

    // SharedPreference
    public void setIntToSharedPreference(Context context, String key, int val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public int getIntFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public void setStringToSharedPreference(Context context, String key, String val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public String getStringFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public void setLongToSharedPreference(Context context, String key, long val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, val);
        editor.apply();
    }

    public long getLongFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, -1);
    }

    public void setBooleanToSharedPreference(Context context, String key, boolean val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public boolean getBooleanFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public void setJSONObjectToSharedPreference(Context context, String key, JSONObject val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val == null ? null : val.toString());
        editor.apply();
    }

    public JSONObject getJSONObjectFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        String s = sharedPreferences.getString(key, null);
        JSONObject result = null;
        if (s != null) {
            try {
                result = new JSONObject(s);
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return result;
    }

    public void setJSONArrayToSharedPreference(Context context, String key, JSONArray val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val == null ? null : val.toString());
        editor.apply();
    }

    public JSONArray getJSONArrayFromSharedPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        String s = sharedPreferences.getString(key, null);
        JSONArray result = null;
        if (s != null) {
            try {
                result = new JSONArray(s);
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return result;
    }

//    ***************************************** JSON *****************************************
//    JSONArray
    public JSONArray addJSONObjectToJSONArray(JSONArray jsonArray, JSONObject jsonObject) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }

        jsonArray.put(jsonObject);

        return jsonArray;
    }

    public JSONArray addJSONArrayToJSONArray(JSONArray jsonArray, JSONArray jArray) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }

        for (int i = 0; i < jArray.length(); i++) {
            jsonArray.put(getJSONObjectFromJSONArray(jArray, i));
        }

        return jsonArray;
    }

    public JSONArray removeJSONObjectToJSONArray(JSONArray jsonArray, int index) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i != index) {
                try {
                    result.put(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        return result;
    }

    public boolean isExistInJSONArray(JSONArray jsonArray, String key, Object val) {
        boolean result = false;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObjectFromJSONArray(jsonArray, i);
            Object object = getObjectFromJSONObject(jsonObject, key);

            if (object.toString().equals(val.toString())) {
                result = true;
                break;
            }
        }

        return result;
    }

//    JSONObject
    public JSONObject getJSONObjectFromJSONObject(JSONObject jsonObject, String key) {
        JSONObject result = null;

        try {
            result = jsonObject.getJSONObject(key);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public JSONArray getJSONArrayFromJSONObject(JSONObject jsonObject, String key) {
        JSONArray result = null;

        try {
            result = jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public String getStringFromJSONObject(JSONObject jsonObject, String key) {
        String result = null;
        try {
            result = jsonObject.getString(key);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public int getIntFromJSONObject(JSONObject jsonObject, String key) {
        int result = -1;
        try {
            result = jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public float getFloatFromJSONObject(JSONObject jsonObject, String key) {
        float result = -1;
        try {
            result = (float) jsonObject.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public double getDoubleFromJSONObject(JSONObject jsonObject, String key) {
        double result = -1;
        try {
            result = jsonObject.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public long getLongFromJSONObject(JSONObject jsonObject, String key) {
        long result = -1;
        try {
            result = jsonObject.getLong(key);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean getBooleanFromJSONObject(JSONObject jsonObject, String key) {
        boolean result = false;
        try {
            result = jsonObject.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Object getObjectFromJSONObject(JSONObject jsonObject, String key) {
        Object result = null;
        try {
            result = jsonObject.get(key);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        return result;
    }

    public JSONObject getJSONObjectFromJSONArray(JSONArray jsonArray, int index) {
        JSONObject result = null;
        try {
            result = jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean isUserLoggedIn(Context context) {
        JSONObject jsonObject = getJSONObjectFromSharedPreference(context, USER);
        if (jsonObject != null) {
            AppController.currentUser = jsonObject;
            AppController.oauth_token = getJSONObjectFromSharedPreference(context, OAUTH_TOKEN);
            AppController.businesses  = getJSONArrayFromSharedPreference (context, BUSINESSES);
            AppController.wallet      = getJSONArrayFromSharedPreference (context, WALLET);

            return true;
        } else {
            return false;
        }
    }

    public void logOut(Context context) {
        setJSONObjectToSharedPreference(context, USER,        null);
        setJSONObjectToSharedPreference(context, OAUTH_TOKEN, null);
        setJSONObjectToSharedPreference(context, BUSINESSES,  null);
        setJSONObjectToSharedPreference(context, WALLET,      null);
    }
//
//    public boolean isUserLoggedOut(Context context) {
//        return getBooleanFromSharedPreference(context, LOGGED_OUT);
//    }

//    AlertDialog with delay
    public AlertDialog showAlertDialog(Context context, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setCancelable(false);
        TextView alertMsg = new TextView(context);
        alertMsg.setText(message);
        alertMsg.setTextSize(18);
        alertMsg.setPadding(16, 64, 16, 64);
        alertMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        dialog.setView(alertMsg);

        final AlertDialog alert = dialog.create();
        alert.show();

        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    alert.dismiss();
                }
            }
        };

        handler.postDelayed(runnable, ALERT_TIME);

        return alert;
    }

    public void setupUI(View view, final Activity activity) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    private void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public Boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public String md5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    public void fadeInView(View view, long duration) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    public void fadeOutView(View view, long duration) {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    public void openUrlInBrowser(Context context, String url) {
        try {
            if(!url.contains("http://") && !url.contains("https://"))
                url = "http://" + url;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        }
        catch(ActivityNotFoundException e) {
            showAlertDialog(context, "Sorry, something went wrong");
        }
    }

    public String getQuery(ContentValues body){
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Set<Map.Entry<String, Object>> set = body.valueSet();
        for (Object aSet : set) {
            if (first)
                first = false;
            else
                result.append("&");

            Map.Entry entry = (Map.Entry) aSet;
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value != null) {
                result.append(key);
                result.append("=");
                result.append(value.toString());
            }
        }

        return result.toString();
    }

//    Format Date
    public String calcDifferenceDate(String strDate, int type) {
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
        try {
            Date current = new Date();
            Date date = format.parse(strDate);

            long different;

            if (current.after(date)) {
                different = current.getTime() - date.getTime();
            } else {
                different = date.getTime() - current.getTime() ;
            }

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            if (elapsedDays > 0) {
                result = elapsedDays + " day" + (elapsedDays > 1 ? "s" : "");
            } else if (elapsedHours > 0) {
                result = elapsedHours + " hour" + (elapsedHours > 1 ? "s" : "");
            } else if (elapsedMinutes > 0) {
                result = elapsedMinutes + " minute" + (elapsedMinutes > 1 ? "s" : "");
            } else if (elapsedSeconds > 0) {
                result = elapsedSeconds + " second" + (elapsedSeconds > 1 ? "s" : "");
            }

            switch (type) {
                case 1:
                    result = "Posted " + result + " ago";
                    break;
                case 2:
                    result = "Only " + result + " left";
                    break;
                case 3:
                    result = "Ended " + result + " ago";
                    break;
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public String convertDateFormat(String strDate) {
        String result = "";
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
        SimpleDateFormat outFormat = new SimpleDateFormat("MMM dd h:mm a", Locale.UK);

        try {
            Date date = inFormat.parse(strDate);
            result = outFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public String convertDateFormat(String strStartDate, String strEndDate) {
        String result = "";
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
        SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM", Locale.UK);

        try {
            Date startDate = inFormat.parse(strStartDate);
            Date endDate = inFormat.parse(strEndDate);
            result = outFormat.format(startDate) + " - " + outFormat.format(endDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public boolean isEnded(String strEndDate) {
        boolean result = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
        try {
            Date current = new Date();
            Date endDate = format.parse(strEndDate);

            result = current.after(endDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public void setTransparencyToView(View v, float ratio) {
        v.getBackground().setAlpha((int)(ratio * 255));
    }

    public void changeViewColor(View view, boolean enabled, Context context) {

        if (view instanceof Button) {
            Drawable[] drawables = ((Button) view).getCompoundDrawables();

            for (Drawable drawable : drawables) {
                if (drawable != null)
                    drawable.setColorFilter(ContextCompat.getColor(context, enabled ? R.color.colorPrimary : R.color.colorGrayDark), PorterDuff.Mode.SRC_ATOP);
            }

            ((Button) view).setTextColor(ContextCompat.getColor(context, enabled ? R.color.colorPrimary : R.color.colorGrayDark));
        } else if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();

            for (Drawable drawable : drawables) {
                if (drawable != null)
                    drawable.setColorFilter(ContextCompat.getColor(context, enabled ? R.color.colorPrimary : R.color.colorGrayDark), PorterDuff.Mode.SRC_ATOP);
            }

            ((TextView) view).setTextColor(ContextCompat.getColor(context, enabled ? R.color.colorPrimary : R.color.colorGrayDark));
        }
    }

    public void changeActionButtonStateColor(Button button, boolean selected, Context context) {
        Drawable[] drawables = button.getCompoundDrawables();

        for (Drawable drawable : drawables) {
            if (drawable != null)
                drawable.setColorFilter(ContextCompat.getColor(context, selected ? android.R.color.white : R.color.colorGrayDark), PorterDuff.Mode.SRC_ATOP);
        }

        button.setTextColor(ContextCompat.getColor(context, selected ? android.R.color.white : R.color.colorGrayDark));
        button.setBackgroundColor(ContextCompat.getColor(context, selected ? R.color.colorPrimary : R.color.colorGrayLight));
    }
}
