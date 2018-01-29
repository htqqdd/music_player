package com.example.lixiang.music_player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class customActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new customFragment())
                .commit();
    }
}
