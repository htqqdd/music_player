package com.example.lixiang.musicplayer;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;


public class SettingsActivity extends AestheticActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.pref_with_toolbar);

//        Toolbar settings_toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
//        settings_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
//        settings_toolbar.setTitle("设置");
//        setSupportActionBar(settings_toolbar);//设置返回键可用
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }



}
