package com.lixiangsoft.lixiang.music_player;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.afollestad.aesthetic.AestheticActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class netMusicActivity extends AestheticActivity {
    private DismissReceiver dismissReceiver;
    private netListAdapter adapter;
    private RecyclerView net;
    private String input;
    private String filter;
    private String type;
    private int page = 1;
    private boolean Stillhasdata = true;
    private boolean notloading = MyApplication.getNetMusiclist().size()>=10;
    private ProgressDialog dialog;
    private Call call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle info = getIntent().getBundleExtra("info");
        if (info != null) {
            input = info.getString("input");
            filter = info.getString("filter");
            type = info.getString("type");
        }
        setContentView(R.layout.activity_net_music);

        Toolbar toolbar = (Toolbar) findViewById(R.id.net_toolbar);
        toolbar.setTitle("搜索结果");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //动态注册广播
        dismissReceiver = new DismissReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("dismiss_dialog");
        registerReceiver(dismissReceiver, intentFilter);


        TextView title = (TextView) findViewById(R.id.net_title);
        title.setText("以下音乐版权属于原资源提供方");
        net = (RecyclerView) findViewById(R.id.netRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        net.setLayoutManager(layoutManager);
        net.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                final int visibleItemCount = recyclerView.getChildCount();
                final int totalItemCount = layoutManager.getItemCount();
                final int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + 0) && Stillhasdata && notloading) {
                    new httpTask().execute();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        adapter = new netListAdapter();
        net.setAdapter(adapter);

    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(dismissReceiver);
        super.onDestroy();
    }


    private class DismissReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.dismissDialog();
            Snackbar.make(net,"在线播放仅作为预览，请在来源网站下载正版音乐后播放",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
        }
    }

    private class httpTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(netMusicActivity.this, "请稍后", "正在玩命加载更多歌曲");
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        //停止http
                        call.cancel();
                    }
                    return true;
                }
            });
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            notloading = false;
            page = page +1;
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder().add("input", input).add("filter", filter).add("type", type).add("page",String.valueOf(page)).build();
                Request request = new Request.Builder().url("https://music.2333.me").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36").addHeader("Referer","https://music.2333.me/?name="+java.net.URLEncoder.encode(input,"utf-8")+"&type="+type).post(requestBody).build();
                call = client.newCall(request);
                Response response = call.execute();
                String res = response.body().string();
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.getInt("code") == 200) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String data = jsonArray.toString();
                    Gson gson = new Gson();
                    List<Music> musicList = gson.fromJson(data, new TypeToken<List<Music>>() {
                    }.getType());
                    Stillhasdata = musicList.size()>=10;
                    List netMusicList = MyApplication.getNetMusiclist();
                    for (int i = 0; i < musicList.size(); i++) {
                        netMusicList.add(new musicInfo(0,0,0,musicList.get(i).getName(),musicList.get(i).getAuthor(),musicList.get(i).getMusic(),musicList.get(i).getPic(),0,musicList.get(i).getLink()));
                    }
                    MyApplication.setNetMusiclist(netMusicList);
                    return "200";
                }
            } catch (Exception e) {
                if (e instanceof java.net.UnknownHostException) {
                    return "404";
                }
                e.printStackTrace();
                return "unKnown";
            }
            return "unKnown";
        }

        @Override
        protected void onPostExecute(String s) {
            switch (s) {
                case "200":
                    notloading = true;
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                    break;
                default:
            }
            super.onPostExecute(s);
        }

    }
}
