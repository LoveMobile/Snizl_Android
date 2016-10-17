package com.snizl.android.views.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.snizl.android.AppConfig;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.HttpUrlManager;
import com.snizl.android.utilities.CommonUtils;

import org.json.JSONObject;

public class BaseFragment extends Fragment implements AppConfig, HttpUrlManager {
    public CommonUtils commonUtils = CommonUtils.getInstance();
    public boolean isLoadingBase;
    public ProgressDialog mProgressDialog;
    public GetDataTask mGetDataTask = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTransparentTheme);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void navigationWithoutFinish(Context mContext, Class mClass) {
        Intent intent = new Intent(mContext, mClass);
        startActivity(intent);
    }

    public void navToMyMuvets() {
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.replace(R.id.content_frame, new MyMuvetsFragment());
//        ft.commit();
    }

    public void navToSettings() {
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.replace(R.id.content_frame, new SettingsFragment());
//        ft.commit();
    }

    public long getUserId() {
        return commonUtils.getLongFromJSONObject(AppController.currentUser, ID);
    }

    public long getUserHubId() {
        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, HUB);
        return commonUtils.getLongFromJSONObject(hub, ID);
    }

    public String getUserHubName() {
        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, HUB);
        return commonUtils.getStringFromJSONObject(hub, NAME);
    }

    public double getUserLat() {
        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, HUB);
        return commonUtils.getDoubleFromJSONObject(hub, NAME);
    }

    public void stopTask() {
        if (mGetDataTask != null) mGetDataTask.cancel(true);
    }
}
