package com.lixiangsoft.lixiang.music_player.EventBusUtil;

public class ServiceEvent {
    public int action;
    public int delay = 0;
    public ServiceEvent(int action){
        this.action = action;
    }
    public ServiceEvent(int action,int delay){
        this.action = action;
        this.delay = delay;
    }
}
