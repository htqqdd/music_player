package com.example.lixiang.music_player;

import java.io.File;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;
import io.objectbox.relation.ToOne;

/**
 * Created by lixiang on 2017/8/16.
 */
@Entity
public class musicInfo {

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    @Id
    private long storeId;
    @Index
    public int mId = 0;
    public int mAlbumId = 0;
    public int mDuration = 0;
    public String mTitle = "";
    public String mArtist = "";
    public String mData = "";
    public String mAlbum = "";
    public int mTimes = 0;
    public String mLink = "";
    @Index
    public Boolean hide = false;
    public musicInfo(){}
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

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public void setmAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
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

    public void setTimes(int time){
        mTimes = time;
    }
}
