package com.example.lixiang.musicplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static com.example.lixiang.musicplayer.Data.mediaChangeAction;

public class sc_list extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent("play_broadcast");
        intent.putExtra("viewPagerChange", 1);
        sendBroadcast(intent);
        finish();
    }
}
