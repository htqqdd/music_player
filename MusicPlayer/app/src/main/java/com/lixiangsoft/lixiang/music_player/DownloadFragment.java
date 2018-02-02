package com.lixiangsoft.lixiang.music_player;

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
    private String type = "netease";
    private int page =1;
    private FloatingActionButton search_list;
    private String[] items = {"网易云音乐", "QQ音乐", "酷狗音乐","酷我音乐","虾米音乐","百度音乐","一听音乐","咪咕音乐","荔枝音乐","蜻蜓音乐","喜马拉雅","5Sing音乐"};
    private String[] item_type = {"netease","qq","kugou","kuwo","xiami","baidu","1ting","migu","lizhi","qingting","ximalaya","5singyc"};
    private String previousType = "netease";
    private List<Music> musicList;
    private List<musicInfo> netMusicList;
    private SwipeRefreshLayout refresh;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_download, container, false);
        refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        search_list = (FloatingActionButton) rootView.findViewById(R.id.search_list);
        new setDefaultTask().execute();
        final TextInputLayout inputLayout = (TextInputLayout) rootView.findViewById(R.id.name_input);
        RadioButton name_radio = (RadioButton) rootView.findViewById(R.id.name_radio);
        RadioButton id_radio = (RadioButton) rootView.findViewById(R.id.id_radio);
        RadioButton url_radio = (RadioButton) rootView.findViewById(R.id.link_radio);
        name_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：不要说话 陈奕迅");filter = "name";
            }
        });
        id_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：25906124");filter = "id";
            }
        });
        url_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) inputLayout.setHint("例如：http://music.163.com/#/song?id=25906124");filter = "url";
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
                RequestBody requestBody = new FormBody.Builder().add("input", input).add("filter", filter).add("type", type).add("page",String.valueOf(page)).build();
                Request request = new Request.Builder().url("https://music.2333.me").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36").addHeader("Referer","https://music.2333.me/?name="+java.net.URLEncoder.encode(input,"utf-8")+"&type="+type).post(requestBody).build();
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
                        netMusicList.add(new musicInfo(0,0,0,musicList.get(i).getName(),musicList.get(i).getAuthor(),musicList.get(i).getMusic(),musicList.get(i).getRealPic(),0,musicList.get(i).getLink()));
                    }
                    MyApplication.setNetMusiclist(netMusicList);
                    return "200";
                }
            } catch (Exception e) {
                if (e instanceof java.net.UnknownHostException) {
                    return "404";
                }else if (e instanceof java.net.SocketTimeoutException){
                    return "timeout";
                }
                e.printStackTrace();
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
                    Bundle bundle = new Bundle();
                    bundle.putString("input",input);
                    bundle.putString("filter",filter);
                    bundle.putString("type",type);
                    Intent intent = new Intent(getActivity(), netMusicActivity.class);
                    intent.putExtra("info",bundle);
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
                case "timeout":
                    Snackbar.make(rootView, "服务器连接超时，请稍后再试", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
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
            input = editText.getText().toString();
            if (input.equals("")) {
                Snackbar.make(rootView, "请输入内容", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            } else {
                refresh.setRefreshing(true);
                new httpTask().execute(input);
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
                        type = item_type[i];
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
            type = bundle.getString("default", "netease");
            for (int i = 0; i < 12; i++) {
                if (type.equals(item_type[i])) return i;
            }
            return null;
        }
    }


}
