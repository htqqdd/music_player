package com.example.lixiang.music_player;

import java.util.Date;

/**
 * Created by lixiang on 2017/3/23.
 */

public class music_date {
    public  int mPosition;
    public  Date mDate;
    public music_date(int position,Date date){
        mPosition = position;
        mDate = date;
    }
    public int getPosition(){
        return mPosition;
    }
    public Date getDate(){
        return mDate;
    }
}
