package com.snizl.android.api;

import android.content.ContentValues;
import android.os.AsyncTask;

import org.json.JSONObject;

public class GetStringDataTask extends AsyncTask<Void, Void, String> {
    private OnStringTaskCompleted listener;
    private String url;
    private ContentValues headers, body;
    private int method;

    public GetStringDataTask(String url, ContentValues headers, ContentValues body, int method, OnStringTaskCompleted listener) {
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.method = method;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        String result;
        result = Connect.getInstance().getStringResponse(url, headers, body, method);
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        listener.onStringDataReceived(result);
    }
}
