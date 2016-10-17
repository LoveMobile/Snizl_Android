package com.snizl.android.views.deal;

import android.content.ContentValues;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetStringDataTask;
import com.snizl.android.api.OnStringTaskCompleted;
import com.snizl.android.libraries.AspectRatioImageView;
import com.snizl.android.libraries.svg.SvgDecoder;
import com.snizl.android.libraries.svg.SvgDrawableTranscoder;
import com.snizl.android.libraries.svg.SvgSoftwareLayerSetter;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClaimInstore extends BaseActivity implements OnStringTaskCompleted{
    @BindView(R.id.iv_feature) ImageView ivFeature;
    @BindView(R.id.iv_qr_code) ImageView ivQRCode;
    @BindView(R.id.iv_logo)    ImageView ivLogo;
    @BindView(R.id.iv_picture) AspectRatioImageView ivPicture;
    @BindView(R.id.tv_title)   TextView tvTitle;
    @BindView(R.id.tv_description) TextView tvDescription;

    private JSONObject offer;
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_instore);

        ButterKnife.bind(this);

        try {
            offer = new JSONObject(getIntent().getStringExtra(OFFER));

            initData();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void initData() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type   = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;

        String url = API_URL_OFFERS + "/" + commonUtils.getLongFromJSONObject(offer, ID) + "/qrcode";

        mGetStringDataTask = new GetStringDataTask(url, headers, null, GET_METHOD, this);
        mGetStringDataTask.execute();

        requestBuilder = Glide.with(this)
                .using(Glide.buildStreamModelLoader(Uri.class, this), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());

        initUI();
    }

    private void initUI() {
        JSONObject picture, business, logo = null, featureimage = null;

        picture  = commonUtils.getJSONObjectFromJSONObject(offer, PICTURE);
        business = commonUtils.getJSONObjectFromJSONObject(offer, BUSINESS);

        if (business != null) {
            logo = commonUtils.getJSONObjectFromJSONObject(business, LOGO);
            featureimage = commonUtils.getJSONObjectFromJSONObject(business, FEATUREIMAGE);
        }

        if (picture != null) {
            String url = commonUtils.getStringFromJSONObject(picture, KEY_URL);
            ivPicture.setUrl(this, url);
        }

        tvTitle.setText(commonUtils.getStringFromJSONObject(offer, TITLE));
        tvDescription.setText(commonUtils.getStringFromJSONObject(offer, DESCRIPTION));

        if (logo != null) {
            String url = commonUtils.getStringFromJSONObject(logo, KEY_URL);
            Glide.with(this)
                    .load(url)
                    .asBitmap()
                    .placeholder(R.drawable.img_no_logo)
                    .centerCrop()
                    .into(ivLogo);
        }

        if (featureimage != null) {
            String url = commonUtils.getStringFromJSONObject(featureimage, KEY_URL);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.img_loading)
                    .error(R.drawable.img_no_picture)
                    .centerCrop()
                    .into(ivFeature);
        }
    }

    @Override
    public void onStringDataReceived(String response) {
        isLoadingBase = false;

        if (response.isEmpty()) return;

        Uri uri = Uri.parse(response);
        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .load(uri)
                .into(ivQRCode);
    }
}
