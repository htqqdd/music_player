package com.example.lixiang.musicplayer;

import android.app.Activity;
import android.app.Application;

import org.polaric.colorful.Colorful;

/**
 * Created by lixiang on 2017/7/19.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Colorful.defaults()
                .primaryColor(Colorful.ThemeColor.TEAL)
                .accentColor(Colorful.ThemeColor.PINK)
                .translucent(false)
                .dark(false);
        Colorful.init(this);
    }
}
