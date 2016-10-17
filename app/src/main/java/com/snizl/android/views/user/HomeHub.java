package com.snizl.android.views.user;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;
import com.snizl.android.views.main.Main;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeHub extends BaseActivity implements OnTaskCompleted{
    private final int GET_HUBS_REQUEST_CODE         = 0;
    private final int GET_DETECTED_HUB_REQUEST_CODE = 1;
    private final int PUT_USER_HUB_REQUEST_CODE     = 2;

    @BindView(R.id.cv_detected_hub)      CardView cvDetectedHub;
    @BindView(R.id.tv_detected_hub_name) TextView tvDetectedHubName;
    @BindView(R.id.sp_hubs)              Spinner spHubs;

    @OnClick(R.id.iv_back)
    public void onBack() {
        if (isLoadingBase) return;

        Class c = null;

        switch (fromWhere) {
            case SIGN_IN:
                c = Login.class;
                break;
            case SIGN_UP:
                c = Register.class;
                break;
            case SETTING:
                c = Setting.class;
                break;
        }

        if (c != null)
            navigationWithFinish(this, c);
    }

    @OnClick(R.id.btn_yes)
    public void onYes() {
        long hub_id  = commonUtils.getLongFromJSONObject(detectedHub, ID);

        updateCurrentHub(hub_id);
    }

    @OnClick(R.id.btn_continue)
    public void onContinue() {
        int position = spHubs.getSelectedItemPosition();
        JSONObject selectedHub = commonUtils.getJSONObjectFromJSONArray(hubs, position);
        long hub_id = commonUtils.getLongFromJSONObject(selectedHub, ID);

        updateCurrentHub(hub_id);
    }


//    *******************************************************************************************
    private int request_code;
    private JSONArray hubs;
    private JSONObject detectedHub;
    private String fromWhere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_hub);

        ButterKnife.bind(this);

        initUI();
        initData();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void initUI() {
        cvDetectedHub.setVisibility(View.GONE);
    }

    private void initData() {
        fromWhere = getIntent().getStringExtra(FROM);
        getHubs();
    }

    private void initSpinner() {
        int position = 0;
        ArrayList<String> hubNames = new ArrayList<>();

        for (int i = 0; i < hubs.length(); i++) {
            hubNames.add(commonUtils.getStringFromJSONObject(commonUtils.getJSONObjectFromJSONArray(hubs, i), NAME));

            if (detectedHub != null) {
                long id = commonUtils.getLongFromJSONObject(commonUtils.getJSONObjectFromJSONArray(hubs, i), ID);
                long detectedId = commonUtils.getLongFromJSONObject(detectedHub, ID);

                if (id == detectedId) {
                    position = i;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_hub_item, hubNames);
        adapter.setDropDownViewResource(R.layout.spinner_hub_item);
        spHubs.setAdapter(adapter);
        spHubs.setSelection(position);
    }

    private void initDetectedHubContainer() {
        String name = " " + commonUtils.getStringFromJSONObject(detectedHub, NAME) + " ";
        tvDetectedHubName.setText(name);
        cvDetectedHub.setVisibility(View.VISIBLE);
    }

    private void getHubs() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_HUBS_REQUEST_CODE;

        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Finding Local Hubs");
        mProgressDialog.show();

        mGetDataTask = new GetDataTask(HUBS_URL, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void getDetectedHub() {
        if (AppController.mLocation == null) {
            startLocationTracker();
            return;
        }

        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        double latitude  = AppController.mLocation.getLatitude();
        double longitude = AppController.mLocation.getLongitude();

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        ContentValues body = new ContentValues();
        body.put(BODY_RANGE, 50);
        body.put(BODY_LAT,   latitude);
        body.put(BODY_LONG,  longitude);

        isLoadingBase = true;
        request_code = GET_DETECTED_HUB_REQUEST_CODE;
        String url = API_URL_GET_CLOSEST_HUB + "?" + commonUtils.getQuery(body);

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void updateCurrentHub(long hub_id) {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        long user_id = getUserId();

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = PUT_USER_HUB_REQUEST_CODE;

        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Updating current Hub");
        mProgressDialog.show();

        String url = API_URL_PUT_USER_HUB.replace("user_id", String.valueOf(user_id)).replace("hub_id", String.valueOf(hub_id));

        mGetDataTask = new GetDataTask(url, headers, null, PUT_METHOD, this);
        mGetDataTask.execute();
    }


    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (response == null) {
            commonUtils.showAlertDialog(this, getString(R.string.no_internet_connection));
            mProgressDialog.dismiss();
            return;
        }

        if (request_code == GET_HUBS_REQUEST_CODE) {
            hubs = commonUtils.getJSONArrayFromJSONObject(response, HUBS);
            initSpinner();
            getDetectedHub();
        } else if (request_code == GET_DETECTED_HUB_REQUEST_CODE) {
            mProgressDialog.dismiss();
            detectedHub = commonUtils.getJSONObjectFromJSONObject(response, HUB);

            if (detectedHub == null) {
                commonUtils.showAlertDialog(this, "Sorry, no hubs found near you");
            } else {
                initDetectedHubContainer();
            }

            initSpinner();
        } else if (request_code == PUT_USER_HUB_REQUEST_CODE) {
            mProgressDialog.dismiss();
            AppController.updatedHub = true;

            JSONObject currentUser = commonUtils.getJSONObjectFromJSONObject(response, USER);
            AppController.currentUser = currentUser;
            commonUtils.setJSONObjectToSharedPreference(this, USER, currentUser);

            if (fromWhere.equals(SIGN_IN) || fromWhere.equals(SIGN_UP)) {
                navigationWithFinish(this, Main.class);
            } else {
                finish();
            }
        }
    }

    private void startLocationTracker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        } else {
            getDetectedHub();
        }
    }
}
