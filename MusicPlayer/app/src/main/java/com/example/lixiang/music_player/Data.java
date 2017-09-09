package com.example.lixiang.music_player;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by lixiang on 2017/3/16.
 */

public class Data {
//程序常量
    public static String pausing = "PAUSING";
    public static String playing = "PLAYING";
    public static int resuming = 2;
    public static int initialize = -1;
    public static int previousAction = 11;
    public static int nextAction = 12;
    public static  int seektoAction = 13;
    public static  int pauseAction = 14;
    public static  int playAction = 15;
    public static  int mediaChangeAction=16;
    public static  int shuffleChangeAction=17;
    public static int deleteAction = 18;
    public static int sc_playAction = 19;
    public static int openDialog =20;

    public static boolean is_recent = false;
    public static boolean is_favourite = false;
    public static boolean is_net =false;
    public static int Recent_position = 0;
    public static int Favourite_position = 0;
    public static boolean serviceStarted = false;

    public static int getViewpager_item() {
        return viewpager_item;
    }

    public static void setViewpager_item(int viewpager_item) {
        Data.viewpager_item = viewpager_item;
    }

    public static int viewpager_item = 0;


    public static boolean isFromSc() {
        return isFromSc;
    }

    public static void setIsFromSc(boolean isFromSc) {
        Data.isFromSc = isFromSc;
    }

    //变量
    private static boolean isFromSc = false;
    private static boolean hasInitialized = false;
    private static ArrayList<musicInfo> musicInfoArrayList;

    public static int getMediaDuration() {
        return mediaDuration;
    }

    public static void setMediaDuration(int mediaDuration) {
        Data.mediaDuration = mediaDuration;
    }

    private static int mediaDuration = 0;
    private static int mediaCurrentPosition = 0;
    //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
    private static int playMode = 3;
    private static int position = 0;
    private static int nextMusic = -1;
    private static String  state = pausing;
    private static Cursor cursor;
    private static Date[] _date;
    private static List<music_date> Datesublist;
    private static List<music_playtimes> Timessublist;
    private static ArrayList<music_playtimes> playtimesArrayList;
    private static SharedPreferences playtimes;
    public static boolean firstOpen = true;

    public static List<DownloadFragment.Music> getNetMusicList() {
        return netMusicList;
    }

    public static void setNetMusicList(List<DownloadFragment.Music> netMusicList) {
        Data.netMusicList = netMusicList;
    }

    private static List<DownloadFragment.Music> netMusicList;


    public static boolean isHasInitialized() {return hasInitialized;}
    public static int get_mediaCurrentPosition() {
        return mediaCurrentPosition;
    }
    public static int getPlayMode(){
        return  playMode;
    }
    public static int getPosition(){ return position;}
    public static String getState(){return state;}
    public static int getNextMusic(){return nextMusic;}
    public static boolean IsNet() {return is_net;}
    public static boolean IsRecent() {return is_recent;}
    public static boolean IsFavourite() {return is_favourite;}
    public static int getRecent_position(){return Recent_position;}
    public static int getFavourite_position(){return  Favourite_position;}
    public static boolean getServiceState() {return serviceStarted;}
    public static int getId (int position){
        return musicInfoArrayList.get(position).getMusicId();
    }
    public static int getAlbumId (int position){
        return musicInfoArrayList.get(position).getMusicAlbumId();
    }
    public static String getArtist (int position){
        if (is_net) return netMusicList.get(position).getAuthor();
        return musicInfoArrayList.get(position).getMusicArtist();
    }
    public static String getLocalArtist(int position){
        return musicInfoArrayList.get(position).getMusicArtist();
    }
    public static String getTitle (int position){
        if (is_net) return netMusicList.get(position).getName();
        return musicInfoArrayList.get(position).getMusicTitle();
    }
    public static String getData (int position){
        if (is_net) return netMusicList.get(position).getMusic();
        return musicInfoArrayList.get(position).getMusicData();
    }
    public static String getAlbum (int position) {
//        if (is_net) return netMusicList.get(position).getAuthor();
        return  musicInfoArrayList.get(position).getMusicAlbum();
    }
    public static int getDuration (int position) {
        if (is_net) return 0;
        return  musicInfoArrayList.get(position).getMusicDuration();
    }
    public static int getTotalNumber () {
        if (is_net) return netMusicList.size();
        return musicInfoArrayList.size();
    }
    public static List<music_date> getDateSublist() {return  Datesublist;}
    public static List<music_playtimes> getTimessublist() {return  Timessublist;}

