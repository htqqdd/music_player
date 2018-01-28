package com.example.lixiang.music_player;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.aesthetic.AestheticActivity;


public class searchActivity extends AestheticActivity implements SearchView.OnQueryTextListener{
    private SearchView searchView;
    private searchAdapter mSearchAdapter;
    private DismissReceiver dismissReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //动态注册广播
        dismissReceiver = new DismissReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("dismiss_dialog");
        registerReceiver(dismissReceiver, intentFilter);

        Toolbar search_toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        search_toolbar.inflateMenu(R.menu.search_menu);
        search_toolbar.setTitle("搜索");
        setSupportActionBar(search_toolbar);//设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView search_recycler_view = (RecyclerView) findViewById(R.id.search_RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        search_recycler_view.setLayoutManager(layoutManager);
        search_recycler_view.setItemAnimator(new DefaultItemAnimator());
        mSearchAdapter = new searchAdapter();
        search_recycler_view.setAdapter(mSearchAdapter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(dismissReceiver);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem search = menu.findItem(R.id.search_menu);
        searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private class DismissReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSearchAdapter.dismissDialog();
//            Snackbar.make(net,"在线播放仅作为预览，请下载正版音乐后播放",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
        }
    }
}
