package com.example.lixiang.music_player;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import io.objectbox.Box;

import static com.tencent.bugly.beta.tinker.TinkerManager.getApplication;

/**
 * Created by lixiang on 2017/10/4.
 */

public class DataUtil {
//    public static void loadDataBase(Context context){
//        Box<MusicData> musicDataBox = ((MyApplication) getApplication()).getBoxStore().boxFor(MusicData.class);
//        String[] media_music_info = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};
//        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//        cursor.moveToFirst();
//        for (int i = 0; i < cursor.getCount(); i++) {
//            MusicData musicData = new MusicData(cursor.getInt(3),cursor.getString(0),cursor.getString(2),cursor.getString(4),cursor.getString(6),cursor.getInt(5),cursor.getLong(1));
//            musicDataBox.put(musicData);
//            cursor.moveToNext();// 将游标移到下一行
//        }
//    }
}
