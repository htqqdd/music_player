package com.lixiangsoft.lixiang.music_player;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;


/**
 * Created by lixiang on 2017/8/21.
 */

public class netListAdapter  extends RecyclerView.Adapter<netListAdapter.ViewHolder>{
    private Context mContext;
    private ProgressDialog dialog;
    private List<musicInfo> netMusicList;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        netMusicList = MyApplication.getNetMusiclist();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        musicInfo musicNow = netMusicList.get(position);
        //设置标题，歌手
        holder.title.setText(musicNow.getMusicTitle());
        holder.singer.setText(musicNow.getMusicArtist());
        //设置歌曲封面
        Glide
                .with(mContext)
                .load(musicNow.getMusicAlbum())
                .placeholder(R.drawable.default_album)
                .into(holder.cover);
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getLocal_net_mode() == false) {
                    //播放
                    dialog = ProgressDialog.show(mContext, "请稍后", "正在玩命加载中");
                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode,
                                             KeyEvent event) {
                            // TODO Auto-generated method stub
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                Intent intent = new Intent("service_broadcast");
                                intent.putExtra("ACTION", MyConstant.resetAction);
                                mContext.sendBroadcast(intent);
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });
                    MyApplication.setMusicListNow(netMusicList,"netMusicList");
                    MyApplication.setPositionNow(position);
                    Intent intent = new Intent("service_broadcast");
                    intent.putExtra("ACTION", MyConstant.playAction);
                    mContext.sendBroadcast(intent);
                }else {
                    Toast.makeText(mContext, "当前处于离线模式", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出菜单
                menu_util.popupNetMenu(mContext,view,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return MyApplication.getNetMusiclist().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        ImageView button;
        TextView title;
        TextView singer;
        RelativeLayout relativeLayout;

        public ViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view;
            cover = (ImageView) view.findViewById(R.id.list_cover);
            button = (ImageView) view.findViewById(R.id.list_button);
            title = (TextView) view.findViewById(R.id.list_song);
            singer = (TextView) view.findViewById(R.id.list_singer);
        }
    }
    public void dismissDialog(){
        if (dialog !=null) {
            dialog.dismiss();
        }
    }

}
