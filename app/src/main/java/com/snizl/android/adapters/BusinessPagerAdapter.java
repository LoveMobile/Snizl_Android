package com.snizl.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.snizl.android.views.business.BusinessAboutFragment;
import com.snizl.android.views.business.BusinessDealsFragment;
import com.snizl.android.views.business.BusinessEventsFragment;

public class BusinessPagerAdapter extends FragmentStatePagerAdapter {
    public BusinessPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new BusinessDealsFragment();
                break;
            case 1:
                fragment = new BusinessEventsFragment();
                break;
            case 2:
                fragment = new BusinessAboutFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title=" ";
        switch (position){
            case 0:
                title="Deals";
                break;
            case 1:
                title="Events";
                break;
            case 2:
                title="About";
                break;
        }

        return title;
    }
}
