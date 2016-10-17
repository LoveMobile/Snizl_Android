package com.snizl.android.views.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.snizl.android.AppConfig;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.GetStringDataTask;
import com.snizl.android.api.HttpUrlManager;
import com.snizl.android.utilities.CommonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity implements AppConfig, HttpUrlManager {

    public CommonUtils commonUtils = CommonUtils.getInstance();
    public boolean isLoadingBase;
    public ProgressDialog mProgressDialog;
    public GetDataTask mGetDataTask = null;
    public GetStringDataTask mGetStringDataTask= null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this, R.style.CustomProgressDialogTheme);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }

    public void navigationWithFinish(Activity mActivity, Class mClass) {
        Intent intent = new Intent(mActivity, mClass);
        startActivity(intent);
        mActivity.finish();
    }

    public void navigationWithFinish(Activity mActivity, Intent intent) {
        startActivity(intent);
        mActivity.finish();
    }

    public void navigationWithoutFinish(Activity mActivity, Class mClass) {
        Intent intent = new Intent(mActivity, mClass);
        startActivity(intent);
    }

    public void setupUI(View view, Activity activity) {
        commonUtils.setupUI(view, activity);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    public long getWalletID(long offer_id) {
        long result = -1;
        JSONArray wallets = AppController.wallet;

        if (wallets == null) return result;

        for (int i = 0; i < wallets.length(); i++) {
            JSONObject wallet       = commonUtils.getJSONObjectFromJSONArray(wallets, i);
            JSONObject offer        = commonUtils.getJSONObjectFromJSONObject(wallet, OFFER);
            JSONObject event        = commonUtils.getJSONObjectFromJSONObject(wallet, EVENT);

            long id_in_wallet;
            if (offer != null) {
                id_in_wallet = commonUtils.getLongFromJSONObject(offer, ID);
            } else {
                id_in_wallet = commonUtils.getLongFromJSONObject(event, ID);
            }


            if (id_in_wallet == offer_id) {
                result = commonUtils.getLongFromJSONObject(wallet, ID);
                break;
            }
        }

        return result;
    }

    public int getIndexOfItemInWallet(long offer_id) {
        int result = -1;
        long wallet_id = getWalletID(offer_id);

        if (wallet_id < 0) return result;

        JSONArray wallets = AppController.wallet;

        for (int i = 0; i < wallets.length(); i++) {
            JSONObject wallet = commonUtils.getJSONObjectFromJSONArray(wallets, i);
            long temp_wallet_id = commonUtils.getLongFromJSONObject(wallet, ID);

            if (temp_wallet_id == wallet_id) {
                result = i;
                break;
            }
        }

        return result;
    }

    public boolean removeItemFromWallet(long offer_id) {
        int index = getIndexOfItemInWallet(offer_id);

        if (index < 0) return false;

        AppController.wallet = commonUtils.removeJSONObjectToJSONArray(AppController.wallet, index);
        commonUtils.setJSONArrayToSharedPreference(this, WALLET, AppController.wallet);

        return true;
    }

    public long getUserId() {
        return commonUtils.getLongFromJSONObject(AppController.currentUser, ID);
    }

    public int getUserRange() {
        return commonUtils.getIntFromJSONObject(AppController.currentUser, RANGE);
    }

    public long getUserHubId() {
        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, HUB);
        return commonUtils.getLongFromJSONObject(hub, ID);
    }

    public boolean hasHub() {
        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, HUB);
        return hub != null;
    }
}

