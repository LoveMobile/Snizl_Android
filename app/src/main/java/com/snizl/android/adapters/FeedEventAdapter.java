package com.snizl.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class FeedEventAdapter extends RecyclerView.Adapter<FeedEventAdapter.ViewHolder> implements AppConfig {
    private Context context;
    private JSONArray events;

    WeakReference<FeedEventActionListener> listener;

    public interface FeedEventActionListener {
        void onBusiness(JSONObject business);
        void onPhoto(View view, JSONObject event);
        void onShare(JSONObject event);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.business_profile_container) LinearLayout businessProfileContainer;
        @BindView(R.id.share_container)            LinearLayout shareContainer;
        @BindView(R.id.iv_logo)        ImageView ivLogo;
        @BindView(R.id.iv_photo)       ImageView ivPhoto;
        @BindView(R.id.tv_name)        TextView tvName;
        @BindView(R.id.tv_post_days)   TextView tvPostDays;
        @BindView(R.id.tv_title)       TextView tvTitle;
        @BindView(R.id.tv_left_days)   TextView tvLeftDays;
        @BindView(R.id.btn_share)      Button btnShare;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public FeedEventAdapter(Context context, JSONArray events, FeedEventActionListener listener) {
        this.context  = context;
        this.events   = events;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_feed_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CommonUtils commonUtils = CommonUtils.getInstance();

        final JSONObject event = commonUtils.getJSONObjectFromJSONArray(events, position);

        ////        Set OnClickListener
        holder.businessProfileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onBusiness(commonUtils.getJSONObjectFromJSONObject(event, BUSINESS));
            }
        });

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onPhoto(v, event);
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.get().onShare(event);
            }
        });

        JSONObject business = commonUtils.getJSONObjectFromJSONObject(event, BUSINESS);
        JSONObject picture  = commonUtils.getJSONObjectFromJSONObject(event, PICTURE);
        JSONObject logo     = commonUtils.getJSONObjectFromJSONObject(business, LOGO);
        JSONObject feed     = commonUtils.getJSONObjectFromJSONObject(event, FEED);
        JSONObject dates    = commonUtils.getJSONObjectFromJSONObject(event, DATES);

        String name      = commonUtils.getStringFromJSONObject(business, NAME);
        String title     = commonUtils.getStringFromJSONObject(event, TITLE);
        String startDate = commonUtils.getStringFromJSONObject(feed, START);
        String endDate   = commonUtils.getStringFromJSONObject(feed, END);
        String postDays  = commonUtils.calcDifferenceDate(startDate, 1);
        String endDays   = commonUtils.calcDifferenceDate(endDate, 3);
        String duration  = commonUtils.convertDateFormat(commonUtils.getStringFromJSONObject(dates, START), commonUtils.getStringFromJSONObject(dates, END));
        boolean isEnded  = commonUtils.isEnded(endDate);

        if (logo == null) {
            holder.ivLogo.setImageResource(R.drawable.img_no_logo);
        } else {
            String logoUrl = commonUtils.getStringFromJSONObject(logo, KEY_URL);
            Glide.with(context)
                    .load(logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .skipMemoryCache(true)
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
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .skipMemoryCache(true)
                    .crossFade()
                    .centerCrop()
                    .into(holder.ivPhoto);
        }

        if (isEnded) {
            holder.tvPostDays.setText(endDays);
            holder.shareContainer.setVisibility(View.GONE);
        } else {
            holder.tvPostDays.setText(postDays);
            holder.tvLeftDays.setText(duration);
            holder.shareContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return events.length();
    }
}
