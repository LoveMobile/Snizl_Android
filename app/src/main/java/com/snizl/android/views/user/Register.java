package com.snizl.android.views.user;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Register extends BaseActivity implements OnTaskCompleted {
    private final int REGISTER_USER_REQUEST_CODE       = 0;
    private final int OAUTH_ACCESS_TOKEN_REQUEST_CODE  = 1;

    @BindView(R.id.et_first_name)  EditText etFirstName;
    @BindView(R.id.et_last_name)   EditText etLastName;
    @BindView(R.id.et_email)       EditText etEmail;
    @BindView(R.id.et_password)    EditText etPassword;
    @BindView(R.id.tv_description) TextView tvDescription;

    @OnClick(R.id.iv_back)
    public void onBack() {
        navigationWithFinish(this, Login.class);
    }

    @OnClick(R.id.btn_sign_up)
    public void signUp() {
        if (validInfo()) {
            ContentValues headers = new ContentValues();
            headers.put(HEADER_X_API_KEY, X_API_KEY);

            ContentValues body = new ContentValues();
            body.put(BODY_FIRST_NAME, strFirstName);
            body.put(BODY_LAST_NAME,  strLastName);
            body.put(BODY_EMAIL,      strEmail);
            body.put(BODY_PASSWORD,   strPassword);

            isLoadingBase = true;
            request_code = REGISTER_USER_REQUEST_CODE;

            mProgressDialog.setMessage("Signing up, please wait");
            mProgressDialog.show();

            mGetDataTask = new GetDataTask(USERS_URL, headers, body, POST_METHOD, this);
            mGetDataTask.execute();
        }
    }

    @OnClick(R.id.terms_conditions)
    public void termsAndConditions() {
        commonUtils.openUrlInBrowser(this, "https://www.snizl.com/legal/terms");
    }


    private String strFirstName, strLastName, strEmail, strPassword;
    private int request_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        setupUI(findViewById(R.id.parent), this);

        initUI();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void initUI() {
        String strDescription = "<font color=#757575>If you're a business owner you can add your business to Snizl by logging in again via a web browser at</font> <font color=#fa1745>www.snizl.com</font> <font color=#757575>and going to the Manage Account section</font>";
        tvDescription.setText(Html.fromHtml(strDescription));
    }

    private void getOauthToken() {
        ContentValues headers = new ContentValues();
        headers.put(HEADER_X_API_KEY, X_API_KEY);

        ContentValues body = new ContentValues();
        body.put(BODY_USERNAME,   strEmail);
        body.put(BODY_PASSWORD,   strPassword);
        body.put(BODY_CLIENT_ID,  CLIENT_ID);
        body.put(BODY_GRANT_TYPE, "password");


        isLoadingBase = true;
        request_code = OAUTH_ACCESS_TOKEN_REQUEST_CODE;

        mGetDataTask = new GetDataTask(API_URL_OAUTH_ACCESS_TOKEN, headers, body, POST_METHOD, this);
        mGetDataTask.execute();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (response == null) {
            if (request_code == OAUTH_ACCESS_TOKEN_REQUEST_CODE) {
                commonUtils.showAlertDialog(this, "Incorrect Username or Password");
            } else {
                commonUtils.showAlertDialog(this, "Please check your internet connection status");
            }
            mProgressDialog.dismiss();
            return;
        }

        if (commonUtils.getIntFromJSONObject(response, STATUS_CODE) >=400) {
            commonUtils.showAlertDialog(this, commonUtils.getStringFromJSONObject(response, MESSAGE));
            mProgressDialog.dismiss();
            return;
        }

        switch (request_code) {
            case REGISTER_USER_REQUEST_CODE:
                JSONObject currentUser = commonUtils.getJSONObjectFromJSONObject(response, USER);
                AppController.currentUser = currentUser;
                commonUtils.setJSONObjectToSharedPreference(this, USER, currentUser);

                getOauthToken();
                break;
            case OAUTH_ACCESS_TOKEN_REQUEST_CODE:
                AppController.oauth_token = response;
                commonUtils.setJSONObjectToSharedPreference(this, OAUTH_TOKEN, response);
                commonUtils.setStringToSharedPreference(this, BODY_PASSWORD, strPassword);

                Intent intent = new Intent(this, HomeHub.class);
                intent.putExtra(FROM, SIGN_UP);
                navigationWithFinish(this, intent);
                break;
        }
    }

    private boolean validInfo() {
        boolean valid = false;
        View focusView = null;

        etFirstName.setError(null);
        etLastName .setError(null);
        etEmail    .setError(null);
        etPassword .setError(null);

        strFirstName = etFirstName.getText().toString().trim();
        strLastName  = etLastName .getText().toString().trim();
        strEmail     = etEmail    .getText().toString().trim();
        strPassword  = etPassword .getText().toString().trim();

        if (strFirstName.isEmpty()) {
            etFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
        } else if (strLastName.isEmpty()) {
            etLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
        } else if (strEmail.isEmpty()) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
        } else if (!commonUtils.isValidEmail(strEmail)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            focusView = etEmail;
        } else if (strPassword.isEmpty()) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
        } else if (strPassword.length() < 8) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
        } else {
            valid = true;
        }

        if (!valid) focusView.requestFocus();

        return valid;
    }
}
