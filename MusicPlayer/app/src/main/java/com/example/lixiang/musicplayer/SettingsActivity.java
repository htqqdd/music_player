package com.example.lixiang.musicplayer;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.polaric.colorful.CActivity;
public class SettingsActivity extends CActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_toolbar);

        Toolbar settings_toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
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
