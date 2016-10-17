package com.snizl.android.views.deal;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.libraries.AspectRatioImageView;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Deal extends BaseActivity implements OnTaskCompleted{
    private final int SAVE_TO_WALLET_REQUEST_CODE     = 0;
    private final int REMOVE_FROM_WALLET_REQUEST_CODE = 1;

    @BindView(R.id.main_content)       CoordinatorLayout mMainContent;
    @BindView(R.id.toolbar)            Toolbar mToolbar;

    @BindView(R.id.iv_picture)         AspectRatioImageView ivPicture;
    @BindView(R.id.tv_business_name)   TextView tvBusiness_name;
    @BindView(R.id.tv_title)           TextView tvTitle;
    @BindView(R.id.tv_description)     TextView tvDescription;
    @BindView(R.id.tv_address)         TextView tvAddress;
    @BindView(R.id.tv_deal_start_time) TextView tvDealStartTime;
    @BindView(R.id.tv_deal_end_time)   TextView tvDealEndTime;
    @BindView(R.id.tv_left_days)       TextView tvLeftDays;
    @BindView(R.id.btn_wallet)         Button btnWallet;
    @BindView(R.id.btn_claim)          Button btnClaim;

    @BindView(R.id.deal_time_container) LinearLayout dealTimeContainer;

    @OnClick(R.id.btn_wallet)
    public void onWallet() {
        if (isLoadingBase) return;

        if (wallet_id > 0) {
            removeFromWallet();
        } else {
            saveToWallet();
        }
    }

    @OnClick(R.id.btn_claim)
    public void onClaim() {
        if (isLoadingBase) return;

        if (isOffer) {
            final Dialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(R.layout.dialog_claim);
            dialog.setCancelable(true);

            Button btnCallIn = (Button) dialog.findViewById(R.id.btn_instore);
            Button btnCallUp = (Button) dialog.findViewById(R.id.btn_phone);
            Button btnWebsite = (Button) dialog.findViewById(R.id.btn_website);
            Button btnSpecial = (Button) dialog.findViewById(R.id.btn_custom);

            JSONObject claim = commonUtils.getJSONObjectFromJSONObject(offer, CLAIM);
            JSONObject call_in = commonUtils.getJSONObjectFromJSONObject(claim, CALL_IN);
            JSONObject call_up = commonUtils.getJSONObjectFromJSONObject(claim, CALL_UP);
            final JSONObject website = commonUtils.getJSONObjectFromJSONObject(claim, WEBSITE);
            JSONObject special = commonUtils.getJSONObjectFromJSONObject(claim, SPECIAL);

            boolean b_call_in = commonUtils.getBooleanFromJSONObject(call_in, ENABLED);
            boolean b_call_up = commonUtils.getBooleanFromJSONObject(call_up, ENABLED);
            boolean b_website = commonUtils.getBooleanFromJSONObject(website, ENABLED);
            boolean b_special = commonUtils.getBooleanFromJSONObject(special, ENABLED);

            commonUtils.changeViewColor(btnCallIn, b_call_in, this);
            commonUtils.changeViewColor(btnCallUp, b_call_up, this);
            commonUtils.changeViewColor(btnWebsite, b_website, this);
            commonUtils.changeViewColor(btnSpecial, b_special, this);

            if (b_call_in)
                btnCallIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performInstoreClaim();
                        dialog.dismiss();
                    }
                });

            if (b_call_up)
                btnCallUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performCallUp();
                        dialog.dismiss();
                    }
                });

            if (b_website)
                btnWebsite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performWebsiteClaim(website);
                        dialog.dismiss();
                    }
                });

            if (b_special)
                btnSpecial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            dialog.show();
        } else {
            String url = commonUtils.getStringFromJSONObject(offer, WEBSITE);
            commonUtils.openUrlInBrowser(this, url);
        }
    }

    private JSONObject offer;
    private boolean isOffer;
    private long wallet_id;
    private int request_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        ButterKnife.bind(this);

        try {
            offer = new JSONObject(getIntent().getStringExtra(FEED));

            initData();
            initUI();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void initData() {
        String strType = commonUtils.getStringFromJSONObject(offer, TYPE);
        isOffer = (strType != null);
    }

    private void initUI() {
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        JSONObject picture, business, address = null, feed, dates;

        picture  = commonUtils.getJSONObjectFromJSONObject(offer, PICTURE);
        feed     = commonUtils.getJSONObjectFromJSONObject(offer, FEED);
        business = commonUtils.getJSONObjectFromJSONObject(offer, BUSINESS);
        dates    = commonUtils.getJSONObjectFromJSONObject(offer, DATES);

        if (business != null)
            address  = commonUtils.getJSONObjectFromJSONObject(business, ADDRESS);


        if (picture != null) {
            String url = commonUtils.getStringFromJSONObject(picture, KEY_URL);
            ivPicture.setUrl(this, url);
        }

        if (business != null) {
            tvBusiness_name.setText(commonUtils.getStringFromJSONObject(business, NAME));
        }

        tvTitle.setText(commonUtils.getStringFromJSONObject(offer, TITLE));
        tvDescription.setText(commonUtils.getStringFromJSONObject(offer, DESCRIPTION));

        if (address != null) {
            tvAddress.setText(commonUtils.getStringFromJSONObject(address, FORMATTED));
        }

        if (feed != null && isOffer) {
            String strStartDate = commonUtils.getStringFromJSONObject(feed, START);
            String strEndDate   = commonUtils.getStringFromJSONObject(feed, END);
            String strLeftDays  = commonUtils.calcDifferenceDate(strEndDate, 2);

            strStartDate = "Start Time: " + commonUtils.convertDateFormat(strStartDate);
            strEndDate   = "End Time: " + commonUtils.convertDateFormat(strEndDate);

            tvDealStartTime.setText(strStartDate);
            tvDealEndTime.setText(strEndDate);
            tvLeftDays.setText(strLeftDays);
        } else {
            dealTimeContainer.setVisibility(View.GONE);
            String strStartDate = commonUtils.getStringFromJSONObject(dates, START);
            String strEndDate   = commonUtils.getStringFromJSONObject(dates, END);
            String duration     = commonUtils.convertDateFormat(strStartDate, strEndDate);

            tvLeftDays.setText(duration);
        }

        btnClaim.setText(isOffer ? "Claim" : "View Website");

        updateWalletButton();
    }

    //    ******************************** Claim ********************************
    private void performInstoreClaim() {
        final Dialog dialog = new Dialog(this, R.style.AppTheme_Dialog);
        dialog.setContentView(R.layout.dialog_claim_instore);
        dialog.setCancelable(true);

        Button btnContinue = (Button) dialog.findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navToClaimInstore();
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

    private void navToClaimInstore() {
        Intent intent = new Intent(this, ClaimInstore.class);
        intent.putExtra(OFFER, offer.toString());
        startActivity(intent);
    }

    private void performCallUp() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            JSONObject business = commonUtils.getJSONObjectFromJSONObject(offer, BUSINESS);
            String strPhoneNumber = commonUtils.getStringFromJSONObject(business, TELEPHONE);
            strPhoneNumber = strPhoneNumber.replace(" ", "");
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + strPhoneNumber));
            startActivity(callIntent);
        }
    }

    private void performWebsiteClaim(JSONObject website) {
        if (website == null) return;

        String url = commonUtils.getStringFromJSONObject(website, KEY_URL);
        commonUtils.openUrlInBrowser(this, url);
    }


    //    ******************************** Wallet ********************************
    private void updateWalletButton() {
        wallet_id = getWalletID(commonUtils.getLongFromJSONObject(offer, ID));
        btnWallet.setText(wallet_id > 0 ? "Remove from Wallet" : "Save to Wallet");
    }


    private void saveToWallet() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type   = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        ContentValues body = new ContentValues();
        body.put(isOffer ? BODY_OFFER_ID : BODY_EVENT_ID, commonUtils.getLongFromJSONObject(offer, ID));

        isLoadingBase = true;
        request_code = SAVE_TO_WALLET_REQUEST_CODE;

        new GetDataTask(WALLET_URL, headers, body, POST_METHOD, this).execute();
    }

    private void removeFromWallet() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type   = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);
        headers.put(HEADER_X_API_KEY, X_API_KEY);

        isLoadingBase = true;
        request_code = REMOVE_FROM_WALLET_REQUEST_CODE;

        String url = API_URL_DELETE_WALLET.replace("wallet_id", String.valueOf(getWalletID(commonUtils.getLongFromJSONObject(offer, ID))));

        new GetDataTask(url, headers, null, DELETE_METHOD, this).execute();
    }

    //    ******************************** Request Result ********************************
    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (request_code == SAVE_TO_WALLET_REQUEST_CODE) {
            if (response == null) {
                commonUtils.showAlertDialog(this, "Please check your internet connection status");
            } else {
                JSONObject wallet = commonUtils.getJSONObjectFromJSONObject(response, WALLET);
                AppController.wallet = commonUtils.addJSONObjectToJSONArray(AppController.wallet, wallet);
                commonUtils.setJSONArrayToSharedPreference(this, WALLET, AppController.wallet);

                updateWalletButton();
            }
        } else if (request_code == REMOVE_FROM_WALLET_REQUEST_CODE){
            removeItemFromWallet(commonUtils.getLongFromJSONObject(offer, ID));
            updateWalletButton();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performCallUp();
            }
        }
    }

}
