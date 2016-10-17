package com.snizl.android.views.user;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.adapters.CategoryAdapter;
import com.snizl.android.adapters.CategoryAdapter.CheckedChangeListener;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Preferences extends BaseActivity implements OnTaskCompleted, CheckedChangeListener {
    private final int GET_CATEGORIES_REQUEST_CODE       = 0;
    private final int GET_USER_CATEGORIES_REQUEST_CODE  = 1;
    private final int PUT_USER_CATEGORY_REQUEST_CODE    = 2;
    private final int DELETE_USER_CATEGORY_REQUEST_CODE = 3;

    @BindView(R.id.rv_categories) RecyclerView mRecyclerView;

    @OnClick(R.id.iv_back)
    public void onBack() {
        if (isLoadingBase) return;
        finish();
    }

    @OnClick(R.id.btn_save)
    public void onSave() {
        if (isLoadingBase) return;

        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Updating User Preferences");
        mProgressDialog.show();

        performSave();
    }

//    *****************************************************************************
    private int request_code;
    private long user_id;
    private JSONArray jsonArrayCategories, jsonArrayUserCategories;
    private ArrayList<CategoryObject> categories    = new ArrayList<>();
    private ArrayList<Long>           checkedList   = new ArrayList<>(), originalCheckedList   = new ArrayList<>();
    private ArrayList<Long>           unCheckedList = new ArrayList<>(), originalUncheckedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ButterKnife.bind(this);

        initData();
        initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGetDataTask.cancel(true);
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void initData() {
        user_id = commonUtils.getLongFromJSONObject(AppController.currentUser, ID);

        getCategories();
    }

    private void initUI() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void displayCategories() {
        for (int i = 0; i < jsonArrayCategories.length(); i++) {
            JSONObject category = commonUtils.getJSONObjectFromJSONArray(jsonArrayCategories, i);
            CategoryObject categoryObject = new CategoryObject();
            categoryObject.setId(commonUtils.getLongFromJSONObject(category, ID));
            categoryObject.setName(commonUtils.getStringFromJSONObject(category, NAME));
            categoryObject.setPreference(commonUtils.isExistInJSONArray(jsonArrayUserCategories, ID, categoryObject.getId()));

            if (categoryObject.isPreference()) {
                originalCheckedList.add(categoryObject.getId());
            } else {
                originalUncheckedList.add(categoryObject.getId());
            }

            categories.add(categoryObject);
        }

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, this);
        mRecyclerView.setAdapter(categoryAdapter);
    }

    private void getCategories() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_CATEGORIES_REQUEST_CODE;

        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Loading User Preferences");
        mProgressDialog.show();

        mGetDataTask = new GetDataTask(API_URL_GET_CATEGORIES, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void getUserCategories() {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = GET_USER_CATEGORIES_REQUEST_CODE;

        String url = API_URL_GET_USER_CATEGORIES.replace("user_id", String.valueOf(user_id));

        mGetDataTask = new GetDataTask(url, headers, null, GET_METHOD, this);
        mGetDataTask.execute();
    }

    private void performSave() {
        long id;

        if (checkedList.size() > 0) {
            id = checkedList.get(0);
            checkedList.remove(0);
            putOrDeleteCategory(id, true);
        } else if (unCheckedList.size() > 0) {
            id = unCheckedList.get(0);
            unCheckedList.remove(0);
            putOrDeleteCategory(id, false);
        } else {
            mProgressDialog.dismiss();
            onBack();
        }
    }

    private void putOrDeleteCategory(long id, boolean isPut) {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        isLoadingBase = true;
        request_code = isPut ? PUT_USER_CATEGORY_REQUEST_CODE: DELETE_USER_CATEGORY_REQUEST_CODE;

        String url = API_URL_GET_USER_CATEGORIES.replace("user_id", String.valueOf(user_id)) + "/" + id;

        mGetDataTask = new GetDataTask(url, headers, null, isPut ? PUT_METHOD: DELETE_METHOD, this);
        mGetDataTask.execute();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (response == null) {
            if (request_code == PUT_USER_CATEGORY_REQUEST_CODE || request_code == DELETE_USER_CATEGORY_REQUEST_CODE) {
                performSave();
            } else {
                mProgressDialog.dismiss();
                return;
            }
        }

        if (request_code == GET_CATEGORIES_REQUEST_CODE) {
            jsonArrayCategories = commonUtils.getJSONArrayFromJSONObject(response, CATEGORIES);
            getUserCategories();
        } else if (request_code == GET_USER_CATEGORIES_REQUEST_CODE) {
            mProgressDialog.dismiss();
            jsonArrayUserCategories = commonUtils.getJSONArrayFromJSONObject(response, CATEGORIES);
            displayCategories();
        }
    }

    @Override
    public void onCheckedChange(long id, boolean isChecked) {
        if (checkedList.contains(id) && isChecked) return;
        if (unCheckedList.contains(id) && !isChecked) return;
        if (originalCheckedList.contains(id) && isChecked) {
            unCheckedList.remove(id);
            return;
        }
        if (originalUncheckedList.contains(id) && !isChecked) {
            checkedList.remove(id);
            return;
        }

        if (isChecked) {
            checkedList.add(id);
        } else {
            unCheckedList.add(id);
        }
    }

    public class CategoryObject {
        private long id;
        private String name;
        private boolean isPreference;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isPreference() {
            return isPreference;
        }

        public void setPreference(boolean preference) {
            isPreference = preference;
        }
    }
}
