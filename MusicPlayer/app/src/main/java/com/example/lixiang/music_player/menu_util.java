package com.example.lixiang.music_player;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.lixiang.music_player.R.id.delete;

/**
 * Created by lixiang on 2017/3/21.
 */

public class menu_util {
    private static View rootview;

    public static void popupNetMenu (final Context context, View v,final int position) {
        final PopupMenu popup = new PopupMenu(context, v);
        popup.getMenuInflater().inflate(R.menu.net_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.openLink:
                        if (Data.getNetMusicList().get(position).getLink() !=null){
                        Uri web_uri = Uri.parse(Data.getNetMusicList().get(position).getLink());
                            Intent intent = new Intent(Intent.ACTION_VIEW, web_uri);
                            context.startActivity(intent);
                        }else {
                            Toast.makeText(context, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.getLink:
                        if (Data.getNetMusicList().get(position).getMusic() != null){
                        Uri download_uri = Uri.parse(Data.getNetMusicList().get(position).getMusic());
                            Intent web_intent = new Intent(Intent.ACTION_VIEW, download_uri);
                            context.startActivity(web_intent);
                        }else {
                            Toast.makeText(context, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }
public static  void popupMenu(final Activity context, View v,final int position) {
    rootview = v;
    final PopupMenu popup = new PopupMenu(context, v);
    popup.getMenuInflater().inflate(R.menu.list_popup_menu, popup.getMenu());
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.setAsNext:
                    setAsNext(context,position);
                    return true;
                case delete:
                    deleteFile(context,Data.getData(position));
                    return true;
                case R.id.setAsRingtone:
                    menu_util.setAsRingtone(context, position);
                    return true;
                case R.id.musicInfo:
                    showMusicInfo(context,position);
                    return true;
            }
            return true;
        }
    });
    popup.show(); //showing popup menu
}

    public static void setAsRingtone(final Activity context, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } else {
                File music = new File(Data.getData(position)); // path is a file to /sdcard/media/ringtone
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, music.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, Data.getTitle(position));
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                values.put(MediaStore.Audio.Media.ARTIST, Data.getArtist(position));
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                //Insert it into the database
                Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getAbsolutePath());
                context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + music.getAbsolutePath() + "\"", null);
                Uri newUri = context.getContentResolver().insert(uri, values);

                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
                //Snackbar
                Snackbar.make(rootview, "已成功设置为来电铃声", Snackbar.LENGTH_LONG).setAction("好的", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
            }
        } else {
            File music = new File(Data.getData(position)); // path is a file to /sdcard/media/ringtone
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, music.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, Data.getTitle(position));
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.ARTIST, Data.getArtist(position));
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            //Insert it into the database
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getAbsolutePath());
            context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + music.getAbsolutePath() + "\"", null);
            Uri newUri = context.getContentResolver().insert(uri, values);

            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            //Snackbar
            Snackbar.make(rootview, "已成功设置为来电铃声", Snackbar.LENGTH_LONG).setAction("好的", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
        }
    }
    public static void setAsNext(Activity context,int position){
        Data.setNextMusic(position);
        com.sothree.slidinguppanel.SlidingUpPanelLayout main_layout = (com.sothree.slidinguppanel.SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);
        Snackbar.make(rootview,"已成功设置为下一首播放",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
    }

    public  static void showMusicInfo(Activity context,int position){
        LayoutInflater inflater = context.getLayoutInflater();
        View musicinfo_dialog = inflater.inflate(R.layout.musicinfo_dialog,(ViewGroup) context.findViewById(R.id.musicInfo_dialog));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("歌曲信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setView(musicinfo_dialog);
        TextView title = (TextView) musicinfo_dialog.findViewById(R.id.dialog_title);
        Log.v("title","title"+title);
        TextView artist = (TextView) musicinfo_dialog.findViewById(R.id.dialog_artist);
        TextView album = (TextView) musicinfo_dialog.findViewById(R.id.dialog_album);
        TextView duration = (TextView) musicinfo_dialog.findViewById(R.id.dialog_duration);
        TextView playtimes = (TextView) musicinfo_dialog.findViewById(R.id.dialog_playtimes);
        TextView path = (TextView) musicinfo_dialog.findViewById(R.id.dialog_path);
        title.setText(Data.getTitle(position));
        artist.setText(Data.getArtist(position));
        album.setText(Data.getAlbum(position));
        int totalSecond = Data.getDuration(position)/1000;
        int minute = totalSecond/60;
        int second = totalSecond - minute*60;
        duration.setText(String.valueOf(minute)+"分"+String.valueOf(second)+"秒");
        playtimes.setText(String.valueOf(Data.findPlayTimesById(Data.getId(position))));
        path.setText(Data.getData(position));
        builder.show();
    }

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(final Activity context,String filePath) {
        final File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            //警告窗口
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("请注意").setMessage("将从设备中彻底删除该歌曲文件，你确定吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    file.delete();
                    if (file.exists()) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("无外置SD卡读写权限").setMessage("因Android对外置SD卡的读写权限限制，文件删除失败");
                        alert.setPositiveButton("我知道了",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                openSAF(context);
                            }
                        });
//                        openSAF.setNegativeButton("取消",new DialogInterface.OnClickListener(){
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
                        alert.show();
                    } else {
                        Snackbar.make(rootview,"文件删除成功",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
                        //更新mediastore
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        //重启界面
                        Intent intent = new Intent("permission_granted");
                        context.sendBroadcast(intent);
                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
            return true;
        }
        return false;
    }

    public static void openSAF(Activity context) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        context.startActivityForResult(intent, 42);
    }
}
