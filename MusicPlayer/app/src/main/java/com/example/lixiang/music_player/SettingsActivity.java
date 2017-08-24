package com.example.lixiang.music_player;
import android.os.Bundle;

import com.afollestad.aesthetic.AestheticActivity;


public class SettingsActivity extends AestheticActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }



}
