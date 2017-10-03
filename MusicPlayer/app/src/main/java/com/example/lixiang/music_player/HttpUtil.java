package com.example.lixiang.music_player;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lixiang on 2017/10/3.
 */

public class HttpUtil {
    public static String getPic(String song,String singer){
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody;
            if (singer.equals("<unknown>")){
                requestBody = new FormBody.Builder().add("music_input", song).add("music_filter", "name").add("music_type", "163").build();
            }else{
                requestBody = new FormBody.Builder().add("music_input", song+singer).add("music_filter", "name").add("music_type", "163").build();
            }
            Request request = new Request.Builder().url("http://www.yove.net/yinyue/").addHeader("Origin", "http://www.yove.net").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Accept", "application/json, text/javascript, */*; q=0.01").post(requestBody).build();
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            JSONObject jsonObject = new JSONObject(res);
            if (jsonObject.getInt("code") == 200) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                String data = jsonArray.toString();
                Gson gson = new Gson();
                List<Music> lyricList = gson.fromJson(data, new TypeToken<List<Music>>() {
                }.getType());
                return lyricList.get(0).getPic();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


}
