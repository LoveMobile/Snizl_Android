package com.snizl.android.views.business;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.snizl.android.R;
import com.snizl.android.views.base.BaseFragment;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BusinessAboutFragment extends BaseFragment {
    @BindView(R.id.tv_description) TextView tvDescription;
    @BindView(R.id.tv_address)     TextView tvAddress;
    @BindView(R.id.tv_phone)       TextView tvPhone;

    @OnClick(R.id.tv_phone)
    public void onPhone() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            callPhone(tvPhone.getText().toString());
        }
    }

    private Context mContext;
    private JSONObject business;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_about, container, false);
        ButterKnife.bind(this, view);
        initData();
        initUI();
        return view;
    }

    private void initData() {
        mContext = getContext();
        business = ((Business)getActivity()).business;
    }

    private void initUI() {
        String strDescription = commonUtils.getStringFromJSONObject(business, DESCRIPTION);
        String strPhone = commonUtils.getStringFromJSONObject(business, TELEPHONE);

        JSONObject address = commonUtils.getJSONObjectFromJSONObject(business, ADDRESS);
        String strAddress = commonUtils.getStringFromJSONObject(address, FORMATTED);

        tvDescription.setText(strDescription);
        tvAddress.setText(strAddress);
        tvPhone.setText(strPhone);
    }

    private void callPhone(String strPhoneNumber) {
        strPhoneNumber = strPhoneNumber.replace(" ", "");

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + strPhoneNumber));
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhone(tvPhone.getText().toString());
            }
        }
    }
}