    public static void setPlayMode(int playMode){
        Data.playMode = playMode;
    }
    public static void setPosition(int position){
        Data.position = position;
    }
    public static void setState(String state){
        Data.state = state;
    }
    public static void setNextMusic(int position){Data.nextMusic = position;}
    public static void setNet(boolean b){Data.is_net = b;}
    public static void setRecent(boolean b){Data.is_recent = b;}
    public static void setFavourite(boolean b){Data.is_favourite = b;}
    public static void setRecent_position(int position){Data.Recent_position = position;}
    public static void setFavourite_position(int position){Data.Favourite_position = position;}
    public static void setServiceStarted(boolean true_or_false){Data.serviceStarted = true_or_false;}

    public static void initialMusicInfo(Service context){
        Log.v("Context123","Context是"+context);
        musicInfoArrayList = new ArrayList<musicInfo>();
        int filter_duration = 30000;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String filtration = sharedPref.getString("filtration", "");
        switch (filtration) {
            case "thirty":
                filter_duration = 30000;
                break;
            case "forty_five":
                filter_duration = 45000;
                break;
            case "sixty":
                filter_duration =60000;
                break;
            default:
        }
            //初始化音乐信息
        String[] media_music_info = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};

            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                    null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            int total = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < total; i++) {
                if (cursor.getInt(1) > filter_duration){
                    musicInfoArrayList.add(new musicInfo(cursor.getInt(3),cursor.getInt(5),cursor.getInt(1),cursor.getString(0),cursor.getString(2),cursor.getString(4),cursor.getString(6)));
                }
                cursor.moveToNext();// 将游标移到下一行
            }
        initialMusicDate(context);
        initialMusicPlaytimes(context);
        hasInitialized = true;
    }
    public static void initialMusicInfo(Context context){
        musicInfoArrayList = new ArrayList<musicInfo>();
        int filter_duration = 30000;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String filtration = sharedPref.getString("filtration", "");
        switch (filtration) {
            case "thirty":
                filter_duration = 30000;
                break;
            case "forty_five":
                filter_duration = 45000;
                break;
            case "sixty":
                filter_duration =60000;
                break;
            default:
        }
        Log.v("Context1234","Context是"+context);
            //初始化音乐信息
        String[] media_music_info = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};

            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                    null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            int total = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < total; i++) {
                if (cursor.getInt(1) >filter_duration){
                    musicInfoArrayList.add(new musicInfo(cursor.getInt(3),cursor.getInt(5),cursor.getInt(1),cursor.getString(0),cursor.getString(2),cursor.getString(4),cursor.getString(6)));
                }
                cursor.moveToNext();// 将游标移到下一行
            }
        initialMusicDate(context);
        initialMusicPlaytimes(context);
    }
    public  static void initialMusicDate(Context context){
        int number = 18;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String suggestion = sharedPref.getString("suggestion", "");
        switch (suggestion) {
            case "three":
                number = 3;
                break;
            case "six":
                number = 6;
                break;
            case "twelve":
                number =12;
                break;
            case "eighteen":
                number =18;
                break;
            default:
        }
        ArrayList<music_date> music_dates = new ArrayList<music_date>();
//        _date = new Date[cursor.getCount()];
        _date = new Date[musicInfoArrayList.size()];

        for (int i = 0; i < musicInfoArrayList.size(); i++) {
            File music = new File(musicInfoArrayList.get(i).getMusicData());
            Date date = new Date(music.lastModified());
            _date[i] = date;
            music_dates.add(new music_date(i,_date[i]));
        }

        //从最近到之前排列
        Comparator<music_date> Datecomparator = new Comparator<music_date>() {
            @Override
            public int compare(music_date t1, music_date t2) {
                if (t1.getDate().before(t2.getDate())) {
                    return 1;
                } else if (t1.getDate().after(t2.getDate())){
                    return  -1;
                }else {
                    return 0;
                }
            }

        };
        Collections.sort(music_dates,Datecomparator);
        //获取最近歌曲
        if (number >music_dates.size()){number = music_dates.size();}
        Datesublist = music_dates.subList(0,number);
    }
    public static void initialMusicPlaytimes(Service context){
        int number = 18;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String suggestion = sharedPref.getString("suggestion", "");
        switch (suggestion) {
            case "three":
                number = 3;
                break;
            case "six":
                number = 6;
                break;
            case "twelve":
                number =12;
                break;
            case "eighteen":
                number =18;
                break;
            default:
        }
        playtimes = context.getSharedPreferences("playtimes",Context.MODE_PRIVATE);
        playtimesArrayList = new ArrayList<music_playtimes>();
        for (int j=0;j< musicInfoArrayList.size();j++){
            music_playtimes  Music_playtimes = new music_playtimes(musicInfoArrayList.get(j).getMusicId(),playtimes.getInt(String.valueOf(musicInfoArrayList.get(j).getMusicId()),0));
            playtimesArrayList.add(Music_playtimes);
        }
        //从多到少排列
        Comparator<music_playtimes> Playtimescomparator = new Comparator<music_playtimes>() {
            @Override
            public int compare(music_playtimes t1, music_playtimes t2) {
                if (t1.getTimes() < (t2.getTimes())) {
                    return 1;
                } else if (t1.getTimes() > (t2.getTimes())){
                    return  -1;
                }else {
                    return 0;
                }
            }

        };
        Collections.sort(playtimesArrayList,Playtimescomparator);
        if (number > playtimesArrayList.size()){ number = playtimesArrayList.size();}
        Timessublist = playtimesArrayList.subList(0,number);
    }
    public static void initialMusicPlaytimes(Context context){
        int number = 18;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String suggestion = sharedPref.getString("suggestion", "");
        switch (suggestion) {
            case "three":
                number = 3;
                break;
            case "six":
                number = 6;
                break;
            case "twelve":
                number =12;
                break;
            case "eighteen":
                number =18;
                break;
            default:
        }
        playtimes = context.getSharedPreferences("playtimes",Context.MODE_PRIVATE);
        playtimesArrayList = new ArrayList<music_playtimes>();
        for (int j=0;j< musicInfoArrayList.size();j++){
            music_playtimes  Music_playtimes = new music_playtimes(musicInfoArrayList.get(j).getMusicId(),playtimes.getInt(String.valueOf(musicInfoArrayList.get(j).getMusicId()),0));
            playtimesArrayList.add(Music_playtimes);
        }
        //从多到少排列
        Comparator<music_playtimes> Playtimescomparator = new Comparator<music_playtimes>() {
            @Override
            public int compare(music_playtimes t1, music_playtimes t2) {
                if (t1.getTimes() < (t2.getTimes())) {
                    return 1;
                } else if (t1.getTimes() > (t2.getTimes())){
                    return  -1;
                }else {
                    return 0;
                }
            }

        };
        Collections.sort(playtimesArrayList,Playtimescomparator);
        if (number > playtimesArrayList.size()){ number = playtimesArrayList.size();}
        Timessublist = playtimesArrayList.subList(0,number);
    }


    public static int findPlayTimesById(int id){
        int Playtimes = 0;
        Playtimes = playtimes.getInt(String.valueOf(id),0);
        return Playtimes;
    }
    public static int findPositionById(int id){
        int Position = 0;
        for (int i =0; i < musicInfoArrayList.size(); i++){
            if (musicInfoArrayList.get(i).getMusicId() == id) {
                Position = i;
                break;
            }
        }
        return Position;
    }


}
