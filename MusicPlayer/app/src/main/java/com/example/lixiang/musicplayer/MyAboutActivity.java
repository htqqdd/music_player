package com.example.lixiang.musicplayer;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;

import org.polaric.colorful.Colorful;

/**
 * Created by lixiang on 2017/7/28.
 */

public abstract class MyAboutActivity extends MaterialAboutActivity {
    private String themeString;

    public MyAboutActivity() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.themeString = Colorful.getThemeString();
        this.setTheme(Colorful.getThemeDelegate().getStyle());
        if(Build.VERSION.SDK_INT >= 21) {
            if(Colorful.getThemeDelegate().isTranslucent()) {
                this.getWindow().addFlags(67108864);
            }

            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription((String)null, (Bitmap)null, this.getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getColorRes()));
            this.setTaskDescription(tDesc);
        }

    }

    protected void onResume() {
        super.onResume();
        if(!Colorful.getThemeString().equals(this.themeString)) {
            Log.d("Colorful", "Theme change detected, restarting activity");
            this.recreate();
        }

    }
}
