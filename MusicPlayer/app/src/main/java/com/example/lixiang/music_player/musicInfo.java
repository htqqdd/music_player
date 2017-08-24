package com.example.lixiang.music_player;

/**
 * Created by lixiang on 2017/8/16.
 */

public class musicInfo {
        public int mId;
        public int mAlbumId;
        public int mDuration;
        public String mTitle;
        public String mArtist;
        public String mData;
        public String mAlbum;
        public musicInfo(int id,int albumId,int duration,String title,String artist,String data,String album){
            mId = id;
            mAlbumId = albumId;
            mDuration = duration;
            mTitle = title;
            mArtist = artist;
            mData = data;
            mAlbum = album;
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
            return mArtist;
        }

        public String getMusicData() {
            return mData;
        }

        public String getMusicAlbum() {
            return mAlbum;
        }
}
