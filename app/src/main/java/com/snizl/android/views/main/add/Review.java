package com.snizl.android.views.main.add;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.HttpUrlManager;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.libraries.AspectRatioImageView;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONObject;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Review extends BaseActivity implements OnTaskCompleted{
    @BindView(R.id.collapsing_toolbar_layout) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.iv_picture) AspectRatioImageView ivPicture;
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_about_title) TextView tvAboutTitle;
    @BindView(R.id.tv_description) TextView tvDescription;
    @BindView(R.id.deal_time_container) LinearLayout mDealTimeContainer;
    @BindView(R.id.tv_deal_start_time) TextView tvDealStartTime;
    @BindView(R.id.tv_deal_end_time)   TextView tvDealEndTime;
    @BindView(R.id.btn_post) Button btnPost;

    @OnClick(R.id.btn_post)
    public void onPost() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type   = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        JSONObject business = commonUtils.getJSONObjectFromJSONArray(AppController.businesses, AppController.seleted_business_index);
        ContentValues body = new ContentValues();
        body.put(BUSINESS, commonUtils.getLongFromJSONObject(business, ID));
        body.put(TITLE, AppController.title);
        body.put(DESCRIPTION, AppController.description);

        if (!AppController.strInstore.isEmpty())
            body.put(CLAIM_WALK_IN, AppController.strInstore);

        if (!AppController.strWebsite.isEmpty())
            body.put(CLAIM_WEBSITE, AppController.strWebsite);

        if (!AppController.strPhone.isEmpty())
            body.put(CLAIM_CALL_UP, AppController.strPhone);

        if (!AppController.strCustom.isEmpty())
            body.put(CLAIM_SPECIAL, AppController.strCustom);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

        Calendar startDate = AppController.date;
        Time startTime = AppController.time;
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH), startTime.getHours(), startTime.getMinutes());
        String strStartDate = dateFormatter.format(startDate.getTime());

        body.put(TIME, strStartDate);

        if (AppController.add_type == 0) {
            body.put(DAYS, AppController.days);
            body.put(HOURS, AppController.hours);
            body.put(MINUTES, AppController.minutes);
        }

        File pictureFile = new File(AppController.selected_media_uri.getPath());
        Log.d(TAG, pictureFile.getPath());
//        body.put(PICTURE, pictureFile);


        isLoadingBase = true;

        mGetDataTask = new GetDataTask(API_URL_OFFERS, headers, body, POST_METHOD, this);
        mGetDataTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ButterKnife.bind(this);

        initData();
        initUI();
    }

    @Override
    public void onBackPressed() {
        onBack(false);
    }

    private void initData() {

    }

    private void initUI() {
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack(false);
            }
        });

        mCollapsingToolbarLayout.setTitle("Review " + (AppController.add_type == 0 ? "Deal" : "Promotion"));

        if (AppController.selected_media_uri != null) {
            ivPicture.setUri(this, AppController.selected_media_uri);
        } else {
            ivPicture.setResId(this, R.drawable.img_post_bg);
        }

        tvTitle.setText(AppController.title);

        String strAboutTitle = "About the " + (AppController.add_type == 0 ? "Deal" : "Promotion");
        tvAboutTitle.setText(strAboutTitle);
        tvDescription.setText(AppController.description);

        if (AppController.add_type == 0) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

            mDealTimeContainer.setVisibility(View.VISIBLE);
            Calendar startDate = AppController.date;
            Time startTime = AppController.time;
            startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH), startTime.getHours(), startTime.getMinutes());
            String strStartDate = dateFormatter.format(startDate.getTime());

            startDate.add(Calendar.DATE,   AppController.days);
            startDate.add(Calendar.HOUR,   AppController.hours);
            startDate.add(Calendar.MINUTE, AppController.minutes);
            String strEndDate   = dateFormatter.format(startDate.getTime());

            strStartDate = "Starts: " + commonUtils.convertDateFormat(strStartDate);
            strEndDate   = "Ends:   " + commonUtils.convertDateFormat(strEndDate);

            tvDealStartTime.setText(strStartDate);
            tvDealEndTime.setText(strEndDate);
        } else {
            mDealTimeContainer.setVisibility(View.GONE);
        }

        String strPostButtonTitle = "Save & Post" + (AppController.add_type == 0 ? "Deal" : "Promo");
        btnPost.setText(strPostButtonTitle);
    }

    private void onBack(boolean result) {
        Intent intent = new Intent();
        setResult(result ? RESULT_OK : RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;
//        onBack(true);
    }
}
