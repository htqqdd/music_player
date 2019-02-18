package com.lixiangsoft.lixiang.music_player;

import android.app.Activity;
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
import static com.lixiangsoft.lixiang.music_player.R.id.list_name;

/**
 * Created by lixiang on 2018/1/30.
 */

public class playlistdetailAdapter extends RecyclerView.Adapter<playlistdetailAdapter.ViewHolder> {
    private Activity mContext;
    private List<musicInfo> listDetail;
    private Playlist playlist;
    private View rootview;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = (Activity) parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.musiclist, parent,
                false));
        return holder;
    }

    @Override
    public int getItemCount() {
        return listDetail.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        musicInfo musicNow = listDetail.get(position);
        holder.song.setText(musicNow.getMusicTitle());
        holder.singer.setText(musicNow.getMusicArtist());
        //设置歌曲封面
        if (musicNow.getAlbumLink()!=null){
            Glide.with(mContext)
                    .load(musicNow.getAlbumLink())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.default_album)
                    .into(holder.cover);
        }else {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, musicNow.getMusicAlbumId());
            Glide
                    .with(mContext)
                    .load(uri)
                    .placeholder(R.drawable.default_album)
                    .into(holder.cover);
        }
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放
                MyApplication.setMusicListNow(listDetail, "listDetail");
                MyApplication.setPositionNow(position);
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", MyConstant.playAction);
                mContext.sendBroadcast(intent);
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出菜单
                rootview = view;
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.getMenuInflater().inflate(R.menu.inlist_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.setAsNext:
                                setAsNext(mContext, position);
                                return true;
                            case R.id.remove:
                                remove(mContext, position);
                                return true;
                            case R.id.hidefromlist:
                                hidefromlist(mContext, position);
                                return true;
                            case delete:
                                deleteFile(mContext, position);
                                return true;
                            case R.id.setAsRingtone:
                                setAsRingtone(mContext, position);
                                return true;
                            case R.id.musicInfo:
                                showMusicInfo(mContext, position);
                                return true;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
//                menu_util.popupMenu((Activity) mContext, view, position,"listDetail");
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (MyApplication.getCustomListNow() != null) {
            playlist = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique();
        }
        if (playlist != null) {
            listDetail = playlist.getMusicInfos();
        } else {
            listDetail = new ArrayList<>();
        }
        super.onAttachedToRecyclerView(recyclerView);
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
            cover = (ImageView) view.findViewById(R.id.list_cover);
            button = (ImageView) view.findViewById(R.id.list_button);
            song = (TextView) view.findViewById(R.id.list_song);
            singer = (TextView) view.findViewById(R.id.list_singer);
        }
    }

    private void remove(Context context, int position) {//从列表移除
        listDetail.remove(position);
        playlist.setMusicInfos(listDetail);
        MyApplication.getBoxStore().boxFor(Playlist.class).put(playlist);
        Toast.makeText(context, "成功移除1首歌曲", Toast.LENGTH_SHORT).show();
//        Snackbar.make(rootview, "成功移除1首歌曲", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        }).show();
        playlistdetailAdapter.this.notifyItemRemoved(position);
        playlistdetailAdapter.this.notifyItemRangeChanged(0,listDetail.size());
    }

    private void hidefromlist(Activity context, int position) {
//        List<musicInfo> list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();

        musicInfo musicInfo = listDetail.get(position);
        musicInfo.setHide(true);
        MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicInfo);
        listDetail.remove(position);
        playlist.setMusicInfos(listDetail);
        MyApplication.getBoxStore().boxFor(Playlist.class).put(playlist);
        Toast.makeText(context, "成功隐藏1首歌曲", Toast.LENGTH_SHORT).show();
//        Snackbar.make(rootview, "成功隐藏1首歌曲", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        }).show();
        //更新界面
        playlistdetailAdapter.this.notifyItemRemoved(position);
        playlistdetailAdapter.this.notifyItemRangeChanged(0,listDetail.size());
        //通知其他adapter
        MyApplication.initialMusicInfo(mContext);
        //更新Recommend界面（全部）
        Intent intent = new Intent("permission_granted");
        mContext.sendBroadcast(intent);
        //更新fastscroll界面
        Intent intent2 = new Intent("list_permission_granted");
        mContext.sendBroadcast(intent2);
    }

    private void setAsRingtone(final Activity context, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            List<musicInfo> list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();
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
            Toast.makeText(context, "已成功设置为来电铃声", Toast.LENGTH_SHORT).show();
            //Snackbar
//            Snackbar.make(rootview, "已成功设置为来电铃声", Snackbar.LENGTH_LONG).setAction("好的", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            }).show();
        }
    }

    private void setAsNext(Activity context, int position) {
        List<musicInfo> list = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos();;
        MyApplication.getMusicListNow().add(MyApplication.getPositionNow(), list.get(position));
        Toast.makeText(context, "已成功设置为下一首播放", Toast.LENGTH_SHORT).show();
//        com.sothree.slidinguppanel.SlidingUpPanelLayout main_layout = (com.sothree.slidinguppanel.SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);
//        Snackbar.make(rootview, "已成功设置为下一首播放", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        }).show();
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
//        final List<musicInfo> list = ;
        final musicInfo nowMusic = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos().get(position);
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
//                musicInfo nowMusic = list.get(position);
                nowMusic.setmTitle(title.getText().toString());
                nowMusic.setmArtist(artist.getText().toString());
                nowMusic.setmAlbum(album.getText().toString());
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(nowMusic);
                //更新界面
                listDetail.remove(position);
                listDetail.add(position,nowMusic);
                playlistdetailAdapter.this.notifyItemChanged(position);
                //通知其他adapter
                MyApplication.initialMusicInfo(mContext);
                //更新Recommend界面（全部）
                Intent intent = new Intent("permission_granted");
                mContext.sendBroadcast(intent);
                //更新fastscroll界面
                Intent intent2 = new Intent("list_permission_granted");
                mContext.sendBroadcast(intent2);
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
        final musicInfo musicinfo = MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, MyApplication.getCustomListNow()).build().findUnique().getMusicInfos().get(position);
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
                        Toast.makeText(context, "文件删除成功", Toast.LENGTH_SHORT).show();
//                        Snackbar.make(rootview, "文件删除成功", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                            }
//                        }).show();
                        //更新mediastore
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        //更新列表
                        playlistdetailAdapter.this.notifyItemRemoved(position);
                        playlistdetailAdapter.this.notifyItemRangeChanged(0,listDetail.size());
                        //通知其他adapter
                        MyApplication.initialMusicInfo(mContext);
                        //更新Recommend界面（全部）
                        Intent intent = new Intent("permission_granted");
                        mContext.sendBroadcast(intent);
                        //更新fastscroll界面
                        Intent intent2 = new Intent("list_permission_granted");
                        mContext.sendBroadcast(intent2);
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
