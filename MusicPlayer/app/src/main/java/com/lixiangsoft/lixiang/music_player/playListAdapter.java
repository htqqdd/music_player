package com.lixiangsoft.lixiang.music_player;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lixiangsoft.lixiang.music_player.EventBusUtil.showListEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 2018/1/29.
 */

public class playListAdapter extends RecyclerView.Adapter<playListAdapter.ViewHolder> {
    private Activity mContext;
    private List<Playlist> list;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        list = MyApplication.getBoxStore().boxFor(Playlist.class).getAll();
        if (list == null) list = new ArrayList<>();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.name.setText(list.get(position).getName());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.getMenuInflater().inflate(R.menu.playlist_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.rename_list:
                                LayoutInflater inflater = mContext.getLayoutInflater();
                                final View createlist_dialog = inflater.inflate(R.layout.createlist, (ViewGroup) mContext.findViewById(R.id.create_list_dialog));
                                final EditText name = (EditText) createlist_dialog.findViewById(R.id.list_name);
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("重命名");
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String text = name.getText().toString();
                                        if (!text.equals("")) {
                                            if (MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, text).build().findUnique() == null) {
                                                Playlist listnow = list.get(position);
                                                listnow.setName(text);
                                                MyApplication.getBoxStore().boxFor(Playlist.class).put(listnow);
                                                MyApplication.setCustomListNow(text);
                                                Toast.makeText(mContext, "成功修改1个播放列表", Toast.LENGTH_SHORT).show();
                                                //更新列表界面
//                                                Intent intent = new Intent("list_changed");
//                                                mContext.sendBroadcast(intent);
                                                EventBus.getDefault().post(new showListEvent(6));
                                            } else {
                                                Toast.makeText(mContext, "该列表已存在", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(mContext, "列表名不能为空", Toast.LENGTH_SHORT).show();
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
                                return true;
                            case R.id.delete_list:
                                MyApplication.getBoxStore().boxFor(Playlist.class).remove(list.get(position));
                                //更新界面
//                                Intent intent = new Intent("list_changed");
//                                mContext.sendBroadcast(intent);
                                EventBus.getDefault().post(new showListEvent(6));
                                return true;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.setCustomListNow(list.get(position).getName());
                //更新列表界面(仅detaillist)
//                Intent intent = new Intent("list_changed");
//                intent.putExtra("Action", 1);
//                mContext.sendBroadcast(intent);
                EventBus.getDefault().post(new showListEvent(5));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = (Activity) parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.playlist, parent,
                false));
        return holder;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView button;
        TextView name;
        View layout;

        public ViewHolder(View view) {
            super(view);
            layout = view;
            button = (ImageView) view.findViewById(R.id.button);
            name = (TextView) view.findViewById(R.id.list_name);
        }
    }
}
