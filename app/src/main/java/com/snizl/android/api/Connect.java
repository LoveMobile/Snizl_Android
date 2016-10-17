package com.snizl.android.api;

import android.content.ContentValues;
import android.util.Log;

import com.snizl.android.AppConfig;
import com.snizl.android.utilities.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Set;

public class Connect implements HttpUrlManager, AppConfig {
    private static Connect mSharedInstance = null;

    public static Connect getInstance() {

        if (mSharedInstance == null) {
            mSharedInstance = new Connect();
        }
        return mSharedInstance;
    }

    public JSONObject getResponse(String strUrl, ContentValues headers, ContentValues body, int method) {
        JSONObject result = null;
        HttpURLConnection connection;

        Log.d(TAG, " *******************   Request   *******************");
        Log.d(TAG, "URL : "    + strUrl);
        Log.d(TAG, "Method : " + (method == 0 ? "POST" : (method == 1 ? "GET" : (method == 2 ? "PUT" : "DELETE"))));
        Log.d(TAG, "Header : " + (headers == null ? "NULL" : headers.toString()));
        Log.d(TAG, "Body : "   + (body    == null ? "NULL" : body.toString()));

        try {
            URL url = new URL(strUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(method == POST_METHOD ? "POST" : (method == GET_METHOD ? "GET" : (method == PUT_METHOD ? "PUT" : "DELETE")));
            connection.setRequestProperty(HEADER_ACCEPT, ACCEPT);
            connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE);

            if (headers != null) {
                Set<Entry<String, Object>> set = headers.valueSet();
                for (Object aSet : set) {
                    Entry entry = (Entry) aSet;
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();

                    if (value != null) {
                        connection.setRequestProperty(key, value.toString());
                    }
                }
            }

            if (body != null) {
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(body));
                writer.flush();
                writer.close();
                os.close();
            }

            connection.connect();

            int status = connection.getResponseCode();
            Log.d(TAG, "Status -> " + status);

            InputStream ins = null;

            if (status >= 200 && status < 300) {
                ins = connection.getInputStream();
            } else if (status >=400 && status < 500) {
                ins = connection.getErrorStream();
            }

            if (ins != null) {
                result = convertInputStreamToJSON(ins);
                ins.close();
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        Log.d(TAG, " *******************   Response   *******************");
        Log.d(TAG, result == null ? "NULL" : result.toString());

        return result;
    }

    public String getStringResponse(String strUrl, ContentValues headers, ContentValues body, int method) {
        String result = null;
        HttpURLConnection connection;

        Log.d(TAG, " *******************   Request   *******************");
        Log.d(TAG, "URL : "    + strUrl);
        Log.d(TAG, "Method : " + (method == 0 ? "POST" : (method == 1 ? "GET" : (method == 2 ? "PUT" : "DELETE"))));
        Log.d(TAG, "Header : " + (headers == null ? "NULL" : headers.toString()));
        Log.d(TAG, "Body : "   + (body    == null ? "NULL" : body.toString()));

        try {
            URL url = new URL(strUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(method == POST_METHOD ? "POST" : (method == GET_METHOD ? "GET" : (method == PUT_METHOD ? "PUT" : "DELETE")));
            connection.setRequestProperty(HEADER_ACCEPT, ACCEPT);
            connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE);

            if (headers != null) {
                Set<Entry<String, Object>> set = headers.valueSet();
                for (Object aSet : set) {
                    Entry entry = (Entry) aSet;
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();

                    if (value != null) {
                        connection.setRequestProperty(key, value.toString());
                    }
                }
            }

            if (body != null) {
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(body));
                writer.flush();
                writer.close();
                os.close();
            }

            connection.connect();

            int status = connection.getResponseCode();
            Log.d(TAG, "Status -> " + status);

            InputStream ins = connection.getInputStream();
            if(ins != null) {
                result = convertInputStreamToString(ins);
                ins.close();
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        Log.d(TAG, " *******************   Response   *******************");
        Log.d(TAG, result == null ? "NULL" : result);

        return result;
    }

    private JSONObject convertInputStreamToJSON(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        JSONObject jObject= null;

        if (result.isEmpty()) return null;

        try {
            jObject = new JSONObject(result);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return jObject;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        return result;
    }

    private String getQuery(ContentValues body) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Set<Entry<String, Object>> set = body.valueSet();
        for (Object aSet : set) {
            if (first)
                first = false;
            else
                result.append("&");

            Entry entry = (Entry) aSet;
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value != null) {
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
            }
        }

        return result.toString();
    }
}
