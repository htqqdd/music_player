package com.example.lixiang.music_player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFragment extends Fragment {


    public DownloadFragment() {
        // Required empty public constructor
    }

    private View rootView;
    private EditText editText;
    private String input;
    private String filter = "name";
    private String type = "163";
    private CharSequence[] items;
    private RadioButton name_radio;
    private RadioButton id_radio;
    private RadioButton url_radio;
    private FloatingActionButton search_list;
    private String previousType = "163";
    private List<Music> musicList;
    private List<musicInfo> netMusicList;
    private SwipeRefreshLayout refresh;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_download, container, false);
        items = new CharSequence[12];
        items[0] = "网易云音乐";
        items[1] = "QQ音乐";
        items[2] = "酷狗音乐";
        items[3] = "酷我音乐";
        items[4] = "虾米音乐";
        items[5] = "百度音乐";
        items[6] = "一听音乐";
        items[7] = "咪咕音乐";
        items[8] = "荔枝音乐";
        items[9] = "蜻蜓音乐";
        items[10] = "5Sing音乐";
        items[11] = "SoundCloud";

        refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        search_list = (FloatingActionButton) rootView.findViewById(R.id.search_list);
        new setDefaultTask().execute();
        final TextInputLayout inputLayout = (TextInputLayout) rootView.findViewById(R.id.name_input);
        name_radio = (RadioButton) rootView.findViewById(R.id.name_radio);
        id_radio = (RadioButton) rootView.findViewById(R.id.id_radio);
        url_radio = (RadioButton) rootView.findViewById(R.id.link_radio);
        name_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：不要说话 陈奕迅");
            }
        });
        id_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：25906124");
            }
        });
        url_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：http://music.163.com/#/song?id=25906124");
            }
        });
        editText = (EditText) rootView.findViewById(R.id.edit_text);
        editText.setOnEditorActionListener(new DoneOnEditorActionListener());
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                editText.setText("");
                refresh.setRefreshing(false);
            }
        });
        Button search = (Button) rootView.findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchAction();
            }
        });
        new getColorTask().execute();
        return rootView;
    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("default_resource", MODE_PRIVATE).edit();
        editor.putString("default", type);
        editor.apply();
        Log.v("执行", "退出" + type);
        super.onDestroy();
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

    private class httpTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                if (filter.equals("url")) {
                    previousType = type;
                    type = "_";
                }
                RequestBody requestBody = new FormBody.Builder().add("music_input", strings[0]).add("music_filter", filter).add("music_type", type).build();
                Request request = new Request.Builder().url("http://www.yove.net/yinyue/").addHeader("Origin", "http://www.yove.net").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Accept", "application/json, text/javascript, */*; q=0.01").post(requestBody).build();
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.getInt("code") == 200) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String data = jsonArray.toString();
                    Gson gson = new Gson();
                    musicList = gson.fromJson(data, new TypeToken<List<Music>>() {
                    }.getType());
                    netMusicList = new ArrayList<musicInfo>();
                    for (int i = 0; i < musicList.size(); i++) {
                        netMusicList.add(new musicInfo(musicList.get(i).getSongid(),0,0,musicList.get(i).getName(),musicList.get(i).getAuthor(),musicList.get(i).getMusic(),musicList.get(i).getRealPic(),0,musicList.get(i).getLink()));
                    }
                    MyApplication.setNetMusiclist(netMusicList);
//                    MyApplication.setMusicListNow(netMusicList,"netMusicList");
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
            refresh.setRefreshing(false);
            if (filter.equals("url")) {
                type = previousType;
            }
            switch (s) {
                case "200":
                    Intent intent = new Intent(getActivity(), netMusicActivity.class);
                    startActivity(intent);
                    break;
                case "404":
                    if (!hasNetwork(getActivity())) {
                        Snackbar.make(rootView, "请检查您的网络", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
                    } else {
                        Snackbar.make(rootView, "服务器开小差了，请稍后再试", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
                    }
                    break;
                case "unKnown":
                    Snackbar.make(rootView, "未获取到资源", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    }).show();
                    break;
                default:
            }
            super.onPostExecute(s);
        }

    }


    private void searchAction() {
        if (MyApplication.getLocal_net_mode() == false) {
            if (name_radio.isChecked()) {
                filter = "name";
            } else if (id_radio.isChecked()) {
                filter = "id";
            } else {
                filter = "url";
            }
            input = editText.getText().toString();
            if (input.equals("")) {
                Snackbar.make(rootView, "请输入内容", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            } else {
                refresh.setRefreshing(true);
                new httpTask().execute(editText.getText().toString());
            }
        } else {
            Toast.makeText(getActivity(), "当前处于离线模式", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListListener(final int defaultNumber) {
        search_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("选择来源");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setSingleChoiceItems(items, defaultNumber, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                type = "163";
                                break;
                            case 1:
                                type = "qq";
                                break;
                            case 2:
                                type = "kugou";
                                break;
                            case 3:
                                type = "kuwo";
                                break;
                            case 4:
                                type = "xiami";
                                break;
                            case 5:
                                type = "baidu";
                                break;
                            case 6:
                                type = "1ting";
                                break;
                            case 7:
                                type = "migu";
                                break;
                            case 8:
                                type = "lizhi";
                                break;
                            case 9:
                                type = "qingting";
                            case 10:
                                type = "5sing";
                                break;
                            case 11:
                                type = "soundcloud";
                                break;
                            default:
                        }
                        Log.v("类型", "类型" + type);
                        setListListener(i);
                    }
                });
                builder.show();
            }
        });
    }

    private class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchAction();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
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
            if (o != null) {
                refresh.setColorSchemeColors((int) o);
            } else {
                refresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
            }
        }
    }

    private class setDefaultTask extends AsyncTask {
        @Override
        protected void onPostExecute(Object o) {
            if (o != null) {
                setListListener((int) o);
            }
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            SharedPreferences bundle = getActivity().getSharedPreferences("default_resource", MODE_PRIVATE);
            Log.v("执行", "读取" + bundle.getString("default", "163"));
            switch (bundle.getString("default", "163")) {
                case "163":
                    type = "163";
                    return 0;
                case "qq":
                    type = "qq";
                    return 1;
                case "kugou":
                    type = "kugou";
                    return 2;
                case "kuwo":
                    type = "kuwo";
                    return 3;
                case "xiami":
                    type = "xiami";
                    return 4;
                case "baidu":
                    type = "baidu";
                    return 5;
                case "1ting":
                    type = "1ting";
                    return 6;
                case "migu":
                    type = "migu";
                    return 7;
                case "lizhi":
                    type = "lizhi";
                    return 8;
                case "qingting":
                    type = "qingting";
                    return 9;
                case "5sing":
                    type = "5sing";
                    return 10;
                case "soundcloud":
                    type = "soundcloud";
                    return 11;
                default:
            }
            return null;
        }
    }


}
