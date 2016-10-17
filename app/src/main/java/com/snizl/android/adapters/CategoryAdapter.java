package com.snizl.android.adapters;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.snizl.android.AppConfig;
import com.snizl.android.R;
import com.snizl.android.views.user.Preferences.CategoryObject;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> implements AppConfig {
    private ArrayList<CategoryObject> categoryObjects;

    WeakReference<CheckedChangeListener> listener;

    public interface CheckedChangeListener {
        void onCheckedChange(long id, boolean isChecked);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_category_name) TextView tvCategoryName;
        @BindView(R.id.cb_category)      AppCompatCheckBox cbCategory;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public CategoryAdapter(ArrayList<CategoryObject> categoryObjects, CheckedChangeListener listener) {
        this.categoryObjects = categoryObjects;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_category_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CategoryObject category = categoryObjects.get(position);

        holder.tvCategoryName.setText(category.getName());

        holder.cbCategory.setOnCheckedChangeListener(null);
        holder.cbCategory.setChecked(category.isPreference());
        holder.cbCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                category.setPreference(isChecked);
                listener.get().onCheckedChange(category.getId(), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryObjects.size();
    }

    public ArrayList<CategoryObject> getItems() {
        return categoryObjects;
    }
}
