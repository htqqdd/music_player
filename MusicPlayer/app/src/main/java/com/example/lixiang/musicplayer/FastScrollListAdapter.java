package com.example.lixiang.musicplayer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.MusicData.initialMusicInfo;

/**
 * Created by lixiang on 2017/8/3.
 */

public class FastScrollListAdapter extends RecyclerView.Adapter<FastScrollListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Context mContext;
    private String[] media_music_info;
    private Cursor cursor;
    private ArrayList<musicInfo> musicInfoArrayList;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        initialMusicInfo(mContext);
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final int Position = position;
        holder.song.setText(musicInfoArrayList.get(position).getMusicTitle());
        holder.singer.setText(musicInfoArrayList.get(position).getMusicArtist());
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, musicInfoArrayList.get(position).getMusicAlbumId());
        Glide
                .with(mContext)
                .load(uri)
                .placeholder(R.drawable.default_album)
                .into(holder.cover);
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放
                Data.setPlayMode(3);
                Data.setRecent(false);
                Data.setFavourite(false);
                Data.setPosition(position);
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", playAction);
                mContext.sendBroadcast(intent);
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出菜单
                menu_util.popupMenu((Activity) mContext, view, position);
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

    private void initialMusicInfo(Context context) {
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
        media_music_info = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};

        cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int total = cursor.getCount();
        cursor.moveToFirst();// 将游标移动到初始位置
        for (int i = 0; i < total; i++) {
            if (cursor.getInt(1) > filter_duration) {
                musicInfoArrayList.add(new musicInfo(cursor.getInt(3), cursor.getInt(5), cursor.getInt(1), cursor.getString(0), cursor.getString(2), cursor.getString(4), cursor.getString(6)));
            }
            cursor.moveToNext();// 将游标移到下一行
        }
    }
}
