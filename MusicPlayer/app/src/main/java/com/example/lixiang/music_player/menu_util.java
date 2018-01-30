package com.example.lixiang.music_player;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;


import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.lixiang.music_player.MyApplication.reDisplay;
import static com.example.lixiang.music_player.R.id.buttonPanel;
import static com.example.lixiang.music_player.R.id.delete;

/**
 * Created by lixiang on 2017/3/21.
 */

public class menu_util {
    private static View rootview;

    public static void popupNetMenu(final Context context, View v, final int position) {
        final PopupMenu popup = new PopupMenu(context, v);
        popup.getMenuInflater().inflate(R.menu.net_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.openLink:
                        if (MyApplication.getNetMusiclist().get(position).getMusicLink() != null) {
                            Uri web_uri = Uri.parse(MyApplication.getNetMusiclist().get(position).getMusicLink());
                            Intent intent = new Intent(Intent.ACTION_VIEW, web_uri);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.getLink:
                        if (MyApplication.getNetMusiclist().get(position).getMusicData() != null) {
                            Uri download_uri = Uri.parse(MyApplication.getNetMusiclist().get(position).getMusicData());
                            Intent web_intent = new Intent(Intent.ACTION_VIEW, download_uri);
                            context.startActivity(web_intent);
                        } else {
                            Toast.makeText(context, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    public static void popupMenu(final Activity context, View v, final int position, final String fromWhichList) {
        rootview = v;
        final PopupMenu popup = new PopupMenu(context, v);
        if (fromWhichList.equals("listDetail")){
            popup.getMenuInflater().inflate(R.menu.inlist_menu, popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.list_popup_menu, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.setAsNext:
                        setAsNext(context, position, fromWhichList);
                        return true;
                    case R.id.addTo:
                        addTo(context, position, fromWhichList);
                        return true;
                    case R.id.remove:
                        remove(context,position);
                        return true;
                    case R.id.hidefromlist:
                        hidefromlist(context, position, fromWhichList);
                        return true;
                    case delete:
                        deleteFile(context, position, fromWhichList);
                        return true;
                    case R.id.setAsRingtone:
                        menu_util.setAsRingtone(context, position, fromWhichList);
                        return true;
                    case R.id.musicInfo:
                        showMusicInfo(context, position, fromWhichList);
                        return true;
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    public static void remove(Context context,int position) {
        Playlist playlist = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique();
        List<musicInfo> list = playlist.getMusicInfos();
        playlist.remove(list.get(position));
        MyApplication.getBoxStore().boxFor(Playlist.class).put(playlist);
        Intent intent = new Intent("list_changed");
        intent.putExtra("Action",1);
        context.sendBroadcast(intent);

    }

    public static void hidefromlist(Activity context, int position, String fromWhich) {
        List<musicInfo> list;
        if (fromWhich.equals("Timessublist")) {
            list = MyApplication.getTimessublist();
        } else if (fromWhich.equals("Datesublist")) {
            list = MyApplication.getDatesublist();
        } else if (fromWhich.equals("listDetail")) {
            list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();
        } else {
            list = MyApplication.getMusicInfoArrayList();
        }
        musicInfo musicInfo = list.get(position);
        musicInfo.setHide(true);
        MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicInfo);
        Toast.makeText(context, "成功隐藏1首歌曲", Toast.LENGTH_SHORT).show();
        reDisplay(context);
    }

    public static void addTo(final Activity context, final int position, String fromWhich) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View addtolist_dialog = inflater.inflate(R.layout.addtolist, (ViewGroup) context.findViewById(R.id.addtolist_dialog));
        final AlertDialog dialog = showAddtolistDialog(addtolist_dialog, context, position, fromWhich);
        View createlist = addtolist_dialog.findViewById(R.id.create_list);
        createlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showCreateListDialog(context, view);
            }
        });

    }

    public static AlertDialog showAddtolistDialog(View v, final Activity context, final int position, String fromWhich) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到播放列表");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(v);
        final AlertDialog dialog = builder.show();
        final List<musicInfo> list;
        if (fromWhich == "Timessublist") {
            list = MyApplication.getTimessublist();
        } else if (fromWhich == "Datesublist") {
            list = MyApplication.getDatesublist();
        } else {
            list = MyApplication.getMusicInfoArrayList();
        }
        ListView listView = (ListView) v.findViewById(R.id.play_list_view);
        final List<Playlist> Playlists = MyApplication.getBoxStore().boxFor(Playlist.class).getAll();
        String[] data;
        data = new String[Playlists.size()];
        for (int i = 0; i < Playlists.size(); i++) {
            data[i] = Playlists.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Playlist listSelected = Playlists.get(i);
                listSelected.add(list.get(position));
                MyApplication.getBoxStore().boxFor(Playlist.class).put(listSelected);
                dialog.dismiss();
                Toast.makeText(context, "成功加入1首歌曲到播放列表", Toast.LENGTH_SHORT).show();
                //更新界面
                Intent intent = new Intent("list_changed");
                context.sendBroadcast(intent);
            }
        });
        return dialog;

    }

