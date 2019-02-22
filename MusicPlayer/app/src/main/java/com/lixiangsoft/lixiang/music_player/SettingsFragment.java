package com.lixiangsoft.lixiang.music_player;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;
import com.bumptech.glide.Glide;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
                addPreferencesFromResource(R.xml.preference);
                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(SettingsFragment.this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (key.equals("local_net_mode")) {
            MyApplication.setLocal_net_mode(sharedPref.getBoolean("local_net_mode", false));
        }
        if (key.equals("default_color")) {
            Log.e("测试", "设置ColorPrimary前" + MyApplication.getColor_primary());
            int color_primary = sharedPref.getInt("default_color", 0);
            Aesthetic.get().colorPrimary(color_primary).colorStatusBarAuto().colorNavigationBar(color_primary).apply();
            MyApplication.setColor_primary(color_primary);
            Log.e("测试", "设置ColorPrimary后" + MyApplication.getColor_primary());
        }
        if (key.equals("accent_color")) {
            int color_accent = sharedPref.getInt("accent_color", 0);
            Aesthetic.get().colorAccent(color_accent).colorStatusBarAuto().apply();
            MyApplication.setColor_accent(color_accent);
        }
        if (key.equals("main_theme")) {
            Aesthetic.get().isDark(true).activityTheme(R.style.AppDarkTheme).colorCardViewBackgroundRes(R.color.night_cardview).apply();
            MyApplication.setMain_theme(sharedPref.getString("main_theme", "day"));
            if (sharedPref.getString("main_theme", null).equals("night")) {
                Aesthetic.get().isDark(true).activityTheme(R.style.AppDarkTheme).colorCardViewBackgroundRes(R.color.night_cardview).apply();
            } else if (sharedPref.getString("main_theme", null).equals("day")) {
                Aesthetic.get().isDark(false).activityTheme(R.style.AppTheme).colorCardViewBackgroundRes(R.color.day_cardview).apply();
            } else {
                Aesthetic.get().isDark(true).activityTheme(R.style.AppAmoledTheme).colorCardViewBackgroundRes(R.color.night_cardview).apply();
            }
        }
    }


    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
