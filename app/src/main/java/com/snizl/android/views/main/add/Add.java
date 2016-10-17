package com.snizl.android.views.main.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.views.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Add extends BaseActivity {
    private final int REVIEW_REQUEST_CODE = 0;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.add_content_frame) FrameLayout mContentFrame;
    @BindView(R.id.btn_previous) Button btnPrevious;

    @OnClick(R.id.btn_previous)
    public void onPrevious() {
        switch (currentPageNumber) {
            case 2:
                currentPageNumber--;
                addFragment();
                break;
            case 3:
                currentPageNumber--;
                addFragment();
                break;
        }
    }

    @OnClick(R.id.btn_next)
    public void onNext() {
        switch (currentPageNumber) {
            case 0:
                if (AppController.seleted_business_index >= 0) {
                    currentPageNumber++;
                    addFragment();
                } else {
                    commonUtils.showAlertDialog(this, "Please choose a business");
                }
                break;
            case 1:
                currentPageNumber++;
                addFragment();
                break;
            case 2:
                if (AppController.title.isEmpty()) {
                    commonUtils.showAlertDialog(this, "Please set a Title");
                } else if (AppController.description.isEmpty()) {
                    commonUtils.showAlertDialog(this, "Please set a Description");
                } else if (AppController.date == null) {
                    commonUtils.showAlertDialog(this, "Please set a Start date");
                } else if (AppController.time == null) {
                    commonUtils.showAlertDialog(this, "Please set a Start time");
                } else if (AppController.add_type == 0 && (AppController.days * 24 * 60 + AppController.hours * 60 + AppController.minutes) < 5) {
                    commonUtils.showAlertDialog(this, "Deal needs to run longer than 5 mins");
                } else {
                    currentPageNumber++;
                    addFragment();
                }

                break;
            case 3:
                if ((AppController.strInstore + AppController.strWebsite + AppController.strPhone + AppController.strCustom).isEmpty()) {
                    commonUtils.showAlertDialog(this, "Please select a way to claim your offer");
                } else {
                    startActivityForResult(new Intent(this, Review.class), REVIEW_REQUEST_CODE);
                }
                break;
        }
    }


    private int currentPageNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ButterKnife.bind(this);

        initData();
        initUI();
    }

    @Override
    public void onBackPressed() {
        initData();
        finish();
    }

    private void initData() {
        if (AppController.businesses.length() > 1) {
            AppController.seleted_business_index = -1;
            currentPageNumber = 0;
        } else {
            AppController.seleted_business_index = 0;
            currentPageNumber = 1;
        }

        AppController.selected_media_uri = null;
        AppController.title       = "";
        AppController.description = "";
        AppController.date = null;
        AppController.time = null;
        AppController.days    = 0;
        AppController.hours   = 0;
        AppController.minutes = 0;
        AppController.strInstore = "";
        AppController.strWebsite = "";
        AppController.strPhone   = "";
        AppController.strCustom  = "";
    }

    private void initUI() {
        mToolbar.setTitle(AppController.add_type == 0 ? "Add a Deal" : "Add a Promotion");
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initData();
                finish();
            }
        });

        btnPrevious.setVisibility(View.INVISIBLE);

        addFragment();
    }

    private void addFragment() {
        Fragment fragment = null;
        switch (currentPageNumber) {
            case 0:
                btnPrevious.setVisibility(View.INVISIBLE);
                fragment = new SelectBusinessFragment();
                break;
            case 1:
                btnPrevious.setVisibility(View.INVISIBLE);
                fragment = new ChooseMediaFragment();
                break;
            case 2:
                btnPrevious.setVisibility(View.VISIBLE);
                fragment = new AddDetailFragment();
                break;
            case 3:
                btnPrevious.setVisibility(View.VISIBLE);
                fragment = new AddActionFragment();
                break;
            case 4:
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(mContentFrame.getId(), fragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REVIEW_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}
