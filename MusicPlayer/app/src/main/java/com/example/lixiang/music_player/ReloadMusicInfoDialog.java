package com.example.lixiang.music_player;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by lixiang on 2018/1/29.
 */

public class ReloadMusicInfoDialog extends DialogPreference {
    public ReloadMusicInfoDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult)
            MyApplication.ReloadMusicInfo(getContext());
        super.onDialogClosed(positiveResult);
    }

    @Override
    public void setDialogTitle(CharSequence dialogTitle) {
        dialogTitle = "本地歌曲全盘扫描";
        super.setDialogTitle(dialogTitle);
    }
}
