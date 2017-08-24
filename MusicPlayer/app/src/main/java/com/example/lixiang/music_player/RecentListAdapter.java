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

import static com.example.lixiang.music_player.Data.playAction;


/**
 * Created by lixiang on 2017/3/25.
 */

public class RecentListAdapter extends RecyclerView.Adapter<RecentListAdapter.ViewHolder> {
    private Context mContext;

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
        final int Position = position;
        holder.album.setText(Data.getAlbum(Data.getDateSublist().get(position).getPosition()));
        holder.singer.setText(Data.getLocalArtist(Data.getDateSublist().get(position).getPosition()));
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(Data.getDateSublist().get(position).getPosition()));
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
                Data.setPlayMode(3);
                Data.setRecent(true);
                Data.setFavourite(false);
                Data.setNet(false);
                Data.setPosition(Data.getDateSublist().get(Position).getPosition());
                Data.setRecent_position(Position);//获取Recent列表位置
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
                menu_util.popupMenu((Activity) mContext, view, Data.getDateSublist().get(Position).getPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return Data.getDateSublist().size();
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
