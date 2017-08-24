package com.example.lixiang.music_player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.afollestad.aesthetic.AestheticActivity;

public class netMusicActivity extends AestheticActivity {
    private DismissReceiver dismissReceiver;
    private netListAdapter adapter;
    private RecyclerView net;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_music);

        //动态注册广播
        dismissReceiver = new DismissReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("dismiss_dialog");
        registerReceiver(dismissReceiver, intentFilter);


        TextView title = (TextView) findViewById(R.id.net_title);
        title.setText("以下音乐版权属于"+Data.getNetMusicList().get(0).getType());
        net = (RecyclerView) findViewById(R.id.netRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        net.setLayoutManager(layoutManager);
        adapter = new netListAdapter();
        net.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(dismissReceiver);
        super.onDestroy();
    }

    private class DismissReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.dismissDialog();
            Snackbar.make(net,"在线播放仅作为预览，请在"+Data.getNetMusicList().get(0).getType()+"下载正版音乐后播放",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
        }
    }
}
