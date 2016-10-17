package com.snizl.android.views.user;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.WalletAdapter;
import com.snizl.android.adapters.WalletAdapter.WalletActionListener;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyWallet extends BaseActivity implements WalletActionListener {
    @BindView(R.id.rv_wallet)           RecyclerView mRecyclerView;
    @BindView(R.id.no_wallet_container) LinearLayout noWalletContainer;

    @OnClick(R.id.iv_back)
    public void onBack() {
        finish();
    }

//    *************************************************************************
    private JSONArray wallet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        ButterKnife.bind(this);

        initData();
        initUI();
    }

    private void initData() {
        wallet = AppController.wallet;
    }

    private void initUI() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        displayWallet();
    }

    private void displayWallet() {
        noWalletContainer.setVisibility(wallet.length() > 0 ? View.INVISIBLE: View.VISIBLE);
        mRecyclerView.setAdapter(new WalletAdapter(this, wallet, this));
    }


    @Override
    public void onBusiness(JSONObject business) {

    }

    @Override
    public void onPhoto(View view, JSONObject offer) {

    }

    @Override
    public void onShare(JSONObject offer) {

    }

    @Override
    public void onDelete(JSONObject wallet) {

    }
}
