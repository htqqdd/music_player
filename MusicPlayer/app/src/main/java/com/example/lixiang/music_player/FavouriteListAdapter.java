package com.example.lixiang.music_player;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lixiang on 2017/3/26.
 */

public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.ViewHolder>{
    private Context mContext;
    private List<musicInfo> Timessublist;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        Timessublist = MyApplication.getTimessublist();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.favourite_musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //设置标题，歌手
        musicInfo musicNow = Timessublist.get(position);
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
                MyApplication.setMusicListNow(Timessublist,"Timessublist");
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
                menu_util.popupMenu((Activity) mContext, view, position,"Timessublist");
            }
        });
    }

    @Override
    public int getItemCount() {
        return Timessublist.size();
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
            cover = (ImageView) view.findViewById(R.id.Favourite_list_cover);
            button = (ImageView) view.findViewById(R.id.Favourite_list_button);
            album = (TextView) view.findViewById(R.id.Favourite_list_album);
            singer = (TextView) view.findViewById(R.id.Favourite_list_singer);
        }
    }

}
