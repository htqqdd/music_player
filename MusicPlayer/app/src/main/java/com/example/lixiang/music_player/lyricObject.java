package com.example.lixiang.music_player;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by lixiang on 2017/9/24.
 */

public class lyricObject{
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public lyricObject(String s){
        String[] all=s.split("]");
        if (all.length ==1){
            text = "";
        }else {
            text = all[1];
        }
        all[0] = all[0].replace(":", ".");
        all[0] = all[0].replace(".", "@");
        String timeStr[] = all[0].substring(1).split("@");
        if (timeStr[0].substring(0,1).equals("0")) {
            time = (Integer.valueOf(timeStr[0]) * 60 + Integer.valueOf(timeStr[1])) * 1000 + Integer.valueOf(timeStr[2]);
        }else {
            time = 0;
        }
    }

    public lyricObject(String t,String s){
if (t.equals("")){
    time  = 0;
    text = "";
}else {
    t = t.replace(":", ".");
    t=t.replace(".", "@");
    String timeStr[] = t.split("@");
    if (timeStr[0].substring(0,1).equals("0")) {
        time = (Integer.valueOf(timeStr[0]) * 60 + Integer.valueOf(timeStr[1])) * 1000 + Integer.valueOf(timeStr[2]);
    }else {
        time = 0;
    }
    text = s;
}
    }

    private long time;
    private String text;
}

