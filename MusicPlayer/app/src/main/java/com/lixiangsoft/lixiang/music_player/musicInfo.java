package com.lixiangsoft.lixiang.music_player;

import java.io.File;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

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
    public String mTitle;
    public String mArtist;
    public String mData;
    public String mAlbum ;
    public int mTimes = 0;
    public String mLink;
    public String AlbumLink;
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

    public String getMusicLargeAlbum() {
        //网易、QQ、虾米、百度、咪咕、蜻蜓支持
        if (mAlbum!=null) {
            if (mAlbum.contains("?param=100x100")) {//?param=100x100网易
                return mAlbum.substring(0, mAlbum.length() - 14) + "?param=800x800";
            } else if (mAlbum.contains("T002R300x300")) {//QQ音乐pic=http://y.gtimg.cn/music/photo_new/T002R300x300M000003Djf8120GknA.jpg
                return mAlbum.replace("T002R300x300", "T002R800x800");
            } else if (mAlbum.contains("/100/")) {//酷狗音乐pic=http://singerimg.kugou.com/uploadpic/pass/softhead/100/20160908/20160908121727482.jpg
                return mAlbum.replace("/100/", "/200/");
            } else if (mAlbum.contains("w_150,h_150")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/ec271e041469a6af9ef8cff93658be98/67308/67308.jpg@s_1,w_150,h_150
                return mAlbum.replace("w_150,h_150", "w_800,h_800");
            } else if (mAlbum.contains("@s_0,w_240")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/7dce01e6a400a986580b20f848c580a9/271877645/271877645.jpg@s_0,w_240
                return mAlbum.replace("@s_0,w_240", "@s_0,w_800");
            } else if (mAlbum.contains("RsT_200x200")) {//咪咕音乐pic=http://img01.12530.com/music/picture/20170518/27/16/sqMP3645.jpg_RsT_200x200.jpg
                return mAlbum.replace("_RsT_200x200", "_RsT_800x800");
            } else if (mAlbum.contains("!200")) {//蜻蜓音乐pic=http://pic.qingting.fm/2015/1225/2015122512113624.jpg!200
                return mAlbum.replace("!200", "!800");
            }
        }
        return mAlbum;
    }
    public String getMusicMediumAlbum() {
        if (mAlbum!=null) {
            if (mAlbum.contains("?param=100x100")) {//?param=100x100网易
                return mAlbum.substring(0, mAlbum.length() - 14) + "?param=300x300";
            } else if (mAlbum.contains("300x300")) {//QQ音乐pic=http://y.gtimg.cn/music/photo_new/T002R300x300M000003Djf8120GknA.jpg
                return mAlbum;
            } else if (mAlbum.contains("/100/")) {//酷狗音乐pic=http://singerimg.kugou.com/uploadpic/pass/softhead/100/20160908/20160908121727482.jpg
                return mAlbum.replace("/100/", "/200/");
            } else if (mAlbum.contains("w_150,h_150")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/ec271e041469a6af9ef8cff93658be98/67308/67308.jpg@s_1,w_150,h_150
                return mAlbum.replace("w_150,h_150", "w_300,h_300");
            } else if (mAlbum.contains("@s_0,w_240")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/7dce01e6a400a986580b20f848c580a9/271877645/271877645.jpg@s_0,w_240
                return mAlbum.replace("@s_0,w_240", "@s_0,w_300");
            } else if (mAlbum.contains("RsT_200x200")) {//咪咕音乐pic=http://img01.12530.com/music/picture/20170518/27/16/sqMP3645.jpg_RsT_200x200.jpg
                return mAlbum.replace("RsT_200x200", "RsT_300x300");
            } else if (mAlbum.contains("!200")) {//蜻蜓音乐pic=http://pic.qingting.fm/2015/1225/2015122512113624.jpg!200
                return mAlbum.replace("!200", "!300");
            }
        }
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

    public String getAlbumLink() {
        return AlbumLink;
    }

    public void setAlbumLink(String albumLink) {
        AlbumLink = albumLink;
    }
}