    public static void showCreateListDialog(final Activity context, View view) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View createlist_dialog = inflater.inflate(R.layout.createlist, (ViewGroup) context.findViewById(R.id.create_list_dialog));
        final EditText name = (EditText) createlist_dialog.findViewById(R.id.list_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("新建歌曲列表");
        builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = name.getText().toString();
                if (!text.equals("")) {
                    if (MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, text).build().findUnique() == null) {
                        if (MyApplication.getBoxStore().boxFor(Playlist.class).getAll().size() == 0) {
                            MyApplication.setCustomListNow(text);
                        }
                        MyApplication.getBoxStore().boxFor(Playlist.class).put(new Playlist(name.getText().toString()));
                        Toast.makeText(context, "成功新建1个播放列表", Toast.LENGTH_SHORT).show();
                        //更新列表界面
                        Intent intent = new Intent("list_changed");
                        context.sendBroadcast(intent);
                    } else {
                        Toast.makeText(context, "该列表已存在", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "列表名不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(createlist_dialog);
        builder.show();
    }

    public static void setAsRingtone(final Activity context, int position, String fromWhich) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            List<musicInfo> list;
            if (fromWhich.equals("Timessublist")) {
                list = MyApplication.getTimessublist();
            } else if (fromWhich.equals("Datesublist")) {
                list = MyApplication.getDatesublist();
            } else if (fromWhich.equals("listDetail")) {
                list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();
            } else {
                list = MyApplication.getMusicInfoArrayList();
            }
            File music = new File(list.get(position).getMusicData()); // path is a file to /sdcard/media/ringtone
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, music.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, list.get(position).getMusicTitle());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.ARTIST, list.get(position).getMusicArtist());
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

    public static void setAsNext(Activity context, int position, String fromWhich) {
        List<musicInfo> list;
        if (fromWhich.equals("Timessublist")) {
            list = MyApplication.getTimessublist();
        } else if (fromWhich.equals("Datesublist")) {
            list = MyApplication.getDatesublist();
        } else if (fromWhich.equals("listDetail")) {
            list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();
        } else {
            list = MyApplication.getMusicInfoArrayList();
        }
        MyApplication.getMusicListNow().add(MyApplication.getPositionNow(), list.get(position));
        com.sothree.slidinguppanel.SlidingUpPanelLayout main_layout = (com.sothree.slidinguppanel.SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);
        Snackbar.make(rootview, "已成功设置为下一首播放", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        }).show();
    }

    public static void showMusicInfo(final Activity context, final int position, String fromWhich) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View musicinfo_dialog = inflater.inflate(R.layout.musicinfo_dialog, (ViewGroup) context.findViewById(R.id.musicInfo_dialog));
        final EditText title = (EditText) musicinfo_dialog.findViewById(R.id.dialog_title);
        final EditText artist = (EditText) musicinfo_dialog.findViewById(R.id.dialog_artist);
        final EditText album = (EditText) musicinfo_dialog.findViewById(R.id.dialog_album);
        TextView duration = (TextView) musicinfo_dialog.findViewById(R.id.dialog_duration);
        TextView playtimes = (TextView) musicinfo_dialog.findViewById(R.id.dialog_playtimes);
        TextView path = (TextView) musicinfo_dialog.findViewById(R.id.dialog_path);
        final List<musicInfo> list;
        if (fromWhich.equals("Timessublist")) {
            list = MyApplication.getTimessublist();
        } else if (fromWhich.equals("Datesublist")) {
            list = MyApplication.getDatesublist();
        } else if (fromWhich.equals("listDetail")) {
            list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();
        } else {
            list = MyApplication.getMusicInfoArrayList();
        }
        title.setText(list.get(position).getMusicTitle());
        artist.setText(list.get(position).getMusicArtist());
        album.setText(list.get(position).getMusicAlbum());
        int totalSecond = list.get(position).getMusicDuration() / 1000;
        int minute = totalSecond / 60;
        int second = totalSecond - minute * 60;
        duration.setText(String.valueOf(minute) + "分" + String.valueOf(second) + "秒");
        playtimes.setText(String.valueOf(list.get(position).getTimes()));
        path.setText(list.get(position).getMusicData());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("歌曲信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                musicInfo nowMusic = list.get(position);
                nowMusic.setmTitle(title.getText().toString());
                nowMusic.setmArtist(artist.getText().toString());
                nowMusic.setmAlbum(album.getText().toString());
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(nowMusic);
                reDisplay(context);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(musicinfo_dialog);
        builder.show();
    }

    public static boolean deleteFile(final Activity context, int position, String fromWhich) {
        final musicInfo musicinfo;
        if (fromWhich.equals("Timessublist")) {
            musicinfo = MyApplication.getTimessublist().get(position);
        } else if (fromWhich.equals("Datesublist")) {
            musicinfo = MyApplication.getDatesublist().get(position);
        } else if (fromWhich.equals("listDetail")) {
            musicinfo = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos().get(position);
        } else {
            musicinfo = MyApplication.getMusicInfoArrayList().get(position);
        }
        final File file = new File(musicinfo.getMusicData());
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
                        alert.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
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
                        MyApplication.getBoxStore().boxFor(musicInfo.class).remove(musicinfo);
                        Snackbar.make(rootview, "文件删除成功", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
                        //更新mediastore
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        //重启界面
                        reDisplay(context);
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