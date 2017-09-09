package com.soft.lixiang.myapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.aesthetic.BottomNavBgMode;
import com.afollestad.aesthetic.BottomNavIconTextMode;
import com.afollestad.aesthetic.NavigationViewMode;

public class MainActivity extends AestheticActivity {
    private boolean isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Aesthetic.isFirstTime()) {
            Aesthetic.get()
                    .activityTheme(R.style.AppTheme)
                    .textColorPrimaryRes(R.color.text_color_primary)
                    .textColorSecondaryRes(R.color.text_color_secondary)
                    .colorPrimaryRes(R.color.md_white)
                    .colorAccentRes(R.color.md_blue)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .textColorPrimary(Color.BLACK)
                    .navigationViewMode(NavigationViewMode.SELECTED_ACCENT)
                    .bottomNavigationBackgroundMode(BottomNavBgMode.PRIMARY)
                    .bottomNavigationIconTextMode(BottomNavIconTextMode.SELECTED_ACCENT)
                    .apply();
        }

        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDark){
                    isDark = false;
                }else {
                    isDark = true;
                }
                Aesthetic.get().isDark(true).apply();
            }
        });

    }
}
