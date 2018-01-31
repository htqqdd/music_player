package com.lixiangsoft.lixiang.music_player;

import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Created by lixiang on 2018/1/29.
 */
@Entity
public class Playlist {
    @Id
    private long Id;

    private List<musicInfo> musicInfos;

    public List<musicInfo> getMusicInfos() {
        return musicInfos;
    }

    public void setMusicInfos(List<musicInfo> musicInfos) {
        this.musicInfos = musicInfos;
    }

    public void add(musicInfo musicInfo){
        musicInfos.add(musicInfo);
    }

    public void remove(musicInfo musicInfo){
        musicInfos.remove(musicInfo);
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Index
    private String name;
    public  Playlist(String name){
        this.name = name;
    }

    public  Playlist(){
    }
}
