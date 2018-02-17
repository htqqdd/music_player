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
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;



/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {

    private FastScrollRecyclerView fastScrollRecyclerView;
    private list_PermissionReceiver list_permissionReceiver;
    private FastScrollListAdapter adapter;


    public MusiclistFragment() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(list_permissionReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //动态注册广播
        list_permissionReceiver = new list_PermissionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("list_permission_granted");
        getActivity().registerReceiver(list_permissionReceiver, intentFilter);

        View rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);
        fastScrollRecyclerView = (FastScrollRecyclerView) rootView.findViewById(R.id.fastScrollRecyclerView);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showMusicList();
        }

        return rootView;

    }

    @Override
    public void onStart() {
        new getColorTask().execute();
        super.onStart();
    }


    private class list_PermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //显示界面
            showMusicList();
        }
    }


    private class getColorTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int accent_color = sharedPref.getInt("accent_color", 0);
            if (accent_color != 0){
                return accent_color;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o == null){
                fastScrollRecyclerView.setThumbColor(getResources().getColor(R.color.colorAccent));
                fastScrollRecyclerView.setPopupBgColor(getResources().getColor(R.color.colorAccent));
            }else {
                fastScrollRecyclerView.setThumbColor((int) o);
                fastScrollRecyclerView.setPopupBgColor((int) o);
            }
        }
    }

    private void showMusicList() {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            fastScrollRecyclerView.setLayoutManager(layoutManager);
            adapter = new FastScrollListAdapter();
            fastScrollRecyclerView.setAdapter(adapter);
    }

}
