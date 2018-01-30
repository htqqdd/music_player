package com.lixiangsoft.lixiang.music_player;
import android.os.Bundle;

import com.afollestad.aesthetic.AestheticActivity;


public class SettingsActivity extends AestheticActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_toolbar);

//        // Display the fragment as the main content.
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();
        android.support.v7.widget.Toolbar settings_toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settings_toolbar);
        settings_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        settings_toolbar.setTitle("设置");
        setSupportActionBar(settings_toolbar);//设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();
    }



}
