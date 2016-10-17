package com.snizl.android.api;

import org.json.JSONObject;

public interface OnTaskCompleted {
    void onDataReceived(JSONObject response);
}
