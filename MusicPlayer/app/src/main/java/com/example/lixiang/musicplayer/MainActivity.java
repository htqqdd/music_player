package com.example.lixiang.musicplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.gyf.barlibrary.ImmersionBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.view.View.GONE;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.pauseAction;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.DRAGGING;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

@RuntimePermissions
public class MainActivity extends AestheticActivity {
    private static SeekBar seekBar;
    ;
    private ViewPager viewPager;
    private MsgReceiver msgReceiver;
    private boolean isfromSc = false;
    private int flag = 0;
    private int cx = 0;
    private int cy = 0;
    private int finalRadius = 0;
    private PlayService playService;
    private Timer mTimer;
    private SlidingUpPanelLayout.PanelState pannelState;
    private NavigationView navigationView;
    private FloatingActionButton random_play;
    private SlidingUpPanelLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("OnCreate执行", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Aesthetic.isFirstTime()) {
            Aesthetic.get()
                    .colorPrimaryRes(R.color.colorPrimary)
                    .colorAccentRes(R.color.colorAccent)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .apply();
        }

        //沉浸状态栏
        ImmersionBar.with(MainActivity.this).statusBarView(R.id.immersion_view).init();


        //初始化全局变量
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        random_play = (FloatingActionButton) findViewById(R.id.random_play);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        //多屏幕尺寸适应
        new screenAdaptionTask().execute();

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
        View view1 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        TextView textView = (TextView) view1.findViewById(R.id.text);
        textView.setText("建议");
        tabLayout.getTabAt(0).setCustomView(view1);
        View view2 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        TextView textView2 = (TextView) view2.findViewById(R.id.text);
        textView2.setText("歌曲");
        tabLayout.getTabAt(1).setCustomView(view2);
        View view3 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        TextView textView3 = (TextView) view3.findViewById(R.id.text);
        textView3.setText("下载");
        tabLayout.getTabAt(2).setCustomView(view3);


