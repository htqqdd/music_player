package com.lixiangsoft.lixiang.music_player;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.lixiangsoft.lixiang.music_player.MyApplication.reDisplay;
import static com.lixiangsoft.lixiang.music_player.R.id.delete;
import static com.lixiangsoft.lixiang.music_player.R.id.list_button;

/**
 * Created by lixiang on 2017/8/3.
 */

public class FastScrollListAdapter extends RecyclerView.Adapter<FastScrollListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Activity mContext;
    private List<musicInfo> musicInfoArrayList;
    private View rootview;
    private int menu_position;
    private int match_selected_position;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mContext = (Activity) recyclerView.getContext();
        musicInfoArrayList = MyApplication.getMusicInfoArrayList();
        if (musicInfoArrayList == null) musicInfoArrayList = new ArrayList<>();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int Position = position;
        final musicInfo musicNow = musicInfoArrayList.get(position);
        holder.song.setText(musicNow.getMusicTitle());
        holder.singer.setText(musicNow.getMusicArtist());
        //设置歌曲封面
        if (musicNow.getAlbumLink()!=null){
            Glide.with(mContext)
                    .load(musicNow.getAlbumLink())
                    .placeholder(R.drawable.default_album)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
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
                MyApplication.setMusicListNow(musicInfoArrayList,"musicInfoArrayList");
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
                popup.getMenuInflater().inflate(R.menu.list_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.setAsNext:
                                setAsNext(mContext, position);
                                return true;
                            case R.id.searchForAlbum:
                                searchForAlbum(mContext, position);
                                return true;
                            case R.id.addTo:
                                addTo(mContext, position);
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
            }
        });

    }

    @Override
    public int getItemCount() {
        return musicInfoArrayList.size();
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

    @NonNull
    @Override
    public String getSectionName(int position) {
        String s = musicInfoArrayList.get(position).getMusicTitle();
        return s.subSequence(0, 1).toString();
    }

    private void hidefromlist(Activity context, int position) {
        musicInfo musicInfo = musicInfoArrayList.get(position);
        musicInfo.setHide(true);
        MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicInfo);
        musicInfoArrayList.remove(position);
        Snackbar.make(rootview, "成功隐藏1首歌曲", Snackbar.LENGTH_SHORT).setAction("好的", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
        //更新界面
        FastScrollListAdapter.this.notifyItemRemoved(position);
        notifyItemRangeChanged(0,musicInfoArrayList.size());
        //通知其他adapter
        MyApplication.initialMusicInfo(mContext);
        //更新Recommend界面（全部）
        Intent intent = new Intent("permission_granted");
        mContext.sendBroadcast(intent);
        //更新列表界面（整个）
        Intent intent2 = new Intent("list_changed");
        mContext.sendBroadcast(intent2);

    }

    private void searchForAlbum(final Activity context, final int position) {
        menu_position = position;
        LayoutInflater inflater = context.getLayoutInflater();
        final View pre_search_for_album = inflater.inflate(R.layout.pre_search_for_album, (ViewGroup) context.findViewById(R.id.pre_search_for_album));
        final EditText title = (EditText) pre_search_for_album.findViewById(R.id.search_for_album_title);
        final EditText singer = (EditText) pre_search_for_album.findViewById(R.id.search_for_album_singer);
        title.setText(musicInfoArrayList.get(position).getMusicTitle());
        singer.setText(musicInfoArrayList.get(position).getMusicArtist());
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请输入搜索内容");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (title.getText().toString().equals("") || singer.getText().toString().equals("")){
                    Toast.makeText(context, "歌曲名和歌手名均不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    new matchTask().execute(title.getText().toString()+singer.getText().toString());
                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setView(pre_search_for_album);
        builder.setCancelable(false);
        builder.show();
    }

    private void show_match_dialog(final List<String> musicList){
        LayoutInflater inflater = mContext.getLayoutInflater();
        final View search_for_album = inflater.inflate(R.layout.search_for_album, (ViewGroup) mContext.findViewById(R.id.search_for_album));
        final ImageView search_for_album_selected = search_for_album.findViewById(R.id.search_for_album_selected);
        final ImageView previous = search_for_album.findViewById(R.id.previous);
        ImageView next = search_for_album.findViewById(R.id.next);
        match_selected_position = 0;
        String url = musicList.get(match_selected_position);
        FutureTarget<File> target = Glide.with(mContext).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        Glide.with(mContext)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.default_album)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(search_for_album_selected);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (match_selected_position <= 0){
                    Toast.makeText(mContext, "没有更多了", Toast.LENGTH_SHORT).show();
                }else {
                    match_selected_position--;
                    String url = musicList.get(match_selected_position);
                    FutureTarget<File> target = Glide.with(mContext).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                    Glide.with(mContext)
                            .load(url)
                            .centerCrop()
                            .placeholder(R.drawable.default_album)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(search_for_album_selected);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (match_selected_position >= musicList.size()-1){
                    Toast.makeText(mContext, "没有更多了", Toast.LENGTH_SHORT).show();
                }else {
                    match_selected_position++;
                    String url = musicList.get(match_selected_position);
                    FutureTarget<File> target = Glide.with(mContext).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                    Glide.with(mContext)
                            .load(url)
                            .centerCrop()
                            .placeholder(R.drawable.default_album)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(search_for_album_selected);
                }
            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                musicInfo musicNow = musicInfoArrayList.get(menu_position);
                musicNow.setAlbumLink(musicList.get(match_selected_position));
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicNow);
                //更新界面
                FastScrollListAdapter.this.notifyItemChanged(menu_position);
                //通知其他adapter
                MyApplication.initialMusicInfo(mContext);
                //更新Recommend界面（全部）
                Intent intent = new Intent("permission_granted");
                mContext.sendBroadcast(intent);
                //更新列表界面（整个）
                Intent intent2 = new Intent("list_changed");
                mContext.sendBroadcast(intent2);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(search_for_album);
        builder.show();
    }

    private class matchTask extends AsyncTask<String, Integer, List<String>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(mContext, "正在搜索中...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder().add("music_input", strings[0]).add("music_filter", "name").add("music_type", "163").build();
                Request request = new Request.Builder().url("http://www.yove.net/yinyue/").addHeader("Origin", "http://www.yove.net").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Accept", "application/json, text/javascript, */*; q=0.01").post(requestBody).build();
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.getInt("code") == 200) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String data = jsonArray.toString();
                    Gson gson = new Gson();
                    List<Music> musicList = gson.fromJson(data, new TypeToken<List<Music>>() {
                    }.getType());
                    List<String> list = new ArrayList<>();
                    for (int i = 0;i<musicList.size();i++){
                        if (musicList.get(i).getMusicLargeAlbum() !=null){
                            list.add(musicList.get(i).getMusicLargeAlbum());
                        }
                    }
                    return list;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> list) {
            if (list == null) {
                Toast.makeText(mContext, "未匹配到封面", Toast.LENGTH_SHORT).show();
            } else {
                show_match_dialog(list);
            }
            super.onPostExecute(list);
        }
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
        MyApplication.getMusicListNow().add(MyApplication.getPositionNow(), musicInfoArrayList.get(position));
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
                //更新界面
                FastScrollListAdapter.this.notifyItemChanged(position);
                //通知其他adapter
                MyApplication.initialMusicInfo(mContext);
                //更新Recommend界面（全部）
                Intent intent = new Intent("permission_granted");
                mContext.sendBroadcast(intent);
                //更新列表界面（整个）
                Intent intent2 = new Intent("list_changed");
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
                        //更新列表
                        FastScrollListAdapter.this.notifyItemRemoved(position);
                        notifyItemRangeChanged(0,musicInfoArrayList.size());
                        //通知其他adapter
                        MyApplication.initialMusicInfo(mContext);
                        //更新Recommend界面（全部）
                        Intent intent = new Intent("permission_granted");
                        mContext.sendBroadcast(intent);
                        //更新列表界面（整个）
                        Intent intent2 = new Intent("list_changed");
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
