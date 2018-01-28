package com.example.lixiang.music_player;

import java.io.File;
import java.util.Date;

/**
 * Created by lixiang on 2017/8/16.
 */

public class musicInfo {
    public int mId = 0;
    public int mAlbumId = 0;
    public int mDuration = 0;
    public String mTitle = "";
    public String mArtist = "";
    public String mData = "";
    public String mAlbum = "";
    public int mTimes = 0;
    public String mLink = "";

    public musicInfo(int id, int albumId, int duration, String title, String artist, String data, String album, int playTimes,String link) {
        mId = id;
        mAlbumId = albumId;
        mDuration = duration;
        mTitle = title;
        mArtist = artist;
        mData = data;
        mAlbum = album;
        mTimes = playTimes;
        mLink = link;
    }

    public int getMusicId() {
        return mId;
    }

    public int getMusicAlbumId() {
        return mAlbumId;
    }

    public int getMusicDuration() {
        return mDuration;
    }

    public String getMusicTitle() {
        return mTitle;
    }

    public String getMusicArtist() {
        if (mArtist.equals("<unknown>")) {
            return "未知歌手";

        }else {
            return mArtist;
        }
    }

    public String getMusicData() {
        return mData;
    }

    public String getMusicAlbum() {
        return mAlbum;
    }

    public String getMusicLink() {
        return mLink;
    }

    public Date getDate() {
        File music = new File(mData);
        Date date = new Date(music.lastModified());
        return date;
    }

    public int getTimes() {
        return mTimes;
    }
}
