package com.snizl.android.views.main;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.FeedEventAdapter;
import com.snizl.android.adapters.FeedEventAdapter.FeedEventActionListener;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.libraries.EndlessRecyclerOnScrollListener;
import com.snizl.android.views.base.BaseFragment;
import com.snizl.android.views.business.Business;
import com.snizl.android.views.deal.Deal;
import com.snizl.android.views.user.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventFragment extends BaseFragment implements OnTaskCompleted, FeedEventActionListener{
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private final int GET_FEED_EVENTS_HUB_REQUEST_CODE    = 1;
    private final int GET_FEED_EVENTS_AROUND_REQUEST_CODE = 2;

    @BindView(R.id.tab_layout)           TabLayout mTabLayout;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.rv_events)            RecyclerView mRecyclerView;
    @BindView(R.id.tv_no_results_found)  TextView tvNoResultsFound;

    private Context mContext;
    private Bitmap bitmapToShare;
    private EndlessRecyclerOnScrollListener mEndlessScrollListener;

    private String hub_name;
    private double lat, lng;
    private boolean isHub, isRefresh;
    private String hubEventsNextUrl, aroundEventsNextUrl;

    private FeedEventAdapter mFeedEventAdapter;
    private JSONArray hubEvents, aroundEvents;

    private int request_code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppController.updatedHub) {
//            getFeeds();
            AppController.updatedHub = false;
        }

        if (mEndlessScrollListener != null)
            mEndlessScrollListener.reset(0, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGetDataTask != null)
            mGetDataTask.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                break;
            case R.id.menu_range:
                ((Main)getActivity()).showAdjustRangeDialog();
                break;
            case R.id.menu_preferences:
                navigationWithoutFinish(mContext, Preferences.class);
                break;
        }

        return true;
    }

    private void initData() {
        mContext = getContext();
        hub_name = getUserHubName();
        lat = lng = 0;
        if (AppController.mLocation != null) {
            lat = AppController.mLocation.getLatitude();
            lng = AppController.mLocation.getLongitude();
        }

        isHub = true;

        hubEventsNextUrl = null;
        aroundEventsNextUrl = null;
    }

    private void initUI() {
        changeTitle();
        setHasOptionsMenu(true);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isHub = tab.getPosition() == 0;
                if (mGetDataTask != null) mGetDataTask.cancel(true);
                refresh();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
            tab.setText(hub_name);
        }

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mEndlessScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                getEvents();
            }
        };
        mRecyclerView.addOnScrollListener(mEndlessScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        refresh();
    }

    private void changeTitle() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.events);
    }

    private void refresh() {
        if (isHub) {
            hubEvents = new JSONArray();
            mFeedEventAdapter = new FeedEventAdapter(mContext, hubEvents, this);
        } else {
            aroundEvents = new JSONArray();
            mFeedEventAdapter = new FeedEventAdapter(mContext, aroundEvents, this);
        }

        mRecyclerView.setAdapter(mFeedEventAdapter);
        mEndlessScrollListener.reset(0, true);
        isRefresh = true;
        hubEventsNextUrl = null;
        aroundEventsNextUrl = null;

        getEvents();
    }

    private void getEvents() {
        String access_token, token_type, url;

        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        if (isRefresh) {
            url = FEED_EVENTS_URL;

            if (isHub) {
                url = url + "/hub";
                request_code = GET_FEED_EVENTS_HUB_REQUEST_CODE;
            } else {
                ContentValues body = new ContentValues();
                body.put(BODY_LAT,  lat);
                body.put(BODY_LONG, lng);

                url = url + "/range?" + commonUtils.getQuery(body);
                request_code = GET_FEED_EVENTS_AROUND_REQUEST_CODE;
            }

            isRefresh = false;
        } else {
            url = isHub ? hubEventsNextUrl : aroundEventsNextUrl;
        }

        if (url == null) return;

        isLoadingBase = true;

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void displayEvents() {
        if (isHub) {
            tvNoResultsFound.setVisibility(hubEvents.length()    > 0 ? View.INVISIBLE: View.VISIBLE);
        } else {
            tvNoResultsFound.setVisibility(aroundEvents.length() > 0 ? View.INVISIBLE: View.VISIBLE);
        }

        mFeedEventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;
        mSwipeRefreshLayout.setRefreshing(false);

        if (response == null) {
            commonUtils.showAlertDialog(mContext, "Please check your internet connection status");
            return;
        }

        if (request_code == GET_FEED_EVENTS_HUB_REQUEST_CODE) {
            JSONArray  jEvents    = commonUtils.getJSONArrayFromJSONObject(response, EVENTS);
            JSONObject meta       = commonUtils.getJSONObjectFromJSONObject(response, META);
            JSONObject pagination = commonUtils.getJSONObjectFromJSONObject(meta, PAGINATION);
            int total_pages  = commonUtils.getIntFromJSONObject(pagination, TOTAL_PAGES);
            int current_page = commonUtils.getIntFromJSONObject(pagination, CURRENT_PAGE);

            hubEvents = commonUtils.addJSONArrayToJSONArray(hubEvents, jEvents);

            if (total_pages > current_page) {
                JSONObject links = commonUtils.getJSONObjectFromJSONObject(pagination, LINKS);
                hubEventsNextUrl = commonUtils.getStringFromJSONObject(links, NEXT);
            } else {
                hubEventsNextUrl = null;
            }
        } else if (request_code == GET_FEED_EVENTS_AROUND_REQUEST_CODE) {
            JSONArray  jEvents    = commonUtils.getJSONArrayFromJSONObject(response, EVENTS);
            JSONObject meta       = commonUtils.getJSONObjectFromJSONObject(response, META);
            JSONObject pagination = commonUtils.getJSONObjectFromJSONObject(meta, PAGINATION);
            int total_pages  = commonUtils.getIntFromJSONObject(pagination, TOTAL_PAGES);
            int current_page = commonUtils.getIntFromJSONObject(pagination, CURRENT_PAGE);

            aroundEvents = commonUtils.addJSONArrayToJSONArray(aroundEvents, jEvents);

            if (total_pages > current_page) {
                JSONObject links = commonUtils.getJSONObjectFromJSONObject(pagination, LINKS);
                aroundEventsNextUrl = commonUtils.getStringFromJSONObject(links, NEXT);
            } else {
                aroundEventsNextUrl = null;
            }
        }

        displayEvents();
    }

    @Override
    public void onBusiness(JSONObject business) {
        stopTask();

        Intent intent = new Intent(mContext, Business.class);
        intent.putExtra(BUSINESS, business.toString());
        startActivity(intent);
    }

    @Override
    public void onPhoto(View view, JSONObject event) {
        Intent intent = new Intent(mContext, Deal.class);
        intent.putExtra(FEED, event.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        ActivityOptions options;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "image");
            startActivity(intent, options.toBundle());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onShare(JSONObject offer) {
        JSONObject picture  = commonUtils.getJSONObjectFromJSONObject(offer, PICTURE);

        if (picture != null) {
            String photoUrl = commonUtils.getStringFromJSONObject(picture, KEY_URL);

            new GetBitmapTask().execute(photoUrl);
        }
    }

    private void shareImage(Bitmap bitmap){

        if (bitmap == null) return;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            bitmapToShare = bitmap;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            String pathofBmp = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "title", null);
            Uri bmpUri = Uri.parse(pathofBmp);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share to"));
        }
    }

    private class GetBitmapTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(mContext)
                        .load(url)
                        .asBitmap()
                        .into(-1, -1)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, e.getMessage());
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            shareImage(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareImage(bitmapToShare);
            }
        }
    }
}