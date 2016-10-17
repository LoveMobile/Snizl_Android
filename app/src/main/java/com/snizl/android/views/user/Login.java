package com.snizl.android.views.user;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;
import com.snizl.android.views.main.Main;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Login extends BaseActivity implements OnTaskCompleted{
    private final int OAUTH_ACCESS_TOKEN_REQUEST_CODE  = 0;
    private final int GET_CURRENT_USER_REQUEST_CODE    = 1;
    private final int GET_USER_BUSINESSES_REQUEST_CODE = 2;
    private final int GET_USER_WALLET_REQUEST_CODE     = 3;

    @BindView(R.id.login_first_container) LinearLayout firstContainer;
    @OnClick(R.id.btn_facebook)
    public void signInWithFacebook() {
//        if (isLoadingBase) return;
    }

    @OnClick(R.id.register_container)
    public void navToRegister(){
        if (isLoadingBase) return;

        navigationWithFinish(this, Register.class);
    }

    @OnClick(R.id.sign_in_container)
    public void navToSignIn() {
        if (isLoadingBase) return;

        showSignInPage();
    }


    @BindView(R.id.login_second_container) LinearLayout secondContainer;
    @BindView(R.id.et_email)               EditText etEmail;
    @BindView(R.id.et_password)            EditText etPassword;

    @OnClick(R.id.btn_sign_in)
    public void onSignIn() {
        if (isLoadingBase) return;

        if (validInfo()) {
            ContentValues headers = new ContentValues();
            headers.put(HEADER_X_API_KEY, X_API_KEY);

            ContentValues body = new ContentValues();
            body.put(BODY_USERNAME, strEmail);
            body.put(BODY_PASSWORD, strPassword);
            body.put(BODY_CLIENT_ID, CLIENT_ID);
            body.put(BODY_GRANT_TYPE, "password");


            isLoadingBase = true;
            request_code = OAUTH_ACCESS_TOKEN_REQUEST_CODE;

            mProgressDialog.setMessage("Signing in, please wait");
            mProgressDialog.show();

            mGetDataTask = new GetDataTask(API_URL_OAUTH_ACCESS_TOKEN, headers, body, POST_METHOD, this);
            mGetDataTask.execute();
        }
    }

    @OnClick(R.id.btn_forgot_password)
    public void onForgotPassword() {
        if (isLoadingBase) return;

        final Dialog dialog = new Dialog(this, R.style.AppTheme_Dialog);
        dialog.setContentView(R.layout.dialog_forgot_password);
        dialog.setCancelable(true);

        Button btnVisitWebsite = (Button) dialog.findViewById(R.id.btn_visit_website);

        btnVisitWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commonUtils.openUrlInBrowser(Login.this, FORGOT_PASSWORD_URL);
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

//    ************************************************************************************************

    private boolean isInFirstContainer = true;
    private String strEmail, strPassword;
    private int request_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setupUI(findViewById(R.id.parent), this);

        navToNext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupVideoView();
    }

    @Override
    public void onBackPressed() {
        if (isLoadingBase) return;

        if (isInFirstContainer)
            super.onBackPressed();
        else
            hideSignInPage();
    }

    private void navToNext() {
        if (!commonUtils.isUserLoggedIn(this)) return;

        if (!hasHub()) {
            Intent intent = new Intent(this, HomeHub.class);
            intent.putExtra(FROM, SIGN_IN);
            navigationWithFinish(this, intent);
        } else {
            navigationWithFinish(this, Main.class);
        }
    }

    private void setupVideoView() {
        final VideoView vvBackground = (VideoView)findViewById(R.id.vv_background);

        String uriPath = "android.resource://" + getPackageName() + "/" + R.raw.login;
        Uri uri = Uri.parse(uriPath);

        vvBackground.setVideoURI(uri);
        vvBackground.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                vvBackground.start();
            }
        });

        vvBackground.start();
    }

    private void showSignInPage() {
        isInFirstContainer = false;

        firstContainer.setVisibility(View.GONE);
        commonUtils.fadeOutView(firstContainer, FADE_DURATION);

        secondContainer.setVisibility(View.VISIBLE);
        commonUtils.fadeInView(secondContainer, FADE_DURATION);
    }

    private void hideSignInPage() {
        isInFirstContainer = true;

        secondContainer.setVisibility(View.GONE);
        commonUtils.fadeOutView(secondContainer, FADE_DURATION);

        firstContainer.setVisibility(View.VISIBLE);
        commonUtils.fadeInView(firstContainer, FADE_DURATION);
    }

    private boolean validInfo() {
        boolean valid = false;
        View focusView = null;

        etEmail.setError(null);
        etPassword.setError(null);

        strEmail    = etEmail   .getText().toString().trim();
        strPassword = etPassword.getText().toString().trim();

        if (strEmail.isEmpty()) {
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

    private void getCurrentUser() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_CURRENT_USER_REQUEST_CODE;

        mGetDataTask = new GetDataTask(API_URL_GET_CURRENT_USER, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void getUserBusinesses() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_USER_BUSINESSES_REQUEST_CODE;

        String url = API_URL_GET_USER_BUSINESSES.replace("user_id", String.valueOf(getUserId()));

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void getWallet() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_USER_WALLET_REQUEST_CODE;

        mGetDataTask = new GetDataTask(WALLET_URL, headers, null, GET_METHOD, this);
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

        if (request_code == OAUTH_ACCESS_TOKEN_REQUEST_CODE && commonUtils.getStringFromJSONObject(response, ERROR) != null) {
            commonUtils.showAlertDialog(this, commonUtils.getStringFromJSONObject(response, ERROR_DESCRIPTION));
            mProgressDialog.dismiss();
            return;
        }

        if (commonUtils.getIntFromJSONObject(response, STATUS_CODE) >= 400) {
            commonUtils.showAlertDialog(this, commonUtils.getStringFromJSONObject(response, MESSAGE));
            mProgressDialog.dismiss();
            return;
        }

        if (request_code == OAUTH_ACCESS_TOKEN_REQUEST_CODE) {
            AppController.oauth_token = response;
            commonUtils.setJSONObjectToSharedPreference(this, OAUTH_TOKEN, response);
            commonUtils.setStringToSharedPreference(this, BODY_PASSWORD, strPassword);

            getCurrentUser();
        } else if (request_code == GET_CURRENT_USER_REQUEST_CODE) {
            JSONObject currentUser = commonUtils.getJSONObjectFromJSONObject(response, USER);
            AppController.currentUser = currentUser;
            commonUtils.setJSONObjectToSharedPreference(this, USER, currentUser);

            getUserBusinesses();
        } else if (request_code == GET_USER_BUSINESSES_REQUEST_CODE) {
            JSONArray businesses = commonUtils.getJSONArrayFromJSONObject(response, BUSINESSES);
            AppController.businesses = businesses;
            commonUtils.setJSONArrayToSharedPreference(this, BUSINESSES, businesses);

            getWallet();
        } else if (request_code == GET_USER_WALLET_REQUEST_CODE) {
            JSONArray wallet = commonUtils.getJSONArrayFromJSONObject(response, WALLET);
            AppController.wallet = wallet;
            commonUtils.setJSONArrayToSharedPreference(this, WALLET, wallet);

            mProgressDialog.dismiss();
            navToNext();
        }
    }
}
