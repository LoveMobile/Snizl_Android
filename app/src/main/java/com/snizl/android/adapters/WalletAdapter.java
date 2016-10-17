package com.snizl.android.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.snizl.android.AppConfig;
import com.snizl.android.R;
import com.snizl.android.utilities.CommonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> implements AppConfig {
    private Context context;
    private JSONArray wallets;

    WeakReference<WalletActionListener> listener;

    public interface WalletActionListener {
        void onBusiness(JSONObject business);
        void onPhoto(View view, JSONObject offer);
        void onShare(JSONObject offer);
        void onDelete(JSONObject wallet);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.business_profile_container) LinearLayout businessProfileContainer;
        @BindView(R.id.share_container)        LinearLayout shareContainer;
        @BindView(R.id.iv_logo)        ImageView ivLogo;
        @BindView(R.id.iv_delete)      ImageView ivDelete;
        @BindView(R.id.iv_photo)       ImageView ivPhoto;
        @BindView(R.id.tv_name)        TextView  tvName;
        @BindView(R.id.tv_post_days)   TextView  tvPostDays;
        @BindView(R.id.tv_title)       TextView  tvTitle;
        @BindView(R.id.tv_left_days)   TextView  tvLeftDays;
        @BindView(R.id.tv_share)       TextView  tvShare;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public WalletAdapter(Context context, JSONArray wallets, WalletActionListener listener) {
        this.context  = context;
        this.wallets   = wallets;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_wallet_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CommonUtils commonUtils = CommonUtils.getInstance();

        final JSONObject wallet = commonUtils.getJSONObjectFromJSONArray(wallets, position);
        final JSONObject offer = commonUtils.getJSONObjectFromJSONObject(wallet, OFFER);
        final JSONObject event = commonUtils.getJSONObjectFromJSONObject(wallet, EVENT);;
        Boolean isOffer = true;

        if (offer == null) {
            isOffer = false;
        }



        ////        Set OnClickListener
        final Boolean finalIsOffer = isOffer;
        holder.businessProfileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onBusiness(commonUtils.getJSONObjectFromJSONObject(finalIsOffer ? offer : event, BUSINESS));
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.get().onDelete(wallet);
            }
        });

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onPhoto(v, finalIsOffer ? offer : event);
            }
        });

        holder.tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onShare(finalIsOffer ? offer : event);
            }
        });

        JSONObject business = commonUtils.getJSONObjectFromJSONObject(finalIsOffer ? offer : event, BUSINESS);
        JSONObject picture  = commonUtils.getJSONObjectFromJSONObject(finalIsOffer ? offer : event, PICTURE);
        JSONObject logo     = commonUtils.getJSONObjectFromJSONObject(business, LOGO);
        JSONObject feed     = commonUtils.getJSONObjectFromJSONObject(finalIsOffer ? offer : event, FEED);

        String name      = commonUtils.getStringFromJSONObject(business, NAME);
        String title     = commonUtils.getStringFromJSONObject(finalIsOffer ? offer : event, TITLE);
        String startDate = commonUtils.getStringFromJSONObject(feed, START);
        String endDate   = commonUtils.getStringFromJSONObject(feed, END);
        String postDays  = commonUtils.calcDifferenceDate(startDate, 1);
        String leftDays  = commonUtils.calcDifferenceDate(endDate, 2);
        String endDays   = commonUtils.calcDifferenceDate(endDate, 3);
        boolean isEnded    = commonUtils.isEnded(endDate);

        if (logo == null) {
            holder.ivLogo.setImageResource(R.drawable.img_no_logo);
        } else {
            String logoUrl = commonUtils.getStringFromJSONObject(logo, KEY_URL);
            Glide.with(context)
                    .load(logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(holder.ivLogo);
        }

        holder.tvName.setText(name);
        holder.tvTitle.setText(title);

        if (picture == null || isEnded) {
            holder.ivPhoto.setVisibility(View.GONE);
        } else {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            String photoUrl = commonUtils.getStringFromJSONObject(picture, KEY_URL);
            Glide.with(context)
                    .load(photoUrl)
                    .placeholder(R.drawable.img_loading)
                    .error(R.drawable.img_no_picture)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.ivPhoto);
        }

        if (isEnded) {
            holder.tvPostDays.setText(endDays);
            holder.shareContainer.setVisibility(View.GONE);
        } else {
            holder.tvPostDays.setText(postDays);
            holder.tvLeftDays.setText(leftDays);
            holder.shareContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return wallets.length();
    }
}
