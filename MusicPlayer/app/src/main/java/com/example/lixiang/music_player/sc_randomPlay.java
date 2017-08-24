package com.example.lixiang.music_player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static com.example.lixiang.music_player.Data.sc_playAction;

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
