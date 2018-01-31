package com.example.lixiang.music_player;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;



/**
 * Created by lixiang on 2017/3/25.
 */

public class RecentListAdapter extends RecyclerView.Adapter<RecentListAdapter.ViewHolder> {
    private Context mContext;
    private List<musicInfo> Datesublist;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        Datesublist = MyApplication.getDatesublist();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.recent_musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        musicInfo musicNow = Datesublist.get(position);
        holder.album.setText(musicNow.getMusicAlbum());
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
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放
                MyApplication.setMusicListNow(Datesublist,"Datesublist");
                MyApplication.setPositionNow(position);
                Log.e("Adapter","位置"+position);
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
                menu_util.popupMenu((Activity) mContext, view, position,"Datesublist");
            }
        });

    }

    @Override
    public int getItemCount() {
        return Datesublist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        ImageView button;
        TextView album;
        TextView singer;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            cover = (ImageView) view.findViewById(R.id.Recent_list_cover);
            button = (ImageView) view.findViewById(R.id.Recent_list_button);
            album = (TextView) view.findViewById(R.id.Recent_list_album);
            singer = (TextView) view.findViewById(R.id.Recent_list_singer);
        }
    }

}
