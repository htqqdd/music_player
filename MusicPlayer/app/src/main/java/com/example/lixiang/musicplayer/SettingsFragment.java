package com.example.lixiang.musicplayer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import org.polaric.colorful.Colorful;


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
        if (key.equals("suggestion")){
            //重启界面
            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restart_intent);
        }
        if (key.equals("primary_color")) {
            String primary_color = sharedPref.getString("primary_color", "");
            Log.v("颜色","颜色"+primary_color);
            switch (primary_color) {
                case "red":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.RED).apply();
                    break;
                case "pink":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.PINK).apply();
                    break;
                case "purple":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.PURPLE).apply();
                    break;
                case "deep_purple":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.DEEP_PURPLE).apply();
                    break;
                case "indigo":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.INDIGO).apply();
                    break;
                case "blue":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.BLUE).apply();
                    break;
                case "light_blue":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.LIGHT_BLUE).apply();
                    break;
                case "cyan":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.CYAN).apply();
                    break;
                case "teal":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.TEAL).apply();
                    break;
                case "green":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.GREEN).apply();
                    break;
                case "light_green":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.LIGHT_GREEN).apply();
                    break;
                case "lime":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.LIME).apply();
                    break;
                case "yellow":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.YELLOW).apply();
                    break;
                case "amber":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.AMBER).apply();
                    break;
                case "orange":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.ORANGE).apply();
                    break;
                case "deep_orange":
                    Colorful.config(getActivity()).primaryColor(Colorful.ThemeColor.DEEP_ORANGE).apply();
                    break;
                default:
            }
            //重启界面
            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restart_intent);
        }
        if (key.equals("accent_color")) {
            String accent_color = sharedPref.getString("accent_color", "");
            Log.v("颜色","颜色"+accent_color);
            switch (accent_color) {
                case "red":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.RED).apply();
                    break;
                case "pink":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.PINK).apply();
                    break;
                case "purple":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.PURPLE).apply();
                    break;
                case "deep_purple":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.DEEP_PURPLE).apply();
                    break;
                case "indigo":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.INDIGO).apply();
                    break;
                case "blue":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.BLUE).apply();
                    break;
                case "light_blue":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIGHT_BLUE).apply();
                    break;
                case "cyan":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.CYAN).apply();
                    break;
                case "teal":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.TEAL).apply();
                    break;
                case "green":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.GREEN).apply();
                    break;
                case "light_green":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIGHT_GREEN).apply();
                    break;
                case "lime":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIME).apply();
                    break;
                case "yellow":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.YELLOW).apply();
                    break;
                case "amber":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.AMBER).apply();
                    break;
                case "orange":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.ORANGE).apply();
                    break;
                case "deep_orange":
                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.DEEP_ORANGE).apply();
                    break;
                default:
            }
            //重启界面
            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restart_intent);
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
