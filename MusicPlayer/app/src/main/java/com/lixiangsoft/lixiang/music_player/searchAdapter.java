package com.lixiangsoft.lixiang.music_player;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 2017/8/15.
 */

public class searchAdapter extends RecyclerView.Adapter<searchAdapter.ViewHolder>{
    private List<list_filter_info> listFiltered;
    private Context mContext;
    private ProgressDialog dialog;
    private View rootView;


    @Override
    public searchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        rootView = parent;
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.search, parent,
                false));
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        List<musicInfo> musicInfoList = MyApplication.getMusicInfoArrayList();
        listFiltered = new ArrayList<>();
        for (int i = 0; i < musicInfoList.size(); i++) {
            listFiltered.add(new list_filter_info(i, musicInfoList.get(i).getMusicAlbumId(), musicInfoList.get(i).getMusicTitle(), musicInfoList.get(i).getMusicArtist()));
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final list_filter_info info = listFiltered.get(position);
        holder.song.setText(info.getTitle());
        holder.singer.setText(info.getArtist());
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, info.getmAlbumId());
        Glide.with(mContext).load(uri).placeholder(R.drawable.default_album).into(holder.cover);
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
                menu_util.popupMenu((Activity) mContext, view, info.getPosition(), "musicInfoArrayList");
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
            if (musicInfoList.get(i).getMusicArtist().toLowerCase().contains(text)|| musicInfoList.get(i).getMusicTitle().toLowerCase().contains(text)){
                listFiltered.add(new list_filter_info(i,musicInfoList.get(i).getMusicAlbumId(),musicInfoList.get(i).getMusicTitle(),musicInfoList.get(i).getMusicArtist()));
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

//    class MyFilter extends Filter {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            filtered = new ArrayList<music_title>();
//            if (constraint != null && constraint.toString().trim().length() > 0) {
//                List<musicInfo> listNow = MyApplication.getMusicInfoArrayList();
//                for (int i = 0; i < previous.size(); i++) {
//                    musicInfo musicNow = listNow.get(i);
//                    String title = musicNow.getMusicTitle();
//                    String singer = musicNow.getMusicArtist();
//                    if (title.contains(constraint) || singer.contains(constraint)) {
//                        filtered.add(new music_title(i, title));
//                    }
//                }
//
//            } else {
//                filtered = previous;
//            }
//            FilterResults filterResults = new FilterResults();
//            filterResults.count = filtered.size();
//            filterResults.values = filtered;
//            return filterResults;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            filtered = (ArrayList<music_title>) results.values;
//            notifyDataSetChanged();
//        }
//    }

//    public class music_title {
//        public int mPosition;
//        public String mTitle;
//
//        public music_title(int position, String title) {
//            mPosition = position;
//            mTitle = title;
//        }
//
//        public int getPosition() {
//            return mPosition;
//        }
//
//        public String getTitle() {
//            return mTitle;
//        }
//    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            Snackbar.make(rootView, "在线播放仅作为预览，请在来源网站下载正版音乐后播放", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
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
        public list_filter_info(int position,int albumId,String title,String artist){
            mTitle = title;
            mPosition = position;
            mArtist = artist;
            mAlbumId = albumId;
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
    }
}
