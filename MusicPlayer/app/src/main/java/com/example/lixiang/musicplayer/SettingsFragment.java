package com.example.lixiang.musicplayer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;


import es.dmoral.toasty.Toasty;

import static android.R.attr.duration;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (key.equals("default_color")) {
            Aesthetic.get().colorPrimary(sharedPref.getInt("default_color", 0)).colorStatusBarAuto().colorNavigationBarAuto().apply();
        }
        if (key.equals("accent_color")) {
            Aesthetic.get().colorAccent(sharedPref.getInt("accent_color", 0)).colorStatusBarAuto().colorNavigationBarAuto().apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
