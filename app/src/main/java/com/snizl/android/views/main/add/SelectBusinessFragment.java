package com.snizl.android.views.main.add;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.AddBusinessAdapter;
import com.snizl.android.utilities.RecyclerItemClickListener;
import com.snizl.android.views.base.BaseFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectBusinessFragment extends BaseFragment {
    @BindView(R.id.rv_businesses) RecyclerView mRecyclerView;

    private Context mContext;
    private JSONArray businesses;
    private ArrayList<Boolean> selectedList;
    private AddBusinessAdapter mAddBusinessAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_business, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    private void initData() {
        mContext = getContext();
        businesses = new JSONArray();
        commonUtils.addJSONArrayToJSONArray(businesses, AppController.businesses);

        selectedList = new ArrayList<>();
        resetSelected(-1);
    }

    private void initUI() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AppController.seleted_business_index = position;
                resetSelected(position);
                displayBusinesses();
            }
        }));
        mAddBusinessAdapter = new AddBusinessAdapter(mContext, businesses, selectedList);
        mRecyclerView.setAdapter(mAddBusinessAdapter);

        displayBusinesses();
    }

    private void displayBusinesses() {
        mAddBusinessAdapter.notifyDataSetChanged();
    }

    private void resetSelected(int position) {
        selectedList.clear();
        for (int i = 0; i < businesses.length(); i++) {
            selectedList.add(i, i == position);
        }
    }
}