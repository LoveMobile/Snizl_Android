package com.snizl.android.views.user;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;
import com.snizl.android.views.user.Login;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Setting extends BaseActivity implements OnTaskCompleted {
    @BindView(R.id.et_first_name) EditText etFirstName;
    @BindView(R.id.et_last_name)  EditText etLastName;
    @BindView(R.id.et_email)      EditText etEmail;
    @BindView(R.id.et_password)   EditText etPassword;

    @OnClick(R.id.iv_back)
    public void onBack() {
        if (isLoadingBase) return;
        finish();
    }

    @OnClick(R.id.iv_apply)
    public void onApply() {
        if (isLoadingBase) return;

        if (validInfo()) {
            boolean isNeeded = false;

            String access_token, token_type;
            access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
            token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

            ContentValues headers = new ContentValues();
            headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

            ContentValues body = new ContentValues();
            if (!strFirstName.equals(sFirstName)) {
                isNeeded = true;
                body.put(BODY_FIRST_NAME, sFirstName);
            }

            if (!strLastName.equals(sLastName)) {
                isNeeded = true;
                body.put(BODY_LAST_NAME, sLastName);
            }

            if (!strEmail.equals(sEmail)) {
                isNeeded = true;
                body.put(BODY_EMAIL, sEmail);
            }

            if (!strPassword.equals(sPassword)) {
                isNeeded = true;
                body.put(BODY_PASSWORD, sPassword);
            }

            if (isNeeded) {
                isLoadingBase = true;

                mProgressDialog.setTitle("Please wait...");
                mProgressDialog.setMessage("Updating Profile");
                mProgressDialog.show();

                String url = USERS_URL + "/" + commonUtils.getLongFromJSONObject(AppController.currentUser, ID);

                mGetDataTask = new GetDataTask(url, headers, body, POST_METHOD, this);
                mGetDataTask.execute();
            }
        }
    }

    @OnClick(R.id.preferences_container)
    public void onPreference() {
        navigationWithoutFinish(this, Preferences.class);
    }

    @OnClick(R.id.home_hub_container)
    public void onHomeHub() {
        Intent intent = new Intent(this, HomeHub.class);
        intent.putExtra(FROM, SETTING);
        navigationWithFinish(this, intent);
    }

    private String strFirstName, strLastName, strEmail, strPassword;
    private String sFirstName, sLastName, sEmail, sPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        initData();
        initUI();
    }

    private void initData() {
        JSONObject currentUser = AppController.currentUser;
        strFirstName = commonUtils.getStringFromJSONObject(currentUser, FIRST_NAME);
        strLastName  = commonUtils.getStringFromJSONObject(currentUser, LAST_NAME);
        strEmail     = commonUtils.getStringFromJSONObject(currentUser, EMAIL);
        strPassword  = commonUtils.getStringFromSharedPreference(this, BODY_PASSWORD);
    }

    private void initUI() {
        etFirstName.setText(strFirstName);
        etLastName .setText(strLastName);
        etEmail    .setText(strEmail);
        etPassword .setText(strPassword);
    }

    private boolean validInfo() {
        boolean valid = false;
        View focusView = null;

        etFirstName.setError(null);
        etLastName .setError(null);
        etEmail    .setError(null);
        etPassword .setError(null);

        sFirstName = etFirstName.getText().toString().trim();
        sLastName  = etLastName .getText().toString().trim();
        sEmail     = etEmail    .getText().toString().trim();
        sPassword  = etPassword .getText().toString().trim();


        if (sFirstName.isEmpty()) {
            etFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
        } else if (sLastName.isEmpty()) {
            etLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
        } else if (sEmail.isEmpty()) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
        } else if (!commonUtils.isValidEmail(sEmail)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            focusView = etEmail;
        } else if (sPassword.isEmpty()) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
        } else if (sPassword.length() < 4) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
        } else {
            valid = true;
        }

        if (!valid) focusView.requestFocus();

        return valid;
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;
        mProgressDialog.dismiss();

        if (response == null) {
            commonUtils.showAlertDialog(this, "Please check your internet connection status");
            return;
        }

        JSONObject currentUser = commonUtils.getJSONObjectFromJSONObject(response, USER);
        AppController.currentUser = currentUser;
        commonUtils.setJSONObjectToSharedPreference(this, USER, currentUser);
        commonUtils.setStringToSharedPreference(this, BODY_PASSWORD, sPassword);

        onBack();
    }
}
