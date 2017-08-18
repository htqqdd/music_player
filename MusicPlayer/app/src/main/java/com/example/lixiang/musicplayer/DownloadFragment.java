package com.example.lixiang.musicplayer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;

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
        //开启JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://music.2333.me/");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if(!TextUtils.isEmpty(title)&&(title.contains("找不到网页") || title.toLowerCase().contains("error"))){
                    webView.loadUrl("about:blank");
                    webView.invalidate();
                    if (!hasNetwork(getActivity())){
                        Toasty.error(getActivity(),"请检查您的网络连接", Toast.LENGTH_SHORT,true).show();
                    } else {
                        Toasty.info(getActivity(),"服务器开小差了，请稍后再试", Toast.LENGTH_SHORT,true).show();
                    }
                    Log.v("标题","错误");
                }
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >75){
                    refresh.setRefreshing(false);
                    webView.setVisibility(View.VISIBLE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

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

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webView.loadUrl("about:blank");
                webView.invalidate();
                if (!hasNetwork(getActivity())){
                    Toasty.error(getActivity(),"请检查您的网络连接", Toast.LENGTH_SHORT,true).show();
                } else {
                    Toasty.info(getActivity(),"服务器开小差了，请稍后再试", Toast.LENGTH_SHORT,true).show();
                }

                Log.v("错误","错误");
                super.onReceivedError(view, request, error);
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.setVisibility(GONE);
                webView.loadUrl("http://music.2333.me/");
            }
        });

        new getColorTask().execute();

        return rootView;
    }



    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void clean(WebView view) {
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


    private class getColorTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int accent_color = sharedPref.getInt("accent_color", 0);
            if (accent_color != 0) {
                return accent_color;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o !=null){
            refresh.setColorSchemeColors((int) o);
            } else {
                refresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
            }
        }
    }

    private boolean hasNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }
}
