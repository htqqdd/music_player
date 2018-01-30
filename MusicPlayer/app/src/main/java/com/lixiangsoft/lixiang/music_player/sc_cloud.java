package com.lixiangsoft.lixiang.music_player;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class sc_cloud extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("viewPagerChange", 2);
        sendBroadcast(intent);
        finish();
    }
}
