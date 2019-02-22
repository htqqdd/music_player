package com.lixiangsoft.lixiang.music_player;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lixiangsoft.lixiang.music_player.EventBusUtil.showListEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment {

//    private PermissionReceiver permissionReceiver;
    private View rootView;
    private boolean permissionGranted = false;


    public RecommendFragment() {
        // Required empty public constructor
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(showListEvent list) {
        Log.e("测试","Recommend收到消息");
        if (list.arg1 == 0){
            showRecentList();
        }else if (list.arg1 == 1){
            showFavouriteList();
        }else if (list.arg1 == 2){
            showRecentList();
            showFavouriteList();
        }
    };


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        rootView = inflater.inflate(R.layout.fragment_recommend, container, false);

        //动态注册广播
//        permissionReceiver = new PermissionReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("permission_granted");
//        getActivity().registerReceiver(permissionReceiver, intentFilter);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            permissionGranted = true;
            showRecentList();
            showFavouriteList();
        }
        return rootView;
    }

//    private class PermissionReceiver extends BroadcastReceiver{
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getIntExtra("Action",0) == 1) {
//                showRecentList();
//            }else if (intent.getIntExtra("Action",0) == 2){
//                showFavouriteList();
//            }else {
//                showRecentList();
//                showFavouriteList();
//            }
//        }
//    }
    private void showRecentList(){
        RecyclerView recent = (RecyclerView) rootView.findViewById(R.id.recent_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recent.setLayoutManager(layoutManager);
        RecentListAdapter adapter = new RecentListAdapter();
        recent.setAdapter(adapter);
    }
    private void showFavouriteList(){
        RecyclerView favourite = (RecyclerView) rootView.findViewById(R.id.favourite_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),3);
        favourite.setLayoutManager(layoutManager);
        FavouriteListAdapter adapter = new FavouriteListAdapter();
        favourite.setAdapter(adapter);
        //与Scrollview滑动冲突
        favourite.setNestedScrollingEnabled(false);
        favourite.setHasFixedSize(true);
    }

    public static void openSAF(Activity context) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        context.startActivityForResult(intent, 42);

    }
}
