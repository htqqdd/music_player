package com.lixiangsoft.lixiang.music_player;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lixiangsoft.lixiang.music_player.EventBusUtil.showListEvent;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {
    private FastScrollRecyclerView fastScrollRecyclerView;
//    private list_PermissionReceiver list_permissionReceiver;
    private FastScrollListAdapter adapter;


    public MusiclistFragment() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(showListEvent list) {
        if(list.arg2 == 3){
            showMusicList();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        int color_accent = MyApplication.getColor_accent();
        fastScrollRecyclerView.setThumbColor(color_accent);
        fastScrollRecyclerView.setPopupBgColor(color_accent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
//        getActivity().unregisterReceiver(list_permissionReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);


        View rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);
        fastScrollRecyclerView = (FastScrollRecyclerView) rootView.findViewById(R.id.fastScrollRecyclerView);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showMusicList();
        }

        return rootView;

    }


    private void showMusicList() {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            fastScrollRecyclerView.setLayoutManager(layoutManager);
            adapter = new FastScrollListAdapter();
            fastScrollRecyclerView.setAdapter(adapter);
    }

}
