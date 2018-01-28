package com.example.lixiang.music_player;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by lixiang on 2017/9/9.
 */

public class MyApplication extends Application {

    public static List<musicInfo> musicInfoArrayList;
    public static List<musicInfo> Datesublist;
    public static List<musicInfo> Timessublist;
    public static List<musicInfo> netMusiclist;
    public static List<musicInfo> MusicListNow = musicInfoArrayList;
    public static String Listlabel = "musicInfoArrayList";
    public static SharedPreferences playtimes;

    public static int playMode = MyConstant.list;
    public static String state = MyConstant.pausing;
    public static int positionNow = 0;
    public static int mediaDuration = 0;
    public static boolean local_net_mode = false;
    public static boolean serviceStarted = false;
    public static boolean hasInitialized = false;

    private static Cursor cursor;
    @Override
    public void onCreate() {
        //初始化Bug汇报
        initialBugly();
        //读取设置
            //离线/在线设置
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean local_net_mode = sharedPref.getBoolean("local_net_mode", false);
        super.onCreate();
    }

    //Getter And Setter
    public static List<musicInfo> getMusicListNow(){
        return MusicListNow;
    }
    public static String getListlabel(){
        return Listlabel;
    }
    public static void setMusicListNow(List<musicInfo> musicListNow,String label) {
        MusicListNow = musicListNow;
        Listlabel = label;
    }
    public static List<musicInfo> getMusicInfoArrayList(){
        return musicInfoArrayList;
    }
    public static List<musicInfo> getDatesublist() { return Datesublist; }
    public static List<musicInfo> getTimessublist() { return Timessublist; }
    public static List<musicInfo> getNetMusiclist() {return netMusiclist;
    }

    public static void setNetMusiclist(List<musicInfo> netMusiclist) {
        MyApplication.netMusiclist = netMusiclist;
    }

    public static int getPlayMode() { return playMode; }
    public static void setPlayMode(int playMode) { MyApplication.playMode = playMode; }
    public static String getState() {return state;}
    public static void setState(String state) {MyApplication.state = state;}
    public static int getPositionNow() { return positionNow; }
    public static void setPositionNow(int positionNow) { MyApplication.positionNow = positionNow; }
    public static int getMediaDuration() {return mediaDuration;}
    public static void setMediaDuration(int mediaDuration) {MyApplication.mediaDuration = mediaDuration;}
    public static boolean getLocal_net_mode() {return local_net_mode;}
    public static void setLocal_net_mode(boolean local_net_mode) {MyApplication.local_net_mode = local_net_mode;}
    public static boolean getServiceState() { return serviceStarted; }
    public static void setServiceStarted(boolean serviceStarted) {MyApplication.serviceStarted = serviceStarted;}
    public static boolean isHasInitialized (){ return hasInitialized;}

    //方法类
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
        //初始化音乐信息
        playtimes = context.getSharedPreferences("playtimes",Context.MODE_PRIVATE);

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
                musicInfoArrayList.add(new musicInfo(cursor.getInt(3),cursor.getInt(5),cursor.getInt(1),cursor.getString(0),cursor.getString(2),cursor.getString(4),cursor.getString(6),playtimes.getInt(String.valueOf(cursor.getInt(3)),0),""));
            }
            cursor.moveToNext();// 将游标移到下一行
        }
        hasInitialized = true;
        Datesublist = new ArrayList<musicInfo>();
        Timessublist = new ArrayList<musicInfo>();
        for (int j = 0; j < musicInfoArrayList.size(); j++) {
            musicInfo musicInfo = musicInfoArrayList.get(j);
            Datesublist.add(musicInfo);
            Timessublist.add(musicInfo);
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
        //从最近到之前排列
        Comparator<musicInfo> Datecomparator = new Comparator<musicInfo>() {
            @Override
            public int compare(musicInfo t1, musicInfo t2) {
                if (t1.getDate().before(t2.getDate())) {
                    return 1;
                } else if (t1.getDate().after(t2.getDate())){
                    return  -1;
                }else {
                    return 0;
                }
            }

        };
        Collections.sort(Datesublist,Datecomparator);
        //获取最近歌曲
        if (number >Datesublist.size()){number = Datesublist.size();}
        Datesublist = Datesublist.subList(0,number);
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
        //从多到少排列
        Comparator<musicInfo> Playtimescomparator = new Comparator<musicInfo>() {
            @Override
            public int compare(musicInfo t1, musicInfo t2) {
                if (t1.getTimes() < (t2.getTimes())) {
                    return 1;
                } else if (t1.getTimes() > (t2.getTimes())){
                    return  -1;
                }else {
                    return 0;
                }
            }

        };
        Collections.sort(Timessublist,Playtimescomparator);
        if (number > Timessublist.size()){ number = Timessublist.size();}
        Timessublist = Timessublist.subList(0,number);
    }
    public static int findPlayTimesById(int id){
        int Playtimes = 0;
        Playtimes = playtimes.getInt(String.valueOf(id),0);
        return Playtimes;
    }

    private void initialBugly(){
        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        Bugly.init(context, "1d65abe1b1", false);
        Beta.initDelay = 1000;
    }
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
