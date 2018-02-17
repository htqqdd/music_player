package com.lixiangsoft.lixiang.music_player;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.lixiangsoft.lixiang.music_player.R.id.delete;

/**
 * Created by lixiang on 2017/8/15.
 */

public class searchAdapter extends RecyclerView.Adapter<searchAdapter.ViewHolder>{
    private List<list_filter_info> listFiltered;
    private Activity mContext;
    private ProgressDialog dialog;
    private View rootview;
    private List<musicInfo> musicInfoArrayList;


    @Override
    public searchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = (Activity) parent.getContext();
        rootview = parent;
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.search, parent,
                false));
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        musicInfoArrayList = MyApplication.getMusicInfoArrayList();
        listFiltered = new ArrayList<>();
        if (musicInfoArrayList !=null) {
            for (int i = 0; i < musicInfoArrayList.size(); i++) {
                musicInfo musicInfo = musicInfoArrayList.get(i);
                listFiltered.add(new list_filter_info(i, musicInfo.getMusicAlbumId(), musicInfo.getMusicTitle(), musicInfo.getMusicArtist(), musicInfo.getMusicAlbum(),musicInfo.getAlbumLink()));
            }
        }else {
            musicInfoArrayList = new ArrayList<>();
        }
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final list_filter_info info = listFiltered.get(position);
        holder.song.setText(info.getTitle());
        holder.singer.setText(info.getArtist());
        //设置歌曲封面
        if (info.getAlbumLink()!=null){
            Glide.with(mContext)
                    .load(info.getAlbumLink())
                    .placeholder(R.drawable.default_album)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.cover);
        }else {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, info.getmAlbumId());
            Glide.with(mContext).load(uri).placeholder(R.drawable.default_album).into(holder.cover);
        }
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放
                MyApplication.setPositionNow(info.getPosition());
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", MyConstant.playAction);
                mContext.sendBroadcast(intent);
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                menu_util.popupMenu((Activity) mContext, view, info.getPosition(), "musicInfoArrayList");
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.getMenuInflater().inflate(R.menu.list_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.setAsNext:
                                setAsNext(mContext, info.getPosition());
                                return true;
                            case R.id.searchForAlbum:
                                Toast.makeText(mContext, "请在“歌曲”页面中执行该操作", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.addTo:
                                addTo(mContext, info.getPosition());
                                return true;
                            case R.id.hidefromlist:
                                hidefromlist(mContext, info.getPosition());
                                return true;
                            case delete:
                                deleteFile(mContext, info.getPosition());
                                return true;
                            case R.id.setAsRingtone:
                                setAsRingtone(mContext, info.getPosition());
                                return true;
                            case R.id.musicInfo:
                                showMusicInfo(mContext, info.getPosition());
                                return true;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFiltered.size();
    }

    public void setFilter(String text) {
        listFiltered = new ArrayList<>();
        List<musicInfo> musicInfoList = MyApplication.getMusicInfoArrayList();
        for (int i = 0; i < musicInfoList.size(); i++) {
            musicInfo musicInfo = musicInfoList.get(i);
            if (musicInfo.getMusicArtist().toLowerCase().contains(text)|| musicInfo.getMusicTitle().toLowerCase().contains(text) || musicInfo.getMusicAlbum().toLowerCase().contains(text)){
                listFiltered.add(new list_filter_info(i,musicInfo.getMusicAlbumId(),musicInfo.getMusicTitle(),musicInfo.getMusicArtist(),musicInfo.getMusicAlbum(),musicInfo.getAlbumLink()));
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        ImageView button;
        TextView song;
        TextView singer;
        RelativeLayout relativeLayout;

        public ViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view;
            cover = (ImageView) view.findViewById(R.id.search_cover);
            button = (ImageView) view.findViewById(R.id.searchView_button);
            song = (TextView) view.findViewById(R.id.search_song);
            singer = (TextView) view.findViewById(R.id.search_singer);
        }
    }



    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            Snackbar.make(rootview, "在线播放仅作为预览，请在来源网站下载正版音乐后播放", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            }).show();
        }
    }

    public class list_filter_info {
        public int mPosition;
        public String mTitle;
        public String mArtist;
        public int mAlbumId;
        public String mAlbum;
        public String mAlbumLink;
        public list_filter_info(int position,int albumId,String title,String artist,String album,String AlbumLink){
            mTitle = title;
            mPosition = position;
            mArtist = artist;
            mAlbumId = albumId;
            mAlbum = album;
            mAlbumLink = AlbumLink;
        }

        public int getPosition() {
            return mPosition;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getArtist() {
            return mArtist;
        }

        public int getmAlbumId() {
            return mAlbumId;
        }

        public String getmAlbum() {
            return mAlbum;
        }

        public String getAlbumLink() {
            return mAlbumLink;
        }
    }

    private void hidefromlist(Activity context, int position) {
        musicInfo musicInfo = musicInfoArrayList.get(position);
        musicInfo.setHide(true);
        MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicInfo);
        musicInfoArrayList.remove(position);
searchAdapter.this.notifyDataSetChanged();
        Snackbar.make(rootview, "成功隐藏1首歌曲", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
        MyApplication.initialMusicInfo(context);
        Intent intent = new Intent("permission_granted");
        context.sendBroadcast(intent);
        Intent intent2 = new Intent("list_permission_granted");
        context.sendBroadcast(intent2);
        Intent intent3 = new Intent("list_changed");
        context.sendBroadcast(intent3);
    }

    private void addTo(final Activity context, final int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View addtolist_dialog = inflater.inflate(R.layout.addtolist, (ViewGroup) context.findViewById(R.id.addtolist_dialog));
        final AlertDialog dialog = showAddtolistDialog(addtolist_dialog, context, position);
        View createlist = addtolist_dialog.findViewById(R.id.create_list);
        createlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showCreateListDialog(context, view);
            }
        });
    }

    private AlertDialog showAddtolistDialog(View v, final Activity context, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到播放列表");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(v);
        final AlertDialog dialog = builder.show();
//        final List<musicInfo> list = MyApplication.getMusicInfoArrayList();
        ListView listView = (ListView) v.findViewById(R.id.play_list_view);
        final List<Playlist> Playlists = MyApplication.getBoxStore().boxFor(Playlist.class).getAll();
        String[] data = new String[Playlists.size()];
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
                listSelected.add(musicInfoArrayList.get(position));
                MyApplication.getBoxStore().boxFor(Playlist.class).put(listSelected);
                dialog.dismiss();
                Snackbar.make(rootview, "成功加入1首歌曲到播放列表", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
                //更新界面
                Intent intent = new Intent("list_changed");
                context.sendBroadcast(intent);
            }
        });
        return dialog;

    }

    private void showCreateListDialog(final Activity context, View view) {
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
                        Snackbar.make(rootview, "成功新建1个播放列表", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();
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

    private void setAsRingtone(final Activity context, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
//            List<musicInfo> list = MyApplication.getMusicInfoArrayList();
            File music = new File(musicInfoArrayList.get(position).getMusicData()); // path is a file to /sdcard/media/ringtone
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, music.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, musicInfoArrayList.get(position).getMusicTitle());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.ARTIST, musicInfoArrayList.get(position).getMusicArtist());
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

    private void setAsNext(Activity context, int position) {
//        List<musicInfo> list = MyApplication.getMusicInfoArrayList();
        MyApplication.getMusicListNow().add(MyApplication.getPositionNow(), musicInfoArrayList.get(position));
        com.sothree.slidinguppanel.SlidingUpPanelLayout main_layout = (com.sothree.slidinguppanel.SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);
        Snackbar.make(rootview, "已成功设置为下一首播放", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        }).show();
    }

    private void showMusicInfo(final Activity context, final int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View musicinfo_dialog = inflater.inflate(R.layout.musicinfo_dialog, (ViewGroup) context.findViewById(R.id.musicInfo_dialog));
        final EditText title = (EditText) musicinfo_dialog.findViewById(R.id.dialog_title);
        final EditText artist = (EditText) musicinfo_dialog.findViewById(R.id.dialog_artist);
        final EditText album = (EditText) musicinfo_dialog.findViewById(R.id.dialog_album);
        TextView duration = (TextView) musicinfo_dialog.findViewById(R.id.dialog_duration);
        TextView playtimes = (TextView) musicinfo_dialog.findViewById(R.id.dialog_playtimes);
        TextView path = (TextView) musicinfo_dialog.findViewById(R.id.dialog_path);
        final musicInfo nowMusic = musicInfoArrayList.get(position);
        title.setText(nowMusic.getMusicTitle());
        artist.setText(nowMusic.getMusicArtist());
        album.setText(nowMusic.getMusicAlbum());
        int totalSecond = nowMusic.getMusicDuration() / 1000;
        int minute = totalSecond / 60;
        int second = totalSecond - minute * 60;
        duration.setText(String.valueOf(minute) + "分" + String.valueOf(second) + "秒");
        playtimes.setText(String.valueOf(nowMusic.getTimes()));
        path.setText(nowMusic.getMusicData());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("歌曲信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nowMusic.setmTitle(title.getText().toString());
                nowMusic.setmArtist(artist.getText().toString());
                nowMusic.setmAlbum(album.getText().toString());
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(nowMusic);
                MyApplication.initialMusicInfo(context);
                Intent intent = new Intent("permission_granted");
                context.sendBroadcast(intent);
                Intent intent2 = new Intent("list_permission_granted");
                context.sendBroadcast(intent2);
                Intent intent3 = new Intent("list_changed");
                context.sendBroadcast(intent3);
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

    private boolean deleteFile(final Activity context, final int position) {
        final musicInfo musicinfo = musicInfoArrayList.get(position);
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
                        musicInfoArrayList.remove(position);
                        Snackbar.make(rootview, "文件删除成功", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
                        //更新mediastore
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        MyApplication.initialMusicInfo(context);
                        Intent intent = new Intent("permission_granted");
                        context.sendBroadcast(intent);
                        Intent intent2 = new Intent("list_permission_granted");
                        context.sendBroadcast(intent2);
                        Intent intent3 = new Intent("list_changed");
                        context.sendBroadcast(intent3);
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
}
