package com.lixiangsoft.lixiang.music_player;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.aesthetic.TabLayoutBgMode;
import com.afollestad.aesthetic.TabLayoutIndicatorMode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gyf.barlibrary.ImmersionBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import me.wcy.lrcview.LrcView;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.lixiangsoft.lixiang.music_player.R.id.delete;
import static com.lixiangsoft.lixiang.music_player.R.id.item_touch_helper_previous_elevation;
import static com.lixiangsoft.lixiang.music_player.R.id.play_now_cover_viewpager;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.DRAGGING;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

@RuntimePermissions
public class MainActivity extends AestheticActivity {
    private static SeekBar seekBar;
    private ViewPager viewPager;
    private MsgReceiver msgReceiver;
    private int flag = 0;
    private int cx = 0;
    private int cy = 0;
    private int finalRadius = 0;
    private PlayService playService;
    private Timer mTimer;
    private NavigationView navigationView;
    private FloatingActionButton random_play;
    private SlidingUpPanelLayout mLayout;
    private CardView main_control_ui;
    private ImageView play_pause_button;
    private ImageView back;
    private ImageView about;
    private LrcView otherLyricView;
    private boolean fromLyric = false;
    boolean visible;
    private ViewGroup transitionsContainer;
    private boolean isfromSc = false;
    //匹配
    private TextView match_title;
    private TextView match_progress;
    private ImageView match_album;
    private int match_position;
    private int match_error;
    private List<musicInfo> musicInfoArrayList;
    private Dialog match_dialog;
    private ViewPager play_now_cover_viewPager;
    private List<musicInfo> previousList;
    private RequestListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("OnCreate","OnCreate");
        setContentView(R.layout.activity_main);

        //沉浸状态栏
        ImmersionBar.with(MainActivity.this).statusBarView(R.id.immersion_view).init();

        if (Aesthetic.isFirstTime()) {
            Aesthetic.get()
                    .colorPrimaryRes(R.color.colorPrimary)
                    .colorAccentRes(R.color.colorAccent)
                    .colorCardViewBackgroundRes(R.color.cardview_light_background)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .apply();
        }

