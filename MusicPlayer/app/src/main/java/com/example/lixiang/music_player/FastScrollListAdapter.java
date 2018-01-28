package com.example.lixiang.music_player;

import android.app.Activity;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 2017/8/3.
 */

public class FastScrollListAdapter extends RecyclerView.Adapter<FastScrollListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Context mContext;
    private List<musicInfo> musicInfoArrayList;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        musicInfoArrayList = MyApplication.getMusicInfoArrayList();
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
        musicInfo musicNow = musicInfoArrayList.get(position);
        holder.song.setText(musicNow.getMusicTitle());
        holder.singer.setText(musicNow.getMusicArtist());
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, musicNow.getMusicAlbumId());
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
                menu_util.popupMenu((Activity) mContext, view, position,"musicInfoArrayList");
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

}
