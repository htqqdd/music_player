package com.example.lixiang.music_player;

/**
 * Created by lixiang on 2017/3/25.
 */

public class music_playtimes {
    public  int mId;
    public  int mTimes;
    public music_playtimes(int id,int times){
        mId = id;
        mTimes = times;
    }
    public int getId(){
        return mId;
    }
    public int getTimes(){
        return mTimes;
    }
}
