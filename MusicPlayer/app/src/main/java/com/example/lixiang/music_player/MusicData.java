package com.example.lixiang.music_player;

import android.provider.MediaStore;

import java.io.File;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;


/**
 * Created by lixiang on 2017/10/4.
 */
@Entity
public class MusicData {
@Id
    public long id;
    public String song;
    public String singer;
    public String path;
    public String album;
    @Index
    public int local_id;
    public int net_id;
    public int album_id;
    public long duration;
    public Date date;
    public int playtimes;
    public boolean isLocal;

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public int getNet_id() {
        return net_id;
    }

    public void setNet_id(int net_id) {
        this.net_id = net_id;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPlaytimes() {
        return playtimes;
    }

    public void setPlaytimes(int playtimes) {
        this.playtimes = playtimes;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getWeb_link() {
        return web_link;
    }

    public void setWeb_link(String web_link) {
        this.web_link = web_link;
    }

    public String getDownload_link() {
        return download_link;
    }

    public void setDownload_link(String download_link) {
        this.download_link = download_link;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String web_link;
    public String download_link;
    public String pic;
    public MusicData(int local_id,String song,String singer,String path,String album,int album_id,long duration) {
        this.local_id = local_id;
        this.song=song;
        this.singer= singer;
        this.path= path;
        this.album = album;
        this.album_id=album_id;
        this.duration = duration;
        this.date= new Date(new File(path).lastModified());
        this.isLocal = true;
    }
}
