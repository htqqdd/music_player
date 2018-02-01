package com.lixiangsoft.lixiang.music_player;

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
        } else if (position == 2){
            return new DownloadFragment();
        }else {
            return new customFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "建议";
        } else if (position == 1) {
            return "歌曲";
        } else if (position == 2){
            return "搜索";
        }else {
            return "列表";
        }
    }
}
