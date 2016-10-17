package com.snizl.android.views.business;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.BusinessPagerAdapter;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class Business extends BaseActivity implements OnTaskCompleted{
    private final int GET_BUSINESSES_OFFERS_REQUEST_CODE = 0;
    private final int GET_BUSINESSES_EVENTS_REQUEST_CODE = 1;

    @BindView(R.id.app_bar_layout)            AppBarLayout            mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar)                   Toolbar                 mToolbar;

    @BindView(R.id.iv_picture) ImageView       ivPicture;
    @BindView(R.id.iv_logo)    CircleImageView ivLogo;
    @BindView(R.id.tv_name)    TextView        tvName;
    @BindView(R.id.tv_address) TextView        tvAddress;
    @BindView(R.id.btn_follow) Button          btnFollow;

    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.view_pager) ViewPager mViewPager;


//    ***************************************************************************
    public JSONObject business;
    private long business_id;

    public JSONArray offers, events;

    private int request_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        ButterKnife.bind(this);

        try {
            business = new JSONObject(getIntent().getStringExtra(BUSINESS));

            initData();
            initUI();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void initData() {
        business_id = commonUtils.getLongFromJSONObject(business, ID);
        String url = API_URL_GET_BUSINESSES_OFFERS.replace("business_id", String.valueOf(business_id));
        request_code = GET_BUSINESSES_OFFERS_REQUEST_CODE;

        getFeeds(url);
    }

    private void initUI() {
//        Toolbar
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        Header
        JSONObject featureimage, logo, address;

        featureimage  = commonUtils.getJSONObjectFromJSONObject(business, FEATUREIMAGE);
        logo          = commonUtils.getJSONObjectFromJSONObject(business, LOGO);
        address       = commonUtils.getJSONObjectFromJSONObject(business, ADDRESS);

        if (featureimage != null) {
            String url = commonUtils.getStringFromJSONObject(featureimage, KEY_URL);
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivPicture);
        } else {
            Glide.with(this)
                    .load(R.drawable.img_no_picture)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivPicture);
        }


        if (logo != null) {
            String url = commonUtils.getStringFromJSONObject(logo, KEY_URL);
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivLogo);
        } else {
            Glide.with(this)
                    .load(R.drawable.img_no_logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivLogo);
        }

        tvName.setText(commonUtils.getStringFromJSONObject(business, NAME));
        if (address != null) {
            tvAddress.setText(commonUtils.getStringFromJSONObject(address, FORMATTED));
        }

        boolean isFollowing = commonUtils.getBooleanFromJSONObject(business, FOLLOWING);

        btnFollow.setText(isFollowing ? getString(R.string.following) : getString(R.string.follow));
        btnFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, isFollowing ? R.drawable.ic_apply : 0, 0);
    }

    private void initViewPager() {
        BusinessPagerAdapter adapter = new BusinessPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }

    private void getFeeds(String url) {
        String access_token, token_type;

        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (response == null) {
            commonUtils.showAlertDialog(this, "Please check your internet connection status");
            return;
        }

        if (request_code == GET_BUSINESSES_OFFERS_REQUEST_CODE) {
            JSONArray  jOffers    = commonUtils.getJSONArrayFromJSONObject(response, OFFERS);
            JSONObject meta       = commonUtils.getJSONObjectFromJSONObject(response, META);
            JSONObject pagination = commonUtils.getJSONObjectFromJSONObject(meta, PAGINATION);
            int total_pages  = commonUtils.getIntFromJSONObject(pagination, TOTAL_PAGES);
            int current_page = commonUtils.getIntFromJSONObject(pagination, CURRENT_PAGE);

            offers = commonUtils.addJSONArrayToJSONArray(offers, jOffers);

            if (total_pages > current_page) {
                JSONObject links = commonUtils.getJSONObjectFromJSONObject(pagination, LINKS);
                String url = commonUtils.getStringFromJSONObject(links, NEXT);

                getFeeds(url);
            } else {
                String url = API_URL_GET_BUSINESSES_EVENTS.replace("business_id", String.valueOf(business_id));
                request_code = GET_BUSINESSES_EVENTS_REQUEST_CODE;

                getFeeds(url);
            }
        } else if (request_code == GET_BUSINESSES_EVENTS_REQUEST_CODE) {
            JSONArray  jEvents    = commonUtils.getJSONArrayFromJSONObject(response, EVENTS);
            JSONObject meta       = commonUtils.getJSONObjectFromJSONObject(response, META);
            JSONObject pagination = commonUtils.getJSONObjectFromJSONObject(meta, PAGINATION);
            int total_pages  = commonUtils.getIntFromJSONObject(pagination, TOTAL_PAGES);
            int current_page = commonUtils.getIntFromJSONObject(pagination, CURRENT_PAGE);

            events = commonUtils.addJSONArrayToJSONArray(events, jEvents);

            if (total_pages > current_page) {
                JSONObject links = commonUtils.getJSONObjectFromJSONObject(pagination, LINKS);
                String url = commonUtils.getStringFromJSONObject(links, NEXT);

                getFeeds(url);
            } else {
                initViewPager();
            }
        }
    }
}
