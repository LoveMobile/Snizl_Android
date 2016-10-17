package com.snizl.android.api;

import android.content.ContentValues;
import android.os.AsyncTask;

import org.json.JSONObject;

public class GetDataTask extends AsyncTask<Void, Void, JSONObject> implements HttpUrlManager{
    private OnTaskCompleted listener;
    private String url;
    private ContentValues headers, body;
    private int method;

    public GetDataTask(String url, ContentValues headers, ContentValues body, int method,  OnTaskCompleted listener) {
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.method = method;
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONObject result;
        result = Connect.getInstance().getResponse(url, headers, body, method);
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        listener.onDataReceived(result);
    }
}