        //新标题栏
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        main_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //侧滑栏动画
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, main_toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
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
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
//        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                CardView main_control_ui = (CardView) findViewById(R.id.main_control_ui);
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
                    pannelState = EXPANDED;
                    //禁止手势滑动
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                if (previousState == DRAGGING && newState == COLLAPSED) {
                    mTimer.cancel();
                    pannelState = COLLAPSED;
                    //恢复手势滑动
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
        });

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                if (mLayout != null &&
                        (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    mLayout.setPanelState(COLLAPSED);
                }
            }
        });
        ImageView about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pannelState == EXPANDED) {
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
            }
        });


        if (Data.getState() == playing) {
            ChangeScrollingUpPanel(Data.getPosition());
            random_play.hide();
            mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelHeight((int) (60 * getResources().getDisplayMetrics().density + 0.5f));
        }
        MainActivityPermissionsDispatcher.needsPermissionWithCheck(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem sleeper = menu.findItem(R.id.sleeper);
        MenuItem equalizer = menu.findItem(R.id.equalizer);
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


        return true;
    }


    @Override
    protected void onStart() {
        Log.e("OnStart执行", "OnStart");
        super.onStart();

        ensureServiceStarted();

        //绑定服务
        Intent bindIntent = new Intent(this, PlayService.class);
        bindService(bindIntent, conn, BIND_AUTO_CREATE);

        //拖动seekbar
        seekBar.setPadding(0, 0, 0, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playService.seekto(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //设置起始页
        if (isfromSc == false) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String start_page = sharedPref.getString("start_page", "");
            switch (start_page) {
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

//        final FloatingActionButton random_play = (FloatingActionButton) findViewById(R.id.random_play);
        random_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.setPlayMode(1);
                Data.setRecent(false);
                Data.setFavourite(false);
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", playAction);
                sendBroadcast(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
//        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
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
        if (Data.getState() == pausing) {
            unbindService(conn);
            stopService(new Intent(this, PlayService.class));
        }
        super.onDestroy();
    }
    //以下为公共方法

    public void sendPermissionGranted() {
        Intent intent = new Intent("permission_granted");
        sendBroadcast(intent);
        Intent intent2 = new Intent("list_permission_granted");
        sendBroadcast(intent2);
        Log.v("发送初始广播", "发送2");
    }

    //播放，暂停按钮
    public void title_play_or_pause(View view) {
        if (pannelState == COLLAPSED) {
            if (Data.getState() == playing) {
                playService.pause();
                playService.removeAudioFocus();
            } else if (Data.getState() == pausing) {
                playService.resume();
            }
        }
    }

    public void main_play_or_pause(View v) {
        if (Data.getState() == playing) {
            playService.pause();
            playService.removeAudioFocus();
        } else if (Data.getState() == pausing) {
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

        switch (Data.getPlayMode()) {
            case 3:
                Data.setPlayMode(0);
                Toasty.info(MainActivity.this, "列表循环播放", Toast.LENGTH_SHORT, true).show();
                repeat_button.setImageResource(R.drawable.repeat);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 0:
                Data.setPlayMode(2);
                Toasty.info(MainActivity.this, "单曲循环播放", Toast.LENGTH_SHORT, true).show();
                repeat_button.setImageResource(R.drawable.repeat_one);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 2:
                Data.setPlayMode(3);
                Toasty.info(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT, true).show();
                repeat_button.setImageResource(R.drawable.repeat_grey);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 1:
                Data.setPlayMode(0);
                Toasty.info(MainActivity.this, "列表重复播放", Toast.LENGTH_SHORT, true).show();
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                repeat_button.setImageResource(R.drawable.repeat);
                break;
            default:
        }
    }

    public void changeShuffle(View v) {
        ImageView repeat_button = (ImageView) findViewById(R.id.repeat_button);
        ImageView shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        if (Data.getPlayMode() == 1) {
            Data.setPlayMode(3);
            Toasty.info(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT, true).show();
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        } else {
            Data.setPlayMode(1);
            Toasty.info(MainActivity.this, "随机播放", Toast.LENGTH_SHORT, true).show();
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        }
    }

    public void animation_change_color(int Int) {
        ImageView play_now_back_color = (ImageView) findViewById(R.id.play_now_back_color);
        if (cx == 0) {
            FloatingActionButton play_or_pause = (FloatingActionButton) findViewById(R.id.play_or_pause);
            RelativeLayout main_control_layout = (RelativeLayout) findViewById(R.id.main_control_layout);
            RelativeLayout control_layout = (RelativeLayout) findViewById(R.id.control_layout);
            cx = play_or_pause.getLeft() + main_control_layout.getLeft() + play_or_pause.getWidth() / 2;
            cy = control_layout.getTop() - seekBar.getTop() + play_or_pause.getTop() + play_or_pause.getHeight() / 2;
            finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());
        }
        final int Int1 = Int;
        final RelativeLayout activity_now_play = (RelativeLayout) findViewById(R.id.activity_now_play);
        if (cx != 0) {
            Animator anim = ViewAnimationUtils.createCircularReveal(play_now_back_color, cx, cy, 0, finalRadius);
            play_now_back_color.setBackgroundColor(Int);
            anim.setDuration(500);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    activity_now_play.setBackgroundColor(Int1);
                }
            });
        }
        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        now_on_play_text.setTextColor(Int);
    }

    public void ChangeScrollingUpPanel(int position) {
        String path = Data.getData(position);
        String title = Data.getTitle(position);
        int album_id = Data.getAlbumId(position);
        String artist = Data.getArtist(position);
        int id = Data.getId(position);
        seekBar.setProgress(0);
        seekBar.setMax(Data.getDuration(position));
        TextView main_song_title = (TextView) findViewById(R.id.main_song_title);
        ImageView play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
        ImageView repeat_button = (ImageView) findViewById(R.id.repeat_button);
        ImageView shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
        main_song_title.setText(Data.getTitle(position));
//设置播放模式按钮
        //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
        if (Data.getPlayMode() == 0) {
            repeat_button.setImageResource(R.drawable.repeat);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }
        if (Data.getPlayMode() == 1) {
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        }
        if (Data.getPlayMode() == 2) {
            repeat_button.setImageResource(R.drawable.repeat_one);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }
        if (Data.getPlayMode() == 3) {
            repeat_button.setImageResource(R.drawable.repeat_grey);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }

        //设置封面,自动封面获取颜色
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(Data.getPosition()));
        ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);
        Glide.with(this).load(uri).placeholder(R.drawable.default_album).error(R.drawable.default_album).crossFade(500).into(play_now_cover);
        setBackColor();

        //设置歌曲名，歌手
        TextView play_now_song = (TextView) findViewById(R.id.play_now_song);
        TextView play_now_singer = (TextView) findViewById(R.id.play_now_singer);
        play_now_song.setText(title);
        play_now_singer.setText(artist);

        //设置播放按钮
        if (Data.getState() == playing) {
            floatingActionButton.setImageResource(R.drawable.pause_black);
            play_pause_button.setImageResource(R.drawable.pause_black);
        } else if (Data.getState() == pausing) {
            floatingActionButton.setImageResource(R.drawable.play_black);
            play_pause_button.setImageResource(R.drawable.play_black);
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

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
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
            if (intent.getIntExtra("UIChange", 0) == initialize) {
                FloatingActionButton random_play = (FloatingActionButton) findViewById(R.id.random_play);
                random_play.hide();
                SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                mLayout.setPanelHeight((int) (60 * getResources().getDisplayMetrics().density + 0.5f));
            }

            if (intent.getIntExtra("UIChange", 0) == pauseAction) {
                ImageView play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
                play_pause_button.setImageResource(R.drawable.play_black);
                floatingActionButton.setImageResource(R.drawable.play_black);
            }
            if (intent.getIntExtra("UIChange", 0) == playAction) {
                ImageView play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
                play_pause_button.setImageResource(R.drawable.pause_black);
                floatingActionButton.setImageResource(R.drawable.pause_black);
            }
            if (intent.getIntExtra("UIChange", 0) == mediaChangeAction) {
                ChangeScrollingUpPanel(Data.getPosition());
            }
            if (intent.getIntExtra("onDestroy", 0) == 1) {
                finish();
            }
            if (intent.getIntExtra("viewPagerChange", -1) != -1) {
                isfromSc = true;
                viewPager.setCurrentItem(intent.getIntExtra("viewPagerChange", -1));
            }
        }

    }

    public void play_now_menu_button(View v) {
        menu_util.popupMenu(this, v, Data.getPosition());
    }

    private void ensureServiceStarted() {
        if (Data.getServiceState() == false) {
            Intent intent = new Intent(this, PlayService.class);
            intent.putExtra("ACTION", initialize);
            startService(intent);

        }
    }


    private void setBackColor() {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(Data.getPosition()));
        Glide.with(MainActivity.this).load(uri).asBitmap().error(R.drawable.default_album).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                BitmapDrawable errorBitmapDrawable = (BitmapDrawable) errorDrawable;
                Bitmap resource = errorBitmapDrawable.getBitmap();
                Palette p = Palette.from(resource).generate();
                Palette.Swatch s1 = p.getVibrantSwatch();
                Palette.Swatch s2 = p.getDarkVibrantSwatch();
                Palette.Swatch s3 = p.getLightVibrantSwatch();
                Palette.Swatch s4 = p.getMutedSwatch();
                Palette.Swatch s5 = p.getLightVibrantSwatch();
                Palette.Swatch s6 = p.getDarkVibrantSwatch();
                Palette.Swatch s7 = p.getDominantSwatch();
                int color = 0;
                if (s1 != null) {
                    color = s1.getRgb();
                } else if (s4 != null) {
                    color = s4.getRgb();
                } else if (s2 != null) {
                    color = s2.getRgb();
                } else if (s3 != null) {
                    color = s3.getRgb();
                } else if (s5 != null) {
                    color = s5.getRgb();
                } else if (s6 != null) {
                    color = s6.getRgb();
                } else if (s7 != null) {
                    color = s7.getRgb();
                } else {
                    color = getResources().getColor(R.color.colorPrimary);
                }
                animation_change_color(color);
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Palette p = Palette.from(resource).generate();
                Palette.Swatch s1 = p.getVibrantSwatch();
                Palette.Swatch s2 = p.getDarkVibrantSwatch();
                Palette.Swatch s3 = p.getLightVibrantSwatch();
                Palette.Swatch s4 = p.getMutedSwatch();
                Palette.Swatch s5 = p.getLightVibrantSwatch();
                Palette.Swatch s6 = p.getDarkVibrantSwatch();
                Palette.Swatch s7 = p.getDominantSwatch();
                int color = 0;
                if (s1 != null) {
                    color = s1.getRgb();
                } else if (s4 != null) {
                    color = s4.getRgb();
                } else if (s2 != null) {
                    color = s2.getRgb();
                } else if (s3 != null) {
                    color = s3.getRgb();
                } else if (s5 != null) {
                    color = s5.getRgb();
                } else if (s6 != null) {
                    color = s6.getRgb();
                } else if (s7 != null) {
                    color = s7.getRgb();
                } else {
                    color = getResources().getColor(R.color.colorPrimary);
                }
                animation_change_color(color);
            }
        });
    }


    private class screenAdaptionTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            return (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);
            RelativeLayout.LayoutParams lp_play_now_cover = (RelativeLayout.LayoutParams) play_now_cover.getLayoutParams();
            lp_play_now_cover.height = (int) o;
            play_now_cover.setLayoutParams(lp_play_now_cover);
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
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (playService != null) {
                    if (Data.getState() == playing) {
                        seekBar.setProgress(playService.getCurrentPosition());
                    }
                }
            }
        };
        mTimer.schedule(task, 0, 1000);
    }

    private void sleeper(){
        final java.util.Calendar c = java.util.Calendar.getInstance();
        final int hourNow = c.get(java.util.Calendar.HOUR_OF_DAY);
        final int minuteNow = c.get(Calendar.MINUTE);
        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourPicked, int minutePicked) {
                int duration = (hourPicked - hourNow) * 60 + minutePicked - minuteNow;
                if (hourPicked >= hourNow && duration > 0 && duration < 360) {
                    playService.deleteService(duration);
                    Toasty.success(MainActivity.this, "已经定时为" + duration + "分钟后关闭", Toast.LENGTH_SHORT, true).show();
                } else {
                    Toasty.error(MainActivity.this, "所选时间须为当天，且距当前时间6小时内", Toast.LENGTH_SHORT, true).show();
                }
            }
        }, hourNow, minuteNow, true).show();
    }

    private void equalizer(){
        SharedPreferences bundle = MainActivity.this.getSharedPreferences("first_audioEffect", MainActivity.this.MODE_PRIVATE);
        if (bundle.getBoolean("isFirst", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("兼容性提醒");
            builder.setMessage("使用内置均衡器前，请确保未使用其他音效软件，否则可能因兼容性问题导致导致导致应用崩溃。\n\n若您仍想使用内置均衡器，请先禁用手机内其他音效软件。");
            builder.setPositiveButton("我已了解，不再提醒", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                    startActivity(intent);
                    SharedPreferences.Editor editor = getSharedPreferences("first_audioEffect", MODE_PRIVATE).edit();
                    editor.putBoolean("isFirst", false);
                    editor.apply();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        } else {
            Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
            startActivity(intent);
        }
    }

}
