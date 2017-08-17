package com.example.lixiang.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.sc_playAction;

public class sc_randomPlay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            //随机播放
            Data.setPlayMode(1);
            Data.setFavourite(false);
            Data.setRecent(false);
            //打开服务播放
            Intent intent = new Intent(this, PlayService.class);
            intent.putExtra("ACTION", sc_playAction);
            startService(intent);
            finish();
    }
}
