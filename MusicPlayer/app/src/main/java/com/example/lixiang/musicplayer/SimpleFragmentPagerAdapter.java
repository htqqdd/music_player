package com.example.lixiang.musicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RecommendFragment();
        } else if (position == 1) {
            return new MusiclistFragment();
        } else {
            return new DownloadFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
//        if (position == 0) {
//            return "";
//        } else if (position == 1) {
//            return "";
//        } else{
//            return "";
//        }
        return "";
    }
}
