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
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.FeedOfferAdapter;
import com.snizl.android.adapters.FeedOfferAdapter.ActionListener;
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
import butterknife.OnClick;

public class OfferFragment extends BaseFragment implements OnTaskCompleted, ActionListener{
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @BindView(R.id.tv_no_results_found) TextView tvNoResultsFound;

    @BindView(R.id.indicator_pref) View indicatorPref;
    @OnClick(R.id.iv_pref)
    public void onClickPref(ImageView view) {
        if (indicatorPref.getVisibility() == View.VISIBLE) {
            indicatorPref.setVisibility(View.INVISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorRedDark));
        } else {
            indicatorPref.setVisibility(View.VISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white));
        }

        bPref = !bPref;

        if (!mGetDataTask.isCancelled()) {
            mGetDataTask.cancel(true);
        }

        refresh();
    }

    @BindView(R.id.indicator_location) View indicatorLocation;
    @OnClick(R.id.iv_location)
    public void onClickLocation(ImageView view) {
        if (indicatorLocation.getVisibility() == View.VISIBLE) {
            indicatorLocation.setVisibility(View.INVISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorRedDark));
        } else {
            indicatorLocation.setVisibility(View.VISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white));
        }

        bLocation = !bLocation;

        if (!mGetDataTask.isCancelled()) {
            mGetDataTask.cancel(true);
        }

        refresh();
    }

    @BindView(R.id.indicator_promo) View indicatorPromo;
    @OnClick(R.id.iv_promo)
    public void onClickPromo(ImageView view) {
        if (indicatorPromo.getVisibility() == View.VISIBLE) {
            indicatorPromo.setVisibility(View.INVISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorRedDark));
        } else {
            indicatorPromo.setVisibility(View.VISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white));
        }

        bPromo = !bPromo;

        if (!mGetDataTask.isCancelled()) {
            mGetDataTask.cancel(true);
        }

        refresh();
    }

    @BindView(R.id.indicator_comp) View indicatorComp;
    @OnClick(R.id.iv_comp)
    public void onClickComp(ImageView view) {
        if (indicatorComp.getVisibility() == View.VISIBLE) {
            indicatorComp.setVisibility(View.INVISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorRedDark));
        } else {
            indicatorComp.setVisibility(View.VISIBLE);
            view.setColorFilter(ContextCompat.getColor(mContext, android.R.color.white));
        }

        bComp = !bComp;

        if (!mGetDataTask.isCancelled()) {
            mGetDataTask.cancel(true);
        }

        refresh();
    }

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.rv_feeds)             RecyclerView mRecyclerView;

//    ***********************************************************************************************
    private Context mContext;
    private boolean bPref, bLocation, bPromo, bComp;
    private Bitmap bitmapToShare;

    private FeedOfferAdapter mFeedOfferAdapter;
    private JSONArray offers = new JSONArray();

    private String loadMoreUrl = null;
    private boolean isAvailableLoadMore = true;

    private EndlessRecyclerOnScrollListener mEndlessScrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppController.updatedHub) {
            getFeeds();
            AppController.updatedHub = false;
        }

        if (mEndlessScrollListener != null)
            mEndlessScrollListener.reset(0, true);
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

    @Override
    public void onPause() {
        super.onPause();
        if (mGetDataTask != null)
            mGetDataTask.cancel(true);
    }

    private void initData() {
        mContext = getContext();

        bPref = bLocation = bPromo = bComp = false;
    }

    private void initUI() {
        setHasOptionsMenu(true);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mEndlessScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (isAvailableLoadMore)
                    getFeeds();
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
        String title = "";
        if (bPref) title += "Deals";
        if (bLocation) title += ((title.isEmpty() ? "" : " & ") + "Location");
        if (bPromo) title += ((title.isEmpty() ? "" : " & ") + "Promos");
        if (bComp) title += ((title.isEmpty() ? "" : " & ") + "Comps");
        if (title.isEmpty()) title = "Deals";

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(title);

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();
        MenuItem drawer_offers = menu.findItem(R.id.drawer_deals);
        drawer_offers.setTitle(title);
    }

    private void refresh() {
        changeTitle();
        offers = new JSONArray();
        mFeedOfferAdapter = new FeedOfferAdapter(mContext, offers, this);
        mRecyclerView.setAdapter(mFeedOfferAdapter);
        mEndlessScrollListener.reset(0, true);
        isAvailableLoadMore = true;
        loadMoreUrl = null;

        getFeeds();
    }

    private void getFeeds() {
        String access_token, token_type, type;
        long hub_id = getUserHubId();

        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);
        type = "offer" + (bPromo ? "|promo" : "") + (bComp ? "|comp" : "");

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        ContentValues body = new ContentValues();
        body.put(BODY_TYPE, type);
        body.put(BODY_PREF, bPref);
        body.put(BODY_HUB, hub_id);

        if (bLocation) {
            if (AppController.mLocation != null) {
                body.put(BODY_LAT,  AppController.mLocation.getLatitude());
                body.put(BODY_LONG, AppController.mLocation.getLongitude());
            } else {
                body.put(BODY_LAT,  0);
                body.put(BODY_LONG, 0);
            }
        }

        isLoadingBase = true;

        String url = FEED_OFFERS_URL + "?" + commonUtils.getQuery(body);

        if (loadMoreUrl != null) {
            url = loadMoreUrl;
            loadMoreUrl = null;
        }

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void displayOffers() {
        tvNoResultsFound.setVisibility(offers.length() > 0 ? View.INVISIBLE: View.VISIBLE);
        mFeedOfferAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;
        mProgressDialog.dismiss();
        mSwipeRefreshLayout.setRefreshing(false);


        if (response == null) {
            commonUtils.showAlertDialog(mContext, "No Results Found");
            return;
        }

        JSONArray jsonArrayOffers = commonUtils.getJSONArrayFromJSONObject(response, OFFERS);
        JSONObject meta = commonUtils.getJSONObjectFromJSONObject(response, META);
        JSONObject pagination = commonUtils.getJSONObjectFromJSONObject(meta, PAGINATION);
        int total_pages  = commonUtils.getIntFromJSONObject(pagination, TOTAL_PAGES);
        int current_page = commonUtils.getIntFromJSONObject(pagination, CURRENT_PAGE);

        if (total_pages > current_page) {
            JSONObject links = commonUtils.getJSONObjectFromJSONObject(pagination, LINKS);
            loadMoreUrl = commonUtils.getStringFromJSONObject(links, NEXT);
        } else if (total_pages == current_page) {
            isAvailableLoadMore = false;
        }

        offers = commonUtils.addJSONArrayToJSONArray(offers, jsonArrayOffers);

        displayOffers();
    }

    @Override
    public void onBusiness(JSONObject business) {
        mGetDataTask.cancel(true);

        Intent intent = new Intent(mContext, Business.class);
        intent.putExtra(BUSINESS, business.toString());
        startActivity(intent);
    }

    @Override
    public void onPhoto(View v, JSONObject offer) {
        mGetDataTask.cancel(true);

        Intent intent = new Intent(mContext, Deal.class);
        intent.putExtra(FEED, offer.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        ActivityOptions options;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), v, "image");
            startActivity(intent, options.toBundle());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
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