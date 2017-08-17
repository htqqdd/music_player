package com.example.lixiang.musicplayer;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import static com.example.lixiang.musicplayer.R.id.fastScrollRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFragment extends Fragment {


    public DownloadFragment() {
        // Required empty public constructor
    }

    private View rootView;
    private WebView webView;
    private SwipeRefreshLayout refresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_download, container, false);

        refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        webView = (WebView) rootView.findViewById(R.id.webView);
        //设置 缓存模式
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
        //开启JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://music.2333.me/");
//        webView.setWebChromeClient(new WebChromeClient(){
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//
//                super.onProgressChanged(view, newProgress);
//            }
//        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onLoadResource(WebView view, String url) {
                clean(view);
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                refresh.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                refresh.setRefreshing(false);
                clean(view);
                super.onPageFinished(view, url);
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });

        new getColorTask().execute();

        return rootView;
    }



    @Override
    public void onDestroy() {
        if (webView !=null){
            webView.loadDataWithBaseURL(null,"","text/html","utf-8",null);
            webView.clearHistory();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void clean(WebView view){
        //网页头部
        view.loadUrl("JavaScript:function setTop(){document.querySelector('body > header').style.display=\"none\";}setTop();");
        //介绍头部
        view.loadUrl("JavaScript:function setTop(){document.querySelector('body > section > div.am-container.am-margin-vertical-xl > header').style.display=\"none\";}setTop();");
        //头部空隙
        view.loadUrl("JavaScript:function setTop(){document.querySelector('body > section > div.am-container.am-margin-vertical-xl > hr').style.display=\"none\";}setTop();");
        //下方介绍
        view.loadUrl("JavaScript:function setTop(){document.querySelector('body > section > div.am-container.am-margin-vertical-xl > div > div').style.display=\"none\";}setTop();");
        //网页尾部
        view.loadUrl("JavaScript:function setTop(){document.querySelector('body > footer').style.display=\"none\";}setTop();");
        //播放界面
        view.loadUrl("JavaScript:function setTop(){document.querySelector('#music-show').style.display=\"none\";}setTop();");
    }


    private class getColorTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String accent_color = sharedPref.getString("accent_color", "");
            switch (accent_color) {
                case "red":
                    return R.color.md_red_500;
                case "pink":
                    return R.color.md_pink_500;
                case "purple":
                    return R.color.md_purple_500;
                case "deep_purple":
                    return R.color.md_deep_purple_500;
                case "indigo":
                    return R.color.md_indigo_500;
                case "blue":
                    return R.color.md_blue_500;
                case "light_blue":
                    return R.color.md_light_blue_500;
                case "cyan":
                    return R.color.md_cyan_500;
                case "teal":
                    return R.color.md_teal_500;
                case "green":
                    return R.color.md_green_500;
                case "light_green":
                    return R.color.md_light_green_500;
                case "lime":
                    return R.color.md_lime_500;
                case "yellow":
                    return R.color.md_yellow_500;
                case "amber":
                    return R.color.md_amber_500;
                case "orange":
                    return R.color.md_orange_500;
                case "deep_orange":
                    return R.color.md_deep_orange_500;
                default:
            }
            return R.color.md_pink_500;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            int color = getResources().getColor((int)o);
            refresh.setColorSchemeColors(color);
        }
    }
}
