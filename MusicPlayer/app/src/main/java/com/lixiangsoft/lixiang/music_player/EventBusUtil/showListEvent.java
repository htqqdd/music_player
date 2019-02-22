package com.lixiangsoft.lixiang.music_player.EventBusUtil;

public class showListEvent {
    //0:Datelist
    //1:Timeslist
    //2:Datelist+TimesList
    //3:musicList
    //4:customList
    //5:customListDetail
    //6:customList+customListDetail
    public int arg1 =-1,arg2=-1,arg3 =-1;
    public showListEvent(int list1){
        arg1 = list1;
    }
    public showListEvent(int list1,int list2){
        arg1 = list1;
        arg2 = list2;
    }
    public showListEvent(int list1,int list2,int list3){
        arg1 = list1;
        arg2 = list2;
        arg3 = list3;
    }
}
