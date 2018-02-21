package com.lixiangsoft.lixiang.music_player;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.objectbox.BoxStore;

/**
 * Created by lixiang on 2017/9/9.
 */

public class MyApplication extends Application {

    public static BoxStore boxStore;

    public static List<musicInfo> musicInfoArrayList;
    public static List<musicInfo> Datesublist;
    public static List<musicInfo> Timessublist;
    public static List<musicInfo> netMusiclist;
    public static List<musicInfo> MusicListNow;
    public static String Listlabel = "musicInfoArrayList";
    public static String customListNow;

    public static int playMode = MyConstant.list;
    public static String state = MyConstant.pausing;
    public static int positionNow = 0;
    public static int mediaDuration = 0;
    public static boolean local_net_mode = false;
    public static boolean serviceStarted = false;
    public static boolean hasInitialized = false;
    public static View.OnClickListener listener;

    @Override
    public void onCreate() {
        //初始化ObjectBox
        boxStore = MyObjectBox.builder().androidContext(this).build();
        //初始化Bug汇报
        initialBugly();
        super.onCreate();
    }

    //方法类
    public static void initialMusicInfo(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int filter_duration = 30000;
        String filtration = sharedPref.getString("filtration", "");
        switch (filtration) {
            case "thirty":
                filter_duration = 30000;
                break;
            case "forty_five":
                filter_duration = 45000;
                break;
            case "sixty":
                filter_duration = 60000;
                break;
            default:
        }
        int number = 18;
        String suggestion = sharedPref.getString("suggestion", "");
        switch (suggestion) {
            case "three":
                number = 3;
                break;
            case "six":
                number = 6;
                break;
            case "twelve":
                number = 12;
                break;
            case "eighteen":
                number = 18;
                break;
            default:
        }
        musicInfoArrayList = new ArrayList<musicInfo>();
        Timessublist = new ArrayList<musicInfo>();
        musicInfoArrayList = boxStore.boxFor(musicInfo.class).query().greater(musicInfo_.mDuration, filter_duration).notEqual(musicInfo_.hide, true).build().find();
        Timessublist = boxStore.boxFor(musicInfo.class).query().orderDesc(musicInfo_.mTimes).notEqual(musicInfo_.hide, true).build().find(0, number);
        if (musicInfoArrayList.size() == 0) {
            musicInfoArrayList = new ArrayList<musicInfo>();
            //初始化音乐信息
            String[] media_music_info = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                    null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            int total = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < total; i++) {
                if (cursor.getInt(1) > filter_duration) {
                    musicInfoArrayList.add(new musicInfo(cursor.getInt(3), cursor.getInt(5), cursor.getInt(1), cursor.getString(0), cursor.getString(2), cursor.getString(4), cursor.getString(6), 0, ""));
                }
                cursor.moveToNext();// 将游标移到下一行
            }
            boxStore.boxFor(musicInfo.class).put(musicInfoArrayList);
            Timessublist = new ArrayList<musicInfo>();
            Timessublist.addAll(musicInfoArrayList);
            if (number > Timessublist.size()) {
                number = Timessublist.size();
            }
            Timessublist = Timessublist.subList(0, number);
        }
        if (MusicListNow == null) {
            MusicListNow = musicInfoArrayList;
        }
//        if (musicInfoArrayList.size() != 0) {
            hasInitialized = true;
//        }
        Datesublist = new ArrayList<>();
        Datesublist.addAll(musicInfoArrayList);
        //从最近到之前排列
        Comparator<musicInfo> Datecomparator = new Comparator<musicInfo>() {
            @Override
            public int compare(musicInfo t1, musicInfo t2) {
                if (t1.getDate().before(t2.getDate())) {
                    return 1;
                } else if (t1.getDate().after(t2.getDate())) {
                    return -1;
                } else {
                    return 0;
                }
            }

        };
        Collections.sort(Datesublist, Datecomparator);
        //获取最近歌曲
        if (number > Datesublist.size()) {
            number = Datesublist.size();
        }
        Datesublist = Datesublist.subList(0, number);
    }

    public static void ReloadMusicInfo(Context context) {
        List<musicInfo> mediaStored = new ArrayList<musicInfo>();
        List<musicInfo> boxStored = boxStore.boxFor(musicInfo.class).getAll();
        //初始化音乐信息
        String[] media_music_info = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int total = cursor.getCount();
        cursor.moveToFirst();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int filter_duration = 30000;
        String filtration = sharedPref.getString("filtration", "");
        switch (filtration) {
            case "thirty":
                filter_duration = 30000;
                break;
            case "forty_five":
                filter_duration = 45000;
                break;
            case "sixty":
                filter_duration = 60000;
                break;
            default:
        }
        int found = 0;
        int remove = 0;
        String musicIDString = "";
        //从MediaStore添加歌曲
        if (total >= 1) {
            for (int i = 0; i < total; i++) {
                if (cursor.getInt(1) > filter_duration) {
                    musicInfo musicInfo = new musicInfo(cursor.getInt(3), cursor.getInt(5), cursor.getInt(1), cursor.getString(0), cursor.getString(2), cursor.getString(4), cursor.getString(6), 0, "");
                    mediaStored.add(musicInfo);
                    if (boxStore.boxFor(musicInfo.class).query().equal(musicInfo_.mId, cursor.getInt(3)).build().findFirst() == null) {
                        boxStore.boxFor(musicInfo.class).put(musicInfo);
                        found = found + 1;
                    }
                }
                cursor.moveToNext();// 将游标移到下一行
            }
        }
        //从BoxStore删除歌曲
        if (mediaStored.size() >= 1) {
            for (int i = 0; i < mediaStored.size(); i++) {
                musicIDString = musicIDString + String.valueOf(mediaStored.get(i).getMusicId()) + ".";
            }
        }
        if (boxStored.size() >= 1) {
            for (int i = 0; i < boxStored.size(); i++) {
                if (!musicIDString.contains(String.valueOf(boxStored.get(i).getMusicId()))) {
                    boxStore.boxFor(musicInfo.class).remove(boxStored.get(i));
                    remove = remove + 1;
                }
            }
        }
        Toast.makeText(context.getApplicationContext(), "新发现" + found + "首歌曲，删除" + remove + "首无效歌曲", Toast.LENGTH_SHORT).show();
        reDisplay(context.getApplicationContext());
    }

    public static void reDisplay(Context context) {
        //界面初始化
        MyApplication.initialMusicInfo(context);
        Intent intent = new Intent("permission_granted");
        context.sendBroadcast(intent);
        Intent intent2 = new Intent("list_permission_granted");
        context.sendBroadcast(intent2);
        Intent intent3 = new Intent("list_changed");
        context.sendBroadcast(intent3);
    }

    private void initialBugly() {
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

    //Getter And Setter
    public static BoxStore getBoxStore() {
        return boxStore;
    }

    public static List<musicInfo> getMusicListNow() {
        return MusicListNow;
    }

    public static String getListlabel() {
        return Listlabel;
    }

    public static void setMusicListNow(List<musicInfo> musicListNow, String label) {
        MusicListNow = musicListNow;
        Listlabel = label;
    }

    public static List<musicInfo> getMusicInfoArrayList() {
        return musicInfoArrayList;
    }

    public static List<musicInfo> getDatesublist() {
        return Datesublist;
    }

    public static List<musicInfo> getTimessublist() {
        return Timessublist;
    }

    public static List<musicInfo> getNetMusiclist() {
        return netMusiclist;
    }

    public static void setNetMusiclist(List<musicInfo> netMusiclist) {
        MyApplication.netMusiclist = netMusiclist;
    }

    public static String getCustomListNow() {
        return customListNow;
    }

    public static void setCustomListNow(String customListNow) {
        MyApplication.customListNow = customListNow;
    }

    public static int getPlayMode() {
        return playMode;
    }

    public static void setPlayMode(int playMode) {
        MyApplication.playMode = playMode;
    }

    public static String getState() {
        return state;
    }

    public static void setState(String state) {
        MyApplication.state = state;
    }

    public static int getPositionNow() {
        return positionNow;
    }

    public static void setPositionNow(int positionNow) {
        MyApplication.positionNow = positionNow;
    }

    public static int getMediaDuration() {
        return mediaDuration;
    }

    public static void setMediaDuration(int mediaDuration) {
        MyApplication.mediaDuration = mediaDuration;
    }

    public static boolean getLocal_net_mode() {
        return local_net_mode;
    }

    public static void setLocal_net_mode(boolean local_net_mode) {
        MyApplication.local_net_mode = local_net_mode;
    }

    public static boolean getServiceState() {
        return serviceStarted;
    }

    public static void setServiceStarted(boolean serviceStarted) {
        MyApplication.serviceStarted = serviceStarted;
    }

    public static boolean hasInitialized() {
        return hasInitialized;
    }

    public static View.OnClickListener getlyric_onClickListener() {
        return listener;
    }

    public static void setlyric_onClickListener(View.OnClickListener listener) {
        MyApplication.listener = listener;
    }
}
