package com.snizl.android.views.business;

import android.Manifest;
import android.app.ActivityOptions;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.snizl.android.R;
import com.snizl.android.adapters.BusinessEventAdapter;
import com.snizl.android.adapters.BusinessEventAdapter.BusinessEventActionListener;
import com.snizl.android.views.base.BaseFragment;
import com.snizl.android.views.deal.Deal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BusinessEventsFragment extends BaseFragment implements BusinessEventActionListener {
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @BindView(R.id.tv_no_results_found) TextView tvNoResultsFound;
    @BindView(R.id.rv_events)           RecyclerView mRecyclerView;

    private Context mContext;
    private Bitmap bitmapToShare;

    private BusinessEventAdapter mBusinessEventAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_events, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    private void initData() {
        mContext = getContext();
        JSONArray events = ((Business)getActivity()).events;
        mBusinessEventAdapter = new BusinessEventAdapter(mContext, events, this);

        tvNoResultsFound.setVisibility(events.length() > 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void initUI() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mBusinessEventAdapter);
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
    public void onShare(JSONObject event) {
        JSONObject picture  = commonUtils.getJSONObjectFromJSONObject(event, PICTURE);

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