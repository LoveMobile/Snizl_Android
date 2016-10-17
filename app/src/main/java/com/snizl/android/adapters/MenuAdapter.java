package com.snizl.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.snizl.android.R;

import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter{

    private Context context;
    private String[] titles;
    private ArrayList<Integer> icons;

    public MenuAdapter(Context context, String[] titles, ArrayList<Integer> icons){
        this.context = context;
        this.titles = titles;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_menu_top_item, null);
        }

        TextView mMenuTitle = (TextView) convertView.findViewById(R.id.tv_title);
        mMenuTitle.setText(titles[position]);

        ImageView mMenuIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
        mMenuIcon.setImageResource(icons.get(position));

        return convertView;
    }
}
