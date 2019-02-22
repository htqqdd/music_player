package com.lixiangsoft.lixiang.music_player.EventBusUtil;

public class mediaChangeEvent {
    public String title;
    public String author;
    public int duration;
    public int color;
    public mediaChangeEvent(String title,String author,int duration,int color){
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.color = color;
    }

}
