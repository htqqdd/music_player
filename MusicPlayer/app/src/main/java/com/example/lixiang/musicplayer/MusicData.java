package com.example.lixiang.musicplayer;

import android.app.Activity;
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

public class MusicData {
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
    public static int infoInitialized = 0;

    public static boolean is_recent = false;
    public static boolean is_favourite = false;
    public static int Recent_position = 0;
    public static int Favourite_position = 0;
    public static boolean serviceStarted = false;


//变量
    private static int mediaDuration = 0;
    private static int mediaCurrentPosition = 0;
    //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
    private static int playMode = 3;
    private static int position = 0;
    private static int nextMusic = -1;
    private static String  state = pausing;
    private static String[] media_music_info;
    private static Cursor cursor;
    private static int[] _ids;
    private static int[] _albumids;
    private static String[] _artists;// 保存艺术家
    private static String[] _titles;
    private static String[] _data;// 标题临时数组
    private static String[] _album;
    private static int[] _duration;
    private static Date[] _date;
    private static List<music_date> Datesublist;
    private static List<music_playtimes> Timessublist;
    private static ArrayList<music_playtimes> playtimesArrayList;
    private static SharedPreferences playtimes;
    public static int colorPrimarySetted;
    public static int colorAccentSetted = R.color.colorAccent;


    public static int get_mediaDuration(int position) {
        return _duration[position];
    }
    public static int get_mediaCurrentPosition() {
        return mediaCurrentPosition;
    }
    public static int getPlayMode(){
        return  playMode;
    }
    public static int getPosition(){ return position;}
    public static String getState(){return state;}
    public static int getNextMusic(){return nextMusic;}
    public static boolean IsRecent() {return is_recent;}
    public static boolean IsFavourite() {return is_favourite;}
    public static int getRecent_position(){return Recent_position;}
    public static int getFavourite_position(){return  Favourite_position;}
    public static int getInfoInitialized() {return infoInitialized;}
    public static boolean getServiceState() {return serviceStarted;}
    public static int getColorPrimarySetted() {return colorPrimarySetted;}
    public static int getColorAccentSetted() {return colorAccentSetted;}

    public static int getId (int position){
        return _ids[position];
    }
    public static int getAlbumId (int position){
        return _albumids[position];
    }
    public static String getArtist (int position){
        return _artists[position];
    }
    public static String getTitle (int position){
        return _titles[position];
    }
    public static String getData (int position){return _data[position];
    }
    public static String getAlbum (int position) {return  _album[position];}
    public static int getDuration (int position) {return  _duration[position];}
    public static Cursor getCursor (){
        return cursor;
    }
    public static List<music_date> getDateSublist() {return  Datesublist;}
    public static List<music_playtimes> getTimessublist() {return  Timessublist;}
    public static ArrayList<music_playtimes> getTimeslist() {return playtimesArrayList;}

    public static void set_mediaDuration(int media_duration) {
        MusicData.mediaDuration = media_duration;
    }
    public static void set_mediaCurrentPosition(int media_CurrentPosition) {
        MusicData.mediaCurrentPosition = media_CurrentPosition;
    }
    public static void setPlayMode(int playMode){
        MusicData.playMode = playMode;
    }
    public static void setPosition(int position){
        MusicData.position = position;
    }
    public static void setState(String state){
        MusicData.state = state;
    }
    public static void setNextMusic(int position){
        MusicData.nextMusic = position;}
    public static void setRecent(boolean b){
        MusicData.is_recent = b;}
    public static void setFavourite(boolean b){
        MusicData.is_favourite = b;}
    public static void setRecent_position(int position){
        MusicData.Recent_position = position;}
    public static void setFavourite_position(int position){
        MusicData.Favourite_position = position;}
    public static void setServiceStarted(boolean true_or_false){
        MusicData.serviceStarted = true_or_false;}
    public static void setColorPrimarySetted(int color){
        MusicData.colorPrimarySetted = color;}
    public static void setColorAccentSetted(int color){
        MusicData.colorAccentSetted = color;}

    public static void initialMusicInfo(Context context){
        Log.v("Context","Context是"+context);
        if (infoInitialized == 0) {

            //初始化音乐信息
            media_music_info = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};

            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                    null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            int total = cursor.getCount();
            cursor.moveToFirst();// 将游标移动到初始位置
            _ids = new int[total];// 返回int的一个列
            _artists = new String[total];// 返回String的一个列
            _titles = new String[total];// 返回String的一个列
            _data = new String[total];
            _albumids = new int[total];
            _album = new String[total];
            _duration = new int[total];
            for (int i = 0; i < total; i++) {
                _ids[i] = cursor.getInt(3);
                _titles[i] = cursor.getString(0);
                _artists[i] = cursor.getString(2);
                _data[i] = cursor.getString(4);
                _albumids[i] = cursor.getInt(5);
                _album[i] = cursor.getString(6);
                _duration[i] = cursor.getInt(1);
                cursor.moveToNext();// 将游标移到下一行
            }
        }
        infoInitialized = infoInitialized+1;
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
        _date = new Date[cursor.getCount()];

        for (int i = 0; i < cursor.getCount(); i++) {
            File music = new File(MusicData.getData(i));
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
        Datesublist = music_dates.subList(0,number);
        infoInitialized = infoInitialized+1;
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
        for (int j=0;j< cursor.getCount();j++){
            music_playtimes  Music_playtimes = new music_playtimes(_ids[j],playtimes.getInt(String.valueOf(_ids[j]),0));
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
        Timessublist = playtimesArrayList.subList(0,number);
    }
    public static void initialMusicPlaytimes(Activity context){
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
        for (int j=0;j< cursor.getCount();j++){
            music_playtimes  Music_playtimes = new music_playtimes(_ids[j],playtimes.getInt(String.valueOf(_ids[j]),0));
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
        Timessublist = playtimesArrayList.subList(0,number);
        infoInitialized = infoInitialized+1;
    }
    public static int findPlayTimesById(int id){
        int Playtimes = 0;
        Playtimes = playtimes.getInt(String.valueOf(id),0);
        return Playtimes;
    }
    public static int findPositionById(int id){
        int Position = 0;
        for (int i =0; i < cursor.getCount(); i++){
            if (_ids[i] == id) {
                Position = i;
                break;
            }
        }
        return Position;
    }
}
