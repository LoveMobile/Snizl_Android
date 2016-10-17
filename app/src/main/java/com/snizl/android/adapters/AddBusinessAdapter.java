package com.snizl.android.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddBusinessAdapter extends RecyclerView.Adapter<AddBusinessAdapter.ViewHolder> implements AppConfig {
    private Context context;
    private JSONArray businesses;
    private ArrayList<Boolean> selectedList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.business_container) LinearLayout mContainer;
        @BindView(R.id.iv_logo)            ImageView ivLogo;
        @BindView(R.id.tv_business_name)   TextView tvBusinessName;
        @BindView(R.id.tv_hub_name)        TextView tvHubName;
        @BindView(R.id.tv_number)          TextView tvNumber;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public AddBusinessAdapter(Context context, JSONArray businesses, ArrayList<Boolean> selectedList) {
        this.context  = context;
        this.businesses   = businesses;
        this.selectedList = selectedList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_business_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CommonUtils commonUtils = CommonUtils.getInstance();

        final JSONObject business = commonUtils.getJSONObjectFromJSONArray(businesses, position);

        JSONObject logo     = commonUtils.getJSONObjectFromJSONObject(business, LOGO);
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

        String businessName      = commonUtils.getStringFromJSONObject(business, NAME);
        holder.tvBusinessName.setText(businessName);

        JSONObject hub = commonUtils.getJSONObjectFromJSONObject(business, HUB);
        if (hub != null) {
            String hubName = commonUtils.getStringFromJSONObject(hub, NAME);
            holder.tvHubName.setText(hubName);
        }

        holder.mContainer.setBackgroundColor(ContextCompat.getColor(context, selectedList.get(position) ? R.color.colorPrimary : android.R.color.white));
        holder.tvBusinessName.setTextColor(ContextCompat.getColor(context, selectedList.get(position) ? android.R.color.white : android.R.color.black));
        holder.tvHubName.setTextColor(ContextCompat.getColor(context, selectedList.get(position) ? android.R.color.white : android.R.color.black));
        holder.tvNumber.setTextColor(ContextCompat.getColor(context, selectedList.get(position) ? android.R.color.white : android.R.color.black));
    }

    @Override
    public int getItemCount() {
        return businesses.length();
    }
}