        //初始化全局变量
        seekBar = findViewById(R.id.seekBar);
        navigationView = findViewById(R.id.nav_view);
        random_play = findViewById(R.id.random_play);
        mLayout = findViewById(R.id.sliding_layout);
        play_pause_button = findViewById(R.id.play_pause_button);
        back = findViewById(R.id.back);
        about = findViewById(R.id.about);
        otherLyricView = findViewById(R.id.other_lrc_view);
        transitionsContainer = findViewById(R.id.activity_now_play);
        main_control_ui = findViewById(R.id.main_control_ui);
        play_now_cover_viewPager = findViewById(R.id.play_now_cover_viewpager);
        listener = new RequestListener() {
            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                Bitmap resource = ((BitmapDrawable) MainActivity.this.getDrawable(R.drawable.default_album)).getBitmap();
                animation_change_color(ColorUtil.getColor(resource));
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                animation_change_color(ColorUtil.getColor((Bitmap) resource));
                return false;
            }
        };
        View.OnClickListener lyric_onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!otherLyricView.hasLrc()) {
                    //判断是否有用户自己储存歌词
                    musicInfo musicNow = MyApplication.getMusicListNow().get(MyApplication.getPositionNow());
                    String path = musicNow.getMusicData();
                    int total = path.length();
                    File file = new File(path.substring(0, total - 4) + ".lrc");
                    //判断是否有程序储存歌词
                    File file2 = new File(MainActivity.this.getFilesDir().getAbsolutePath() + "/" + musicNow.getMusicTitle() + ".lrc");
                    if (file.isFile() && file.exists()) {
                        //加载歌词
                        otherLyricView.loadLrc(file);
                        changeVisibility();
                        otherLyricView.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                            @Override
                            public boolean onPlayClick(long time) {
                                fromLyric = true;
                                seekBar.setProgress((int) time);
                                return false;
                            }
                        });
                    } else if (file2.isFile() && file2.exists()) {
                        otherLyricView.loadLrc(file2);
                        changeVisibility();
                        otherLyricView.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                            @Override
                            public boolean onPlayClick(long time) {
                                fromLyric = true;
                                seekBar.setProgress((int) time);
                                return false;
                            }
                        });
                    } else if (MyApplication.getLocal_net_mode() == false) {
                        new getLyricTask().execute(musicNow.getMusicTitle(), musicNow.getMusicArtist());
                        changeVisibility();
                    } else {
                        Toast.makeText(MainActivity.this, "当前处于离线模式", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    changeVisibility();
                }
            }
        };
        MyApplication.setlyric_onClickListener(lyric_onClickListener);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (mLayout.getPanelHeight() == 0) {
                        random_play.show();
                    }
                    navigationView.setCheckedItem(R.id.suggestion_view);
                } else if (position == 1) {
                    if (mLayout.getPanelHeight() == 0) {
                        random_play.show();
                    }
                    navigationView.setCheckedItem(R.id.music_list_view);
                } else if (position == 3) {
                    if (mLayout.getPanelHeight() == 0) {
                        random_play.show();
                    }
                    navigationView.setCheckedItem(R.id.custom_list_view);
                } else {
                    random_play.hide();
                    navigationView.setCheckedItem(R.id.download_view);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        //设置起始页
        Intent sc_intent = getIntent();
        String sc_action = sc_intent.getStringExtra("sc_action");
        if (sc_action != null) {
            isfromSc = true;
            switch (sc_action) {
                case "cloud":
                    viewPager.setCurrentItem(2);
                    break;
                case "library":
                    viewPager.setCurrentItem(0);
                    break;
                case "list":
                    viewPager.setCurrentItem(1);
                    break;
                default:
            }
        }

        //多屏幕尺寸适应
        new screenAdaptionTask().execute();


        //新标题栏
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        main_toolbar.inflateMenu(R.menu.main_menu);
        setSupportActionBar(main_toolbar);

        //侧滑栏动画
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, main_toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View navHeader = inflater.inflate(R.layout.nav_header, (ViewGroup) MainActivity.this.findViewById(R.id.nav_header));
                ImageView imageView = (ImageView) navHeader.findViewById(R.id.header);
                if (sharedPref.getString("main_theme", "day").equals("day")) {
                    Glide.with(MainActivity.this).load(R.drawable.nav).into(imageView);
                } else {
                    Glide.with(MainActivity.this).load(R.drawable.nav_black).into(imageView);
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (flag) {
                    case 0:
                        break;
                    case R.id.nav_settings:
                        Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings_intent);
                        flag = 0;
                        break;
                    case R.id.nav_about:
                        Intent about_intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(about_intent);
                        flag = 0;
                        break;
                    default:
                }
            }
        };
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(mDrawerToggle);
        //Nac_view点击
        navigationView.setCheckedItem(R.id.suggestion_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.suggestion_view) {
                    viewPager.setCurrentItem(0);
                } else if (id == R.id.music_list_view) {
                    viewPager.setCurrentItem(1);
                } else if (id == R.id.download_view) {
                    viewPager.setCurrentItem(2);
                } else if (id == R.id.custom_list_view) {
                    viewPager.setCurrentItem(3);
                } else if (id == R.id.nav_settings) {
                    flag = id;
                } else if (id == R.id.nav_about) {
                    flag = id;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        //动态注册广播
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("play_broadcast");
        registerReceiver(msgReceiver, intentFilter);

        //上滑面板
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(slideOffset, 1 - slideOffset);
                main_control_ui.startAnimation(alphaAnimation);
                alphaAnimation.setFillAfter(true);//动画结束后保持状态
                alphaAnimation.setDuration(0);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState == COLLAPSED && newState == DRAGGING) {
                    updateSeekBar();
                }
                if (previousState == DRAGGING && newState == EXPANDED) {
                    //禁止手势滑动
                    main_control_ui.setClickable(false);
                    play_pause_button.setClickable(false);
                    about.setClickable(true);
                    back.setClickable(true);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                if (previousState == DRAGGING && newState == COLLAPSED) {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    main_control_ui.setClickable(true);
                    play_pause_button.setClickable(true);
                    about.setClickable(false);
                    back.setClickable(false);
                    //恢复手势滑动
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLayout != null &&
                        (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    mLayout.setPanelState(COLLAPSED);
                }
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
                popupMenu.show();
                MenuItem search = popupMenu.getMenu().findItem(R.id.search);
                MenuItem sleeper = popupMenu.getMenu().findItem(R.id.sleeper);
                MenuItem equalizer = popupMenu.getMenu().findItem(R.id.equalizer);
                search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent intent = new Intent(MainActivity.this, searchActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
                sleeper.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        //Timepicker Dialog
                        sleeper();
                        return true;
                    }
                });
                equalizer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        equalizer();
                        return false;
                    }
                });
            }
        });


        main_control_ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(EXPANDED);
            }
        });


        if (MyApplication.getState() == MyConstant.playing) {
            ChangeScrollingUpPanel(MyApplication.getPositionNow());
            random_play.hide();
            mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelHeight((int) (60 * getResources().getDisplayMetrics().density + 0.5f));
        }

        final ColorShades shades = new ColorShades();
        play_now_cover_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int previousState;
            boolean fromUser = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (fromUser) {
                    MyApplication.setPositionNow(position);
                    Intent intent = new Intent("service_broadcast");
                    intent.putExtra("ACTION", MyConstant.playAction);
                    MainActivity.this.sendBroadcast(intent);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING && state == ViewPager.SCROLL_STATE_SETTLING) {
                    fromUser = true;
                } else {
                    fromUser = false;
                }
                previousState = state;
            }
        });

        MainActivityPermissionsDispatcher.needsPermissionWithCheck(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem sleeper = menu.findItem(R.id.sleeper);
        MenuItem equalizer = menu.findItem(R.id.equalizer);
        final MenuItem match_lyric_album = menu.findItem(R.id.match_lyric_album);
        search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, searchActivity.class);
                startActivity(intent);
                return true;
            }
        });
        sleeper.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //Timepicker Dialog
                sleeper();
                return true;
            }
        });
        equalizer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                equalizer();
                return false;
            }
        });
        match_lyric_album.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                match_all();
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onStart() {
        ensureServiceStarted();
        //绑定服务
        Intent bindIntent = new Intent(this, PlayService.class);
        bindService(bindIntent, conn, BIND_AUTO_CREATE);

        //拖动seekbar
        seekBar.setPadding(5, 0, 5, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playService != null) {
                    if (fromUser || fromLyric) {
                        playService.seekto(progress);
                        fromLyric = false;
                    }
                    if (MyApplication.getState() == MyConstant.playing) {
                        otherLyricView.updateTime(playService.getCurrentPosition());
                    }
                }
                //更新Seekbar的时间显示
                TextView currentPosition = (TextView) findViewById(R.id.current_position);
                currentPosition.setText(toTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        random_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.setPlayMode(MyConstant.random);
                MyApplication.setMusicListNow(MyApplication.getMusicInfoArrayList(), "musicInfoArrayList");
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", MyConstant.random_playAction);
                sendBroadcast(intent);
            }
        });

        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mLayout != null &&
                (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(COLLAPSED);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(msgReceiver);
        Log.v("OnDestory执行", "OnDestory");
        if (MyApplication.getState() == MyConstant.pausing) {
            unbindService(conn);
            stopService(new Intent(this, PlayService.class));
        }
        super.onDestroy();
    }
    //以下为公共方法

    public void sendPermissionGranted() {
        new initialTask().execute();
    }

    public class initialTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            MyApplication.initialMusicInfo(MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            //更新Recommend界面（全部）
            Intent intent = new Intent("permission_granted");
            sendBroadcast(intent);
            //更新fastscroll界面
            Intent intent2 = new Intent("list_permission_granted");
            sendBroadcast(intent2);
            super.onPostExecute(o);
        }
    }

    //播放，暂停按钮
    public void title_play_or_pause(View view) {
        if (MyApplication.getState() == MyConstant.playing) {
            playService.pause();
            playService.removeAudioFocus();
        } else if (MyApplication.getState() == MyConstant.pausing) {
            playService.resume();
        }
    }

    public void main_play_or_pause(View v) {
        if (MyApplication.getState() == MyConstant.playing) {
            playService.pause();
            playService.removeAudioFocus();
        } else if (MyApplication.getState() == MyConstant.pausing) {
            playService.resume();
        }
    }

    public void previous(View v) {
        playService.previous();

    }

    public void next(View v) {
        playService.next();
    }

    public void changeRepeat(View v) {
        //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
        ImageView repeat_button = (ImageView) findViewById(R.id.repeat_button);
        ImageView shuffle_button = (ImageView) findViewById(R.id.shuffle_button);

        switch (MyApplication.getPlayMode()) {
            case MyConstant.list:
                MyApplication.setPlayMode(MyConstant.list_repeat);
                Snackbar.make(mLayout, "列表循环播放", Snackbar.LENGTH_SHORT).show();
                repeat_button.setImageResource(R.drawable.repeat);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case MyConstant.list_repeat:
                MyApplication.setPlayMode(MyConstant.one_repeat);
                Snackbar.make(mLayout, "单曲循环播放", Snackbar.LENGTH_SHORT).show();
                repeat_button.setImageResource(R.drawable.repeat_one);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case MyConstant.one_repeat:
                MyApplication.setPlayMode(MyConstant.list);
                Snackbar.make(mLayout, "顺序播放", Snackbar.LENGTH_SHORT).show();
                repeat_button.setImageResource(R.drawable.repeat_grey);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case MyConstant.random:
                MyApplication.setPlayMode(MyConstant.list_repeat);
                Snackbar.make(mLayout, "列表重复播放", Snackbar.LENGTH_SHORT).show();
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                repeat_button.setImageResource(R.drawable.repeat);
                break;
            default:
        }
    }

    public void changeShuffle(View v) {
        ImageView repeat_button = (ImageView) findViewById(R.id.repeat_button);
        ImageView shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        if (MyApplication.getPlayMode() == MyConstant.random) {
            MyApplication.setPlayMode(MyConstant.list);
            Snackbar.make(mLayout, "顺序播放", Snackbar.LENGTH_SHORT).show();
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        } else {
            MyApplication.setPlayMode(MyConstant.random);
            Snackbar.make(mLayout, "随机播放", Snackbar.LENGTH_SHORT).show();
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        }
    }

    public void animation_change_color(final int Int) {
        ImageView play_now_back_color = (ImageView) findViewById(R.id.play_now_back_color);
        final RelativeLayout activity_now_play = (RelativeLayout) findViewById(R.id.activity_now_play);
        if (cx == 0) {
            FloatingActionButton play_or_pause = (FloatingActionButton) findViewById(R.id.play_or_pause);
            RelativeLayout seekbar_layout = (RelativeLayout) findViewById(R.id.seekbar_layout);
            RelativeLayout control_layout = (RelativeLayout) findViewById(R.id.control_layout);
            cx = play_or_pause.getLeft() + control_layout.getLeft() + play_or_pause.getWidth() / 2;
            cy = control_layout.getTop() - seekbar_layout.getTop() + play_or_pause.getTop() + play_or_pause.getHeight() / 2;
            finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());
        }
        if (cx != 0) {
            Animator anim = ViewAnimationUtils.createCircularReveal(play_now_back_color, cx, cy, 0, finalRadius);
            play_now_back_color.setBackgroundColor(Int);
            anim.setDuration(500);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    activity_now_play.setBackgroundColor(Int);
                }
            });
        } else {
            activity_now_play.setBackgroundColor(Int);
        }
        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        now_on_play_text.setTextColor(Int);
        //lrcview字体颜色
        if (ColorUtil.isColorLight(Int)) {
            otherLyricView.setNormalColor(Color.parseColor("#60000000"));
            otherLyricView.setTimelineTextColor(Color.parseColor("#000000"));
            otherLyricView.setCurrentColor(Color.parseColor("#000000"));
        } else {
            otherLyricView.setNormalColor(Color.parseColor("#60FFFFFF"));
            otherLyricView.setTimelineTextColor(Color.parseColor("#FFFFFF"));
            otherLyricView.setCurrentColor(Color.parseColor("#FFFFFF"));
        }

        //歌词背景颜色
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            View bottom = findViewById(R.id.gradient_bottom);
            View top = findViewById(R.id.gradient_top);
            View gradient = findViewById(R.id.gradient);
            top.setBackground(
                    ScrimUtil.makeCubicGradientScrimDrawable(Int, //颜色
                            8, //渐变层数
                            Gravity.TOP)); //起始方向
            bottom.setBackground(
                    ScrimUtil.makeCubicGradientScrimDrawable(Int, //颜色
                            8, //渐变层数
                            Gravity.BOTTOM)); //起始方向
            gradient.setBackground(
                    ScrimUtil.makeCubicGradientScrimDrawable(Int, //颜色
                            8, //渐变层数
                            Gravity.BOTTOM)); //起始方向
        }
    }

    public void ChangeScrollingUpPanel(int position) {
        seekBar.setProgress(0);
        seekBar.setMax(MyApplication.getMediaDuration());
        final TextView duration = (TextView) findViewById(R.id.duration);
        duration.setText(toTime(MyApplication.getMediaDuration()));
        //设置歌曲名，歌手
        musicInfo nowMusic = MyApplication.getMusicListNow().get(position);
        String title = nowMusic.getMusicTitle();
        String artist = nowMusic.getMusicArtist();
        TextView play_now_song = (TextView) findViewById(R.id.play_now_song);
        TextView play_now_singer = (TextView) findViewById(R.id.play_now_singer);
        play_now_song.setText(title);
        play_now_singer.setText(artist);
        //小控制栏
        TextView main_song_title = (TextView) findViewById(R.id.main_song_title);
        main_song_title.setText(title);
        final ImageView repeat_button = (ImageView) findViewById(R.id.repeat_button);
        ImageView shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
        //判断是否更改List
        if (previousList != MyApplication.getMusicListNow()) {
            playNowCoverPagerAdapter coverPagerAdapter = new playNowCoverPagerAdapter(MainActivity.this);
            play_now_cover_viewPager.setAdapter(coverPagerAdapter);
            previousList = MyApplication.getMusicListNow();
        }
        play_now_cover_viewPager.setCurrentItem(MyApplication.getPositionNow());

        if (otherLyricView.getVisibility() == VISIBLE) {
            changeVisibility();
        }
        //animationChangeColor
        if (!nowMusic.getMusicLink().equals("")) {//网络
            Glide.with(this).load(nowMusic.getMusicLargeAlbum()).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(listener).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        } else {//本地
            if (nowMusic.getAlbumLink() != null) {//本地下载
                Glide.with(this).load(nowMusic.getAlbumLink()).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(listener).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            } else {//本地原有
                Glide.with(this).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), nowMusic.getMusicAlbumId())).asBitmap().listener(listener).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }
        }

        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        now_on_play_text.setText("正在播放");
        otherLyricView.loadLrc("");
        //设置播放模式按钮
        int playMode = MyApplication.getPlayMode();
        if (playMode == MyConstant.list_repeat) {
            repeat_button.setImageResource(R.drawable.repeat);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        } else if (playMode == MyConstant.random) {
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        } else if (playMode == MyConstant.one_repeat) {
            repeat_button.setImageResource(R.drawable.repeat_one);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        } else {
            repeat_button.setImageResource(R.drawable.repeat_grey);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }

        //设置播放按钮
        if (MyApplication.getState() == MyConstant.playing) {
            floatingActionButton.setImageResource(R.drawable.ic_pause_black_24dp);
            play_pause_button.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            play_pause_button.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }


    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needsPermission() {
        sendPermissionGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("权限概述")
                .setMessage("MediaPlayer如果缺少以下关键权限可能无法正常工作。\n\n储存权限：读取媒体、照片、文件权限用于获取歌曲信息并播放。")
                .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("仍不允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void permissionDenied() {
        new AlertDialog.Builder(this)
                .setTitle("权限概述")
                .setMessage("MediaPlayer如果缺少以下关键权限可能无法正常工作。\n\n储存权限：读取媒体、照片、文件权限用于获取歌曲信息并播放。")
                .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivityPermissionsDispatcher.needsPermissionWithCheck(MainActivity.this);
                    }
                })
                .setNegativeButton("仍不允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();

    }

    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新UI
            if (intent.getIntExtra("UIChange", 0) == MyConstant.initialize) {
                mLayout.setPanelHeight((int) (60 * getResources().getDisplayMetrics().density + 0.5f));
                random_play.hide();
            } else if (intent.getIntExtra("UIChange", 0) == MyConstant.pauseAction) {
                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
                floatingActionButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                play_pause_button.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            } else if (intent.getIntExtra("UIChange", 0) == MyConstant.playAction) {
                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
                floatingActionButton.setImageResource(R.drawable.ic_pause_black_24dp);
                play_pause_button.setImageResource(R.drawable.ic_pause_black_24dp);
            } else if (intent.getIntExtra("UIChange", 0) == MyConstant.mediaChangeAction) {
                ChangeScrollingUpPanel(MyApplication.getPositionNow());
            } else if (intent.getIntExtra("onDestroy", 0) == 1) {
                finish();
            }
        }

    }

    public void play_now_menu_button(View view) {
        if (MyApplication.getListlabel().equals("netMusicList")) {
            final int position = MyApplication.getPositionNow();
            android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.net_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.openLink:
                            if (!MyApplication.getNetMusiclist().get(position).getMusicLink().equals("")) {
                                Uri web_uri = Uri.parse(MyApplication.getNetMusiclist().get(position).getMusicLink());
                                Intent intent = new Intent(Intent.ACTION_VIEW, web_uri);
                                MainActivity.this.startActivity(intent);
                            } else {
                                Toast.makeText(MainActivity.this, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case R.id.getLink:
                            if (!MyApplication.getNetMusiclist().get(position).getMusicData().equals("")) {
                                Uri download_uri = Uri.parse(MyApplication.getNetMusiclist().get(position).getMusicData());
                                Intent web_intent = new Intent(Intent.ACTION_VIEW, download_uri);
                                MainActivity.this.startActivity(web_intent);
                            } else {
                                Toast.makeText(MainActivity.this, "未获取到链接，请尝试更换提供方", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                    }
                    return true;
                }
            });
            popup.show(); //showing popup menu
        } else {//正在播放列表
            android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(MainActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.list_popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.setAsNext:
                            setAsNext(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                        case R.id.searchForAlbum:
                            Toast.makeText(MainActivity.this, "请在“歌曲”界面中执行该操作", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.addTo:
                            addTo(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                        case R.id.hidefromlist:
                            hidefromlist(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                        case delete:
                            deleteFile(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                        case R.id.setAsRingtone:
                            setAsRingtone(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                        case R.id.musicInfo:
                            showMusicInfo(MainActivity.this, MyApplication.getPositionNow());
                            return true;
                    }
                    return true;
                }
            });
            popup.show(); //showing popup menu
        }

    }

    private void ensureServiceStarted() {
        if (MyApplication.getServiceState() == false) {
            Intent intent = new Intent(this, PlayService.class);
            intent.putExtra("ACTION", MyConstant.initialize);
            startService(intent);

        }
    }

    private class screenAdaptionTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String start_page = sharedPref.getString("start_page", "");
            //检测网络模式设置
            Boolean local_net_mode = sharedPref.getBoolean("local_net_mode", false);
            MyApplication.setLocal_net_mode(local_net_mode);
            return start_page;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (!isfromSc) {
                switch ((String) o) {
                    case "suggestion":
                        viewPager.setCurrentItem(0);
                        break;
                    case "list":
                        viewPager.setCurrentItem(1);
                        break;
                    case "cloud":
                        viewPager.setCurrentItem(2);
                        break;
                    default:
                }
            }
            ViewPager play_now_cover_viewPager = findViewById(R.id.play_now_cover_viewpager);
            View lrcView = findViewById(R.id.other_lrc_view);
            RelativeLayout control_layout = findViewById(R.id.control_layout);
            CardView music_info_cardView = findViewById(R.id.music_info_cardView);
            ViewGroup.LayoutParams lp_control_layout = control_layout.getLayoutParams();
            RelativeLayout.LayoutParams lp_play_now_cover = (RelativeLayout.LayoutParams) play_now_cover_viewPager.getLayoutParams();
            RelativeLayout.LayoutParams lp_lrcView = (RelativeLayout.LayoutParams) lrcView.getLayoutParams();
            ViewGroup.LayoutParams lp_cardView = music_info_cardView.getLayoutParams();
            lp_play_now_cover.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
            lp_lrcView.height = ((int) (getResources().getDisplayMetrics().heightPixels * 0.4));
            lp_control_layout.height = ((int) (getResources().getDisplayMetrics().heightPixels * 0.16));
            lp_cardView.height = ((int) (getResources().getDisplayMetrics().heightPixels * 0.17));
            play_now_cover_viewPager.setLayoutParams(lp_play_now_cover);
            lrcView.setLayoutParams(lp_lrcView);
            control_layout.setLayoutParams(lp_control_layout);
            music_info_cardView.setLayoutParams(lp_cardView);
            super.onPostExecute(o);
        }
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            playService = ((PlayService.musicBinder) service).getService();
        }
    };

    private void updateSeekBar() {
        mTimer = new Timer();
        final TextView duration = (TextView) findViewById(R.id.duration);
        if (playService != null) {
            duration.setText(toTime(MyApplication.getMediaDuration()));
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (playService != null) {
                    if (MyApplication.getState() == MyConstant.playing) {
                        seekBar.setProgress(playService.getCurrentPosition());
                    }
                }
            }
        };
        mTimer.schedule(task, 0, 1000);
    }

    private void sleeper() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        final int hourNow = c.get(java.util.Calendar.HOUR_OF_DAY);
        final int minuteNow = c.get(Calendar.MINUTE);
        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourPicked, int minutePicked) {
                int duration = (hourPicked - hourNow) * 60 + minutePicked - minuteNow;
                if (hourPicked >= hourNow && duration > 0 && duration < 360) {
                    playService.deleteService(duration);
                    Snackbar.make(mLayout, "已经定时为" + duration + "分钟后关闭", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(mLayout, "所选时间须为当天，且距当前时间6小时内", Snackbar.LENGTH_SHORT).show();
                }
            }
        }, hourNow, minuteNow, true).show();
    }

    private void equalizer() {
        SharedPreferences bundle = MainActivity.this.getSharedPreferences("first_audioEffect", MainActivity.this.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (bundle.getBoolean("isFirst", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("兼容性提醒");
            builder.setMessage("使用内置均衡器前，请确保未使用其他音效软件，否则可能因兼容性问题导致该均衡器无效。\n\n若您仍想使用内置均衡器，请先禁用手机内其他音效软件。");
            builder.setPositiveButton("使用内置均衡器", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                    startActivity(intent);
                    SharedPreferences.Editor editor = getSharedPreferences("first_audioEffect", MODE_PRIVATE).edit();
                    editor.putBoolean("isFirst", false);
                    editor.apply();
                }
            });
            builder.setNegativeButton("使用系统均衡器", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0) {
                        startActivity(intent);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putBoolean("audio_effect", true);
                        editor.apply();
                    } else {
                        Toast.makeText(MainActivity.this, "未发现系统均衡器，使用内置均衡器", Toast.LENGTH_SHORT).show();
                        //打开内置均衡器
                        Intent intent2 = new Intent(MainActivity.this, EqualizerActivity.class);
                        MainActivity.this.startActivity(intent2);
                    }
                    SharedPreferences.Editor editor2 = getSharedPreferences("first_audioEffect", MODE_PRIVATE).edit();
                    editor2.putBoolean("isFirst", false);
                    editor2.apply();
                }
            });
            builder.show();
        } else {
            if (sharedPref.getBoolean("audio_effect", false)) {//打开系统均衡器
                Intent intent = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() > 0) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "未发现系统均衡器，使用内置均衡器", Toast.LENGTH_SHORT).show();
                    //打开内置均衡器
                    Intent intent2 = new Intent(MainActivity.this, EqualizerActivity.class);
                    MainActivity.this.startActivity(intent2);
                }
            } else {
                //打开内置均衡器
                Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                MainActivity.this.startActivity(intent);
            }
        }
    }

    private String toTime(int i) {
        int primary_second = i / 1000;
        int minute = primary_second / 60;
        int second = primary_second - minute * 60;
        return String.format("%2d", minute).replace(" ", "0") + ":" + String.format("%2d", second).replace(" ", "0");
    }

    private class getLyricTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            otherLyricView.setLabel("正在搜索歌词");
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                String input = strings[0];
                if (!strings[1].equals("未知歌手")) {
                    input = input + strings[1];
                }
                RequestBody requestBody = new FormBody.Builder().add("input", input).add("filter", "name").add("type", "netease").add("page", "1").build();
                Request request = new Request.Builder().url("https://music.2333.me").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36").addHeader("Referer", "https://music.2333.me/?name=" + java.net.URLEncoder.encode(input, "utf-8") + "&type=netease").post(requestBody).build();
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.getInt("code") == 200) {
                    Log.e("歌词", "歌曲列表已获得");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String data = jsonArray.toString();
                    Gson gson = new Gson();
                    List<Music> musicList = gson.fromJson(data, new TypeToken<List<Music>>() {
                    }.getType());
                    for (int i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getName().equals(strings[0]) && strings[1].equals("未知歌手")) {
                            return strings[0] + "@" + musicList.get(i).getLrc();
                        }
                        if (musicList.get(i).getName().equals(strings[0]) && musicList.get(i).getAuthor().equals(strings[1])) {
                            return strings[0] + "@" + musicList.get(i).getLrc();
                        }
                    }
                    return "404";
                }
            } catch (Exception e) {
                if (e instanceof java.net.UnknownHostException) {
                    return "404";
                } else if (e instanceof java.net.SocketTimeoutException) {
                    return "timeout";
                }
                e.printStackTrace();
            }
            return "404";
        }

        @Override
        protected void onPostExecute(String s) {
            switch (s) {
                case "404":
                    otherLyricView.setLabel("未搜索到匹配歌词");
                    if (!hasNetwork(MainActivity.this))
                        Snackbar.make(mLayout, "请检查您的网络", Snackbar.LENGTH_SHORT).show();
                    break;
                case "timeout":
                    Snackbar.make(mLayout, "服务器连接超时，请稍后再试", Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    final String Str[] = s.split("@");
                    try {
                        final String lyric = praseLyric(Str[1]);
                        otherLyricView.loadLrc(lyric);
                        otherLyricView.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                            @Override
                            public boolean onPlayClick(long time) {
                                fromLyric = true;
                                seekBar.setProgress((int) time);
                                return false;
                            }
                        });
                        Snackbar.make(mLayout, "保存此歌词？", Snackbar.LENGTH_LONG).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                savelyric(Str[0], lyric);
                            }
                        }).show();
                    } catch (Exception e) {
                        otherLyricView.loadLrc("");
                        otherLyricView.setLabel("未搜索到匹配歌词");
                    }
                    break;
            }
            super.onPostExecute(s);
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

    private String praseLyric(String lyric) {
        try {
            String[] split = lyric.split("\n");
            if (split[split.length - 2].substring(9, 10).equals("]")) {
                //歌曲时间格式标准[04:28.46]
                return lyric;
            } else if (split[split.length - 2].substring(10, 11).equals("]")) {
                //歌曲时间格式不标准[04:28.660][04:28.660]
                lyric = "";
                Format f = new DecimalFormat("00");
                for (int i = 0; i < split.length; i++) {
                    if (!split[i].substring(1, 2).equals("0")) {
                        //有作者标签
                    } else {
                        //无作者标签
                        if (split[i].lastIndexOf("]") != 9) {
                            int allIndex = split[i].indexOf("]");
                            while (allIndex != -1) {
                                split[i] = split[i].substring(0, allIndex - 3) + String.valueOf(f.format(Integer.valueOf(split[i].substring(allIndex - 3, allIndex)) / 50 * 3)) + split[i].substring(allIndex);
                                allIndex = split[i].indexOf("]", allIndex + 1);
                            }
                        }
                        lyric = lyric + split[i] + "\n";
                    }
                }
                return lyric;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lyric;
    }

    private void changeVisibility() {
        View music_info_cardView = findViewById(R.id.music_info_cardView);
        View control_layout = findViewById(R.id.control_layout);
        View seekbar_layout = findViewById(R.id.seekbar_layout);
        View lrcView = findViewById(R.id.other_lrc_view);
        View gradient = findViewById(R.id.gradient);
        View gradient_bottom = findViewById(R.id.gradient_bottom);
        View gradient_top = findViewById(R.id.gradient_top);
        TransitionManager.beginDelayedTransition(transitionsContainer, new TransitionSet().addTransition(new Fade()).setInterpolator(visible ? new LinearOutSlowInInterpolator() : new FastOutLinearInInterpolator()));
        if (music_info_cardView.getVisibility() == VISIBLE) {
            music_info_cardView.setVisibility(GONE);
            control_layout.setVisibility(GONE);
            seekbar_layout.setVisibility(GONE);
            lrcView.setVisibility(VISIBLE);
            gradient.setVisibility(VISIBLE);
            gradient_bottom.setVisibility(VISIBLE);
            gradient_top.setVisibility(VISIBLE);
        } else {
            music_info_cardView.setVisibility(VISIBLE);
            control_layout.setVisibility(VISIBLE);
            seekbar_layout.setVisibility(VISIBLE);
            lrcView.setVisibility(GONE);
            gradient.setVisibility(GONE);
            gradient_bottom.setVisibility(GONE);
            gradient_top.setVisibility(GONE);
        }
        visible = !visible;
    }

    private void savelyric(String name, String lyric) {
        try {
            FileOutputStream fout = openFileOutput(name + ".lrc", MODE_PRIVATE);
            byte[] bytes = lyric.getBytes();
            fout.write(bytes);
            fout.close();
            Snackbar.make(mLayout, "歌词已保存，修改歌曲名后失效", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void match(int position) {
        musicInfoArrayList = MyApplication.getMusicInfoArrayList();
        final musicInfo musicNow = musicInfoArrayList.get(position);
        match_title.setText(musicNow.getMusicTitle());
        int display = match_position + 1;
        match_progress.setText("正在匹配第" + display + "个，共" + musicInfoArrayList.size() + "个");
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, musicInfoArrayList.get(match_position).getMusicAlbumId());
        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.default_album).listener(new RequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                new matchTask().execute(musicNow.getMusicTitle(), musicNow.getMusicArtist());
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                next_match();
                return false;
            }
        }).into(match_album);

    }

    private void next_match() {
        if (match_position < musicInfoArrayList.size() - 1) {
            match_position++;
            match(match_position);
        } else {
            match_dialog.dismiss();
            Toast.makeText(getApplicationContext(), match_error + "首歌曲匹配失败", Toast.LENGTH_SHORT).show();
            MyApplication.reDisplay(getApplicationContext());
        }
    }

    private void match_all() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提醒");
        builder.setMessage("即将联网匹配所有本地歌曲的封面，这将耗费较多流量，请连接至Wifi并在较好的网络条件下继续。提前修正歌曲信息将有助于提高匹配成功率。");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View match_dialog_view = inflater.inflate(R.layout.match_dialog, (ViewGroup) MainActivity.this.findViewById(R.id.match_dialog));
                match_album = match_dialog_view.findViewById(R.id.match_album);
                match_title = match_dialog_view.findViewById(R.id.match_title);
                match_progress = match_dialog_view.findViewById(R.id.match_progress);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("");
                builder.setView(match_dialog_view);
                builder.setCancelable(false);
                match_dialog = builder.show();
                match_error = 0;
                match_position = 0;
                match(match_position);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    //菜单
    private void hidefromlist(Activity context, int position) {
        musicInfo musicInfo = MyApplication.getMusicListNow().get(position);
        musicInfo.setHide(true);
        MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicInfo);
        MyApplication.getMusicListNow().remove(position);
        Snackbar.make(mLayout, "成功隐藏1首歌曲", Snackbar.LENGTH_SHORT).show();
        //更新界面
        playService.next();
        //通知其他adapter
        MyApplication.reDisplay(context);

    }

    private void addTo(final Activity context, final int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View addtolist_dialog = inflater.inflate(R.layout.addtolist, (ViewGroup) context.findViewById(R.id.addtolist_dialog));
        final AlertDialog dialog = showAddtolistDialog(addtolist_dialog, context, position);
        View createlist = addtolist_dialog.findViewById(R.id.create_list);
        createlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showCreateListDialog(context, view);
            }
        });
    }

    private AlertDialog showAddtolistDialog(View v, final Activity context, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加到播放列表");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(v);
        final AlertDialog dialog = builder.show();
//        final List<musicInfo> list = MyApplication.getMusicInfoArrayList();
        ListView listView = (ListView) v.findViewById(R.id.play_list_view);
        final List<Playlist> Playlists = MyApplication.getBoxStore().boxFor(Playlist.class).getAll();
        String[] data = new String[Playlists.size()];
        for (int i = 0; i < Playlists.size(); i++) {
            data[i] = Playlists.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Playlist listSelected = Playlists.get(i);
                listSelected.add(MyApplication.getMusicListNow().get(position));
                MyApplication.getBoxStore().boxFor(Playlist.class).put(listSelected);
                dialog.dismiss();
                Snackbar.make(mLayout, "成功加入1首歌曲到播放列表", Snackbar.LENGTH_SHORT).show();
                //更新界面
                Intent intent = new Intent("list_changed");
                context.sendBroadcast(intent);
            }
        });
        return dialog;

    }

    private void showCreateListDialog(final Activity context, View view) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View createlist_dialog = inflater.inflate(R.layout.createlist, (ViewGroup) context.findViewById(R.id.create_list_dialog));
        final EditText name = (EditText) createlist_dialog.findViewById(R.id.list_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("新建歌曲列表");
        builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = name.getText().toString();
                if (!text.equals("")) {
                    if (MyApplication.getBoxStore().boxFor(Playlist.class).query().equal(Playlist_.name, text).build().findUnique() == null) {
                        if (MyApplication.getBoxStore().boxFor(Playlist.class).getAll().size() == 0) {
                            MyApplication.setCustomListNow(text);
                        }
                        MyApplication.getBoxStore().boxFor(Playlist.class).put(new Playlist(name.getText().toString()));
                        Snackbar.make(mLayout, "成功新建1个播放列表", Snackbar.LENGTH_SHORT).show();
                        //更新列表界面
                        Intent intent = new Intent("list_changed");
                        context.sendBroadcast(intent);
                    } else {
                        Toast.makeText(context, "该列表已存在", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "列表名不能为空", Toast.LENGTH_SHORT).show();
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
    }

    private void setAsRingtone(final Activity context, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            File music = new File(MyApplication.getMusicListNow().get(position).getMusicData()); // path is a file to /sdcard/media/ringtone
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, music.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, MyApplication.getMusicListNow().get(position).getMusicTitle());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.ARTIST, MyApplication.getMusicListNow().get(position).getMusicArtist());
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);
            //Insert it into the database
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getAbsolutePath());
            context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + music.getAbsolutePath() + "\"", null);
            Uri newUri = context.getContentResolver().insert(uri, values);
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            //Snackbar
            Snackbar.make(mLayout, "已成功设置为来电铃声", Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAsNext(Activity context, int position) {
//        List<musicInfo> list = MyApplication.getMusicInfoArrayList();
        MyApplication.getMusicListNow().add(MyApplication.getPositionNow(), MyApplication.getMusicListNow().get(position));
        com.sothree.slidinguppanel.SlidingUpPanelLayout main_layout = (com.sothree.slidinguppanel.SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);
        Snackbar.make(mLayout, "已成功设置为下一首播放", Snackbar.LENGTH_SHORT).show();
    }

    private void showMusicInfo(final Activity context, final int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View musicinfo_dialog = inflater.inflate(R.layout.musicinfo_dialog, (ViewGroup) context.findViewById(R.id.musicInfo_dialog));
        final EditText title = (EditText) musicinfo_dialog.findViewById(R.id.dialog_title);
        final EditText artist = (EditText) musicinfo_dialog.findViewById(R.id.dialog_artist);
        final EditText album = (EditText) musicinfo_dialog.findViewById(R.id.dialog_album);
        TextView duration = (TextView) musicinfo_dialog.findViewById(R.id.dialog_duration);
        TextView playtimes = (TextView) musicinfo_dialog.findViewById(R.id.dialog_playtimes);
        TextView path = (TextView) musicinfo_dialog.findViewById(R.id.dialog_path);
        final musicInfo nowMusic = MyApplication.getMusicListNow().get(position);
        title.setText(nowMusic.getMusicTitle());
        artist.setText(nowMusic.getMusicArtist());
        album.setText(nowMusic.getMusicAlbum());
        int totalSecond = nowMusic.getMusicDuration() / 1000;
        int minute = totalSecond / 60;
        int second = totalSecond - minute * 60;
        duration.setText(String.valueOf(minute) + "分" + String.valueOf(second) + "秒");
        playtimes.setText(String.valueOf(nowMusic.getTimes()));
        path.setText(nowMusic.getMusicData());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("歌曲信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nowMusic.setmTitle(title.getText().toString());
                nowMusic.setmArtist(artist.getText().toString());
                nowMusic.setmAlbum(album.getText().toString());
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(nowMusic);
                //更新界面
                ChangeScrollingUpPanel(MyApplication.getPositionNow());
                //通知其他adapter
                MyApplication.reDisplay(context);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setView(musicinfo_dialog);
        builder.show();
    }

    private boolean deleteFile(final Activity context, final int position) {
        final musicInfo musicinfo = MyApplication.getMusicListNow().get(position);
        final File file = new File(musicinfo.getMusicData());
        if (file.isFile() && file.exists()) {
            //警告窗口
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("请注意").setMessage("将从设备中彻底删除该歌曲文件，你确定吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    file.delete();
                    if (file.exists()) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("无外置SD卡读写权限").setMessage("因Android对外置SD卡的读写权限限制，文件删除失败");
                        alert.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                openSAF(context);
                            }
                        });
//                        openSAF.setNegativeButton("取消",new DialogInterface.OnClickListener(){
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
                        alert.show();
                    } else {
                        MyApplication.getBoxStore().boxFor(musicInfo.class).remove(musicinfo);
                        MyApplication.getMusicListNow().remove(position);
                        Snackbar.make(mLayout, "文件删除成功", Snackbar.LENGTH_SHORT).show();
                        //更新mediastore
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        //更新列表
                        playService.next();
                        //通知其他adapter
                        MyApplication.reDisplay(context);
                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
            return true;
        }
        return false;
    }

    private class matchTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String input = strings[0];
                if (!strings[1].equals("未知歌手")) {
                    input = input + strings[1];
                }
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder().add("music_input", input).add("music_filter", "name").add("music_type", "163").build();
                Request request = new Request.Builder().url("http://www.yove.net/yinyue/").addHeader("Origin", "http://www.yove.net").addHeader("X-Requested-With", "XMLHttpRequest").addHeader("Accept", "application/json, text/javascript, */*; q=0.01").post(requestBody).build();
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.getInt("code") == 200) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String data = jsonArray.toString();
                    Gson gson = new Gson();
                    List<Music> musicList = gson.fromJson(data, new TypeToken<List<Music>>() {
                    }.getType());
                    for (int i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getName().equals(strings[0])) {
                            return musicList.get(i).getMusicLargeAlbum();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "unKnown";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("unKnown") || s == null) {
                match_error++;
                if (hasNetwork(MainActivity.this)) {
                    next_match();
                } else {
                    Toast.makeText(MainActivity.this, "请检查您的网络连接", Toast.LENGTH_SHORT).show();
                }
            } else {
                download_diaplay_Image(s);
            }
            super.onPostExecute(s);
        }
    }

    public void download_diaplay_Image(final String url) {
        try {
            final Context context = getApplicationContext();
            FutureTarget<File> target = Glide.with(context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.default_album)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            match_album.setImageDrawable(resource);
                            musicInfo musicNow = musicInfoArrayList.get(match_position);
                            musicNow.setAlbumLink(url);
                            MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicNow);
                            new Handler().postDelayed(new Runnable() {//延迟执行
                                public void run() {
                                    next_match();
                                }
                            }, 200);

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
