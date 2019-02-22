package com.lixiangsoft.lixiang.music_player;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.NoiseSuppressor;
import android.media.audiofx.Virtualizer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TableRow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lixiangsoft.lixiang.music_player.EventBusUtil.ServiceEvent;
import com.lixiangsoft.lixiang.music_player.EventBusUtil.UIChangeEvent;
import com.lixiangsoft.lixiang.music_player.EventBusUtil.dismissEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.CATEGORY_TRANSPORT;
import static android.app.Notification.PRIORITY_MIN;
import static android.media.AudioManager.STREAM_MUSIC;

public class PlayService extends Service implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private String path;
    private MediaPlayer mediaPlayer;
        private ServiceReceiver serviceReceiver;
    private boolean onetime = true;
    private static final int NOTIFICATION_ID = 101;
    private AudioManager audioManager;

    private BassBoost mBass;
    private Equalizer mEqualizer;
    private AcousticEchoCanceler canceler;
    private AutomaticGainControl control;
    private NoiseSuppressor suppressor;
    private LoudnessEnhancer loudnessEnhancer;
    private Virtualizer mVirtualizer;
    private int audioSessionId = 0;
    //    private int positionNow = 0;
    private String titleNow;
    private String singerNow;
    private Bitmap albumNow;
    private Notification notification;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;

    private PendingIntent pendingIntent(int action) {
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", action);
        return PendingIntent.getBroadcast(this, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PlayService() {
    }

    public class musicBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("测试", "Service Create");
        BitmapDrawable resourceDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.default_album);
        albumNow = resourceDrawable.getBitmap();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(STREAM_MUSIC);
        audioSessionId = mediaPlayer.getAudioSessionId();
        initialAudioEffect(audioSessionId);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        if (mediaSessionManager == null) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaSession = new MediaSession(getApplicationContext(), "MusicPlayer");
            mediaSession.setActive(true);
            mediaSession.setCallback(new MediaSession.Callback() {

            });
        }

        MyApplication.setServiceStarted(true);

        //监听耳机状态广播
        IntentFilter head_set_intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, head_set_intentFilter);

//        //耳机线控
//        IntentFilter mediafilter = new IntentFilter();
//        //拦截按键KeyEvent.KEYCODE_MEDIA_NEXT、KeyEvent.KEYCODE_MEDIA_PREVIOUS、KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//        mediafilter.addAction(Intent.ACTION_MEDIA_BUTTON);
//        mediafilter.setPriority(0);//设置优先级，优先级太低可能被拦截，收不到信息。一般默认优先级为0，通话优先级为1，该优先级的值域是-1000到1000。
//        registerReceiver(mediaButtonReceiver, mediafilter);

        EventBus.getDefault().register(this);
        //动态注册广播此广播用于接收Notification的PenddingIntent
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("service_broadcast");
        registerReceiver(serviceReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("测试", "Service Start");
//        if (intent != null) {
            if (intent.getIntExtra("ACTION", -2) == MyConstant.initialize) {
                MyApplication.setPositionNow(0);
            }
        if (intent.getIntExtra("ACTION", -2) == MyConstant.sc_playAction) {
            if (MyApplication.hasInitialized() == false) {
                MyApplication.initialMusicInfo(this);
            }
            MyApplication.setPlayMode(MyConstant.random);
            play(randomPosition());
        }
        MyApplication.setServiceStarted(true);
//        } else {
//            MyApplication.setServiceStarted(false);
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(ServiceEvent event) {
        switch (event.action) {
            case MyConstant.playAction:
                play(MyApplication.getPositionNow());
                return;
            case MyConstant.random_playAction:
                play(randomPosition());
                return;
            case MyConstant.previousAction:
                previous();
                return;
            case MyConstant.nextAction:
                next();
                return;
            case MyConstant.pauseAction:
                pause(true);
                return;
            case MyConstant.resumeAction:
                resume();
                return;
            case MyConstant.deleteAction:
                deleteService(event.delay);
                return;
            case MyConstant.resetAction:
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                }
                return;
        }

    }

    ;

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new musicBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v("AUDIOFOCUS_GAIN", "焦点获得");
                //The service gained audio focus, so it needs to start playing.
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(STREAM_MUSIC);
                        mediaPlayer.setOnCompletionListener(this);
                        mediaPlayer.setOnPreparedListener(this);
                        mediaPlayer.setOnErrorListener(this);
                        audioSessionId = mediaPlayer.getAudioSessionId();
                        initialAudioEffect(audioSessionId);
                    } else if (!mediaPlayer.isPlaying()) resume();
                    mediaPlayer.setVolume(1.0f, 1.0f);

                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //The service lost audio focus, the user probably moved to playing media on another app, so release the media player.
                Log.v("AUDIOFOCUS_LOSS", "焦点丢失");
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pause(false);
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v("FOCUS_LOSS_TRANSIENT", "焦点暂时丢失");
                //Focus lost for a short time, pause the MediaPlayer.
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pause(false);
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, probably a notification arrived on the device, lower the playback volume.
                Log.v("AUDIOFOCUS_LOSS_CAN", "焦点更短暂丢失");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Boolean lost_focus = sharedPref.getBoolean("lost_focus", false);
                if (lost_focus) {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                    }
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //关闭加载框
        EventBus.getDefault().post(new dismissEvent());
        if (onetime) {
            EventBus.getDefault().post(new UIChangeEvent(MyConstant.initialize));
            onetime = false;
        }
        mp.start();
        MyApplication.setMediaDuration(mp.getDuration());
        MyApplication.setState(MyConstant.playing);
        EventBus.getDefault().post(new UIChangeEvent(MyConstant.mediaChangeAction));
        updateMetaDataAndBuildNotification();
        //储存播放次数
        new savePlayTimesTask().execute();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    public void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    public int getAudioSessionId() {
        return audioSessionId;
    }

    public void play(final int position) {
        if (requestAudioFocus()) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnErrorListener(this);
                audioSessionId = mediaPlayer.getAudioSessionId();
                initialAudioEffect(audioSessionId);
            }
            mediaPlayer.reset();
            try {
                path = MyApplication.getMusicListNow().get(position).getMusicData();
                MyApplication.setPositionNow(position);
                if (path == null || path.equals("")) {
                    //关闭加载框
                    EventBus.getDefault().post(new dismissEvent());
                    Toast.makeText(this, "未获取到此歌曲的链接，请尝试更换资源提供方", Toast.LENGTH_SHORT).show();
                } else if (path.substring(path.lastIndexOf("."), path.length()).equals(".ape")) {
                    Toast.makeText(this, "不支持此格式的歌曲", Toast.LENGTH_SHORT).show();
                    next();
                } else {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepareAsync(); // 进行缓冲
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void seekto(int currentPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(currentPosition);
        }
    }

    //生成随机数[0-n)
    public int randomPosition() {
        Random random = new Random();
        int totalNumber = MyApplication.getMusicListNow().size();
        if (totalNumber > 1) {
            int random_position = random.nextInt(totalNumber - 1);
            return random_position;
        } else {
            int r = (int) (2 * Math.random());
            return r;
        }
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("Control", 0) == MyConstant.previousAction) {
                previous();
            } else if (intent.getIntExtra("Control", 0) == MyConstant.nextAction) {
                next();
            } else if (intent.getIntExtra("Control", 0) == MyConstant.pauseAction) {
                pause(true);
            } else if (intent.getIntExtra("Control", 0) == MyConstant.resumeAction) {
                resume();
            } else if (intent.getIntExtra("Control", 0) == MyConstant.deleteAction) {
                deleteService(0);
            }
//            else if (intent.getIntExtra("Control", 0) == MyConstant.resetAction) {
//                if (mediaPlayer != null) {
//                    mediaPlayer.reset();
//                }
//            }
        }
    }

    public void deleteService(int time) {
        if (time != 0) {
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // 设置定时
//            int duration = time * 60 * 1000;
            long setTime = time + SystemClock.elapsedRealtime();
            //设置定时任务
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, setTime, pendingIntent(MyConstant.deleteAction));
        } else {
            pause(true);
            removeNotification();
            EventBus.getDefault().post(new UIChangeEvent(MyConstant.DestoryAction));
        }
    }

    public void previous() {
        int playMode = MyApplication.getPlayMode();
        int position = MyApplication.getPositionNow();
        int size = MyApplication.getMusicListNow().size();
        if (playMode == MyConstant.list_repeat) {//0:列表重复
            if (position <= 0) {
                play(size - 1);
            } else {
                play(position - 1);
            }
        } else if (playMode == MyConstant.random) {// 1:随机
            play(randomPosition());
        } else if (playMode == MyConstant.one_repeat) {//2:单曲重复
            play(position);
        } else {//3:顺序
            if (position == 0) {
                Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
                return;
            } else {
                play(position - 1);
            }
        }

    }

    public void next() {
        int playMode = MyApplication.getPlayMode();
        int position = MyApplication.getPositionNow();
        int size = MyApplication.getMusicListNow().size();
        if (playMode == MyConstant.list_repeat) {
            if (position < size - 1) {
                play(position + 1);
            } else {
                play(0);
            }
        } else if (playMode == MyConstant.random) {//1:随机
            play(randomPosition());
        } else if (playMode == MyConstant.one_repeat) {//2:单曲重复
            play(position);
        } else if (playMode == MyConstant.list) {//3:顺序
            if (position >= size - 1) {
                Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
                return;
            } else {
                play(position + 1);
            }
        }
    }

    public void pause(boolean removeAudioFocus) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            EventBus.getDefault().post(new UIChangeEvent(MyConstant.pauseAction));
            if (MyApplication.getState() == MyConstant.playing) {
                MyApplication.setState(MyConstant.pausing);
                buildNotification(MyConstant.pausing);
            }
        }
        if (removeAudioFocus) {
            removeAudioFocus();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            requestAudioFocus();
            mediaPlayer.start();
        } else {
            play(MyApplication.getPositionNow());
        }
        EventBus.getDefault().post(new UIChangeEvent(MyConstant.playAction));
        MyApplication.setState(MyConstant.playing);
        buildNotification(MyConstant.playing);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(serviceReceiver);
        unregisterReceiver(becomingNoisyReceiver);
//        unregisterReceiver(mediaButtonReceiver);
        MyApplication.setServiceStarted(false);
        releaseMedia();
        removeNotification();
        removeAudioFocus();
        try{
            mEqualizer.release();
            mVirtualizer.release();
            canceler.release();
            control.release();
            suppressor.release();
            mBass.release();
            loudnessEnhancer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private boolean requestAudioFocus() {
        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.v("焦点获得", "获得2");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Could not gain focus
        return false;
    }

    public boolean removeAudioFocus() {
        try {
            if (audioManager != null) {
                return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public class savePlayTimesTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            musicInfo musicNow = MyApplication.getMusicListNow().get(MyApplication.getPositionNow());
            if (musicNow.getMusicLink().equals("")) {
                musicNow.setTimes(musicNow.getTimes() + 1);
                MyApplication.getBoxStore().boxFor(musicInfo.class).put(musicNow);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

    RequestListener listener = new RequestListener() {
        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            BitmapDrawable resourceDrawable = (BitmapDrawable) getDrawable(R.drawable.default_album);
            Bitmap resource = resourceDrawable.getBitmap();
            albumNow = resource;
            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumNow)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, singerNow)
                    .putString(MediaMetadata.METADATA_KEY_TITLE, titleNow)
                    .build());
            PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
            stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f);
            mediaSession.setPlaybackState(stateBuilder.build());
            buildNotification(MyConstant.playing);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            albumNow = (Bitmap) resource;
            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumNow)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, singerNow)
                    .putString(MediaMetadata.METADATA_KEY_TITLE, titleNow)
                    .build());
            PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
            stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f);
            mediaSession.setPlaybackState(stateBuilder.build());
            buildNotification(MyConstant.playing);
            return false;
        }
    };

    private void updateMetaDataAndBuildNotification() {
        musicInfo musicNow = MyApplication.getMusicListNow().get(MyApplication.getPositionNow());
        singerNow = musicNow.getMusicArtist();
        titleNow = musicNow.getMusicTitle();
        if (!musicNow.getMusicLink().equals("")) {//网络
            Glide.with(this).load(musicNow.getMusicMediumAlbum()).asBitmap().listener(listener).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        } else {//本地
            if (musicNow.getAlbumLink() != null) {
                Glide.with(this)
                        .load(musicNow.getAlbumLink())
                        .asBitmap()
                        .placeholder(R.drawable.default_album)
                        .listener(listener)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            } else {
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, musicNow.getMusicAlbumId());
                Glide.with(this).load(uri).asBitmap().listener(listener).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }
        }
    }


    private void buildNotification(String playState) {
        int notificationAction = R.drawable.ic_pause_black_24dp;//needs to be initialized
        PendingIntent play_pauseIntent = pendingIntent(MyConstant.pauseAction);
        if (playState == MyConstant.pausing) {
            notificationAction = R.drawable.ic_play_arrow_black_24dp;
            play_pauseIntent = pendingIntent(MyConstant.resumeAction);
        }
        Intent startMain = new Intent(PlayService.this, MainActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(PlayService.this, 0, startMain, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.e("服务", "Ongoing" + "MyApplication.getState()" + MyApplication.getState());
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "MPlayer";
            CharSequence name = "MPlayer";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
//            mChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(PlayService.this, CHANNEL_ID)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(albumNow)
                    .setSmallIcon(R.drawable.ic_album_black_24dp)
                    .setDeleteIntent(pendingIntent(MyConstant.deleteAction))
                    .setColor(ColorUtil.getColor(albumNow))
                    .setContentIntent(startMainActivity)
                    .setOngoing(MyApplication.getState().equals(MyConstant.playing))//该选项会导致手表通知不显示
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_black_24dp, "SkiptoPrevious", pendingIntent(MyConstant.previousAction)) // #0
                    .addAction(notificationAction, "Play or Pause", play_pauseIntent)  // #1
                    .addAction(R.drawable.ic_skip_next_black_24dp, "SkiptoNext", pendingIntent(MyConstant.nextAction))     // #2
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSession.getSessionToken())
                    )
                    .setContentTitle(titleNow)
                    .setContentText(singerNow)
                    .build();

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            notification = new Notification.Builder(PlayService.this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(albumNow)
                    .setSmallIcon(R.drawable.ic_album_black_24dp)
                    .setDeleteIntent(pendingIntent(MyConstant.deleteAction))
                    .setColor(ColorUtil.getColor(albumNow))
                    .setContentIntent(startMainActivity)
                    .setOngoing(MyApplication.getState().equals(MyConstant.playing))//该选项会导致手表通知不显示
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_black_24dp, "SkiptoPrevious", pendingIntent(MyConstant.previousAction)) // #0
                    .addAction(notificationAction, "Play or Pause", play_pauseIntent)  // #1
                    .addAction(R.drawable.ic_skip_next_black_24dp, "SkiptoNext", pendingIntent(MyConstant.nextAction))     // #2
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSession.getSessionToken())
                    )
                    .setContentTitle(titleNow)
                    .setContentText(singerNow)
                    .build();

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }


    //以下为音效部分
    private void initialAudioEffect(final int audioSessionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
                    mBass = new BassBoost(0, audioSessionId);
                    mVirtualizer = new Virtualizer(0, audioSessionId);
                    mEqualizer = new Equalizer(0, audioSessionId);
                    canceler = AcousticEchoCanceler.create(audioSessionId);
                    control = AutomaticGainControl.create(audioSessionId);
                    suppressor = NoiseSuppressor.create(audioSessionId);
                    getPreference();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public void setEqualizer(boolean b) {
        try {
            mEqualizer.setEnabled(b);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setEqualizerBandLevel(Short band, Short level) {
        try {
            mEqualizer.setBandLevel(band, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCanceler(boolean b) {
        try {
            canceler.setEnabled(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setControl(boolean b) {
        try {
            control.setEnabled(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSuppressor(boolean b) {
        try {
            suppressor.setEnabled(b);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setBass(boolean b) {
        try {
            mBass.setEnabled(b);
            Log.v("开启", "bass" + mBass.getEnabled());
            Log.v("开启", "增强" + loudnessEnhancer.getEnabled());
            loudnessEnhancer.setEnabled(mBass.getEnabled() || mVirtualizer.getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBassStrength(Short strength) {
        try {
            mBass.setStrength(strength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVirtualizer(Boolean b) {
        try {
            mVirtualizer.setEnabled(b);
            Log.v("开启", "virtualizer" + mVirtualizer.getEnabled());
            loudnessEnhancer.setEnabled(mVirtualizer.getEnabled() || mBass.getEnabled());
            Log.v("开启", "增强" + loudnessEnhancer.getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVirtualizerStrength(Short s) {
        try {
            mVirtualizer.setStrength(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPreference() {
        SharedPreferences bundle = this.getSharedPreferences("audioEffect", MODE_PRIVATE);
        Log.v("开启", "getPreference");
        try {
            //超强音量
            if ((bundle.getBoolean("Bass", false) || bundle.getBoolean("Virtualizer", false)) == true) {
                loudnessEnhancer.setEnabled(true);
                Log.v("开启", "loudness");
                loudnessEnhancer.setTargetGain(500);

            } else {
                loudnessEnhancer.setEnabled(false);
            }
            //虚拟环绕
            if (bundle.getBoolean("Virtualizer", false) == false) {
                setVirtualizer(false);
            } else {
                setVirtualizer(true);
                setVirtualizerStrength((short) bundle.getInt("Virtualizer_seekBar", 0));
                Log.v("强度", "Virtualizer" + bundle.getInt("Virtualizer_seekBar", 0));
            }
            //低音增强
            Log.v("开启", String.valueOf(bundle.getBoolean("Bass", false)));
            if (bundle.getBoolean("Bass", false) == false) {
                setBass(false);
            } else {
                setBass(true);
                Log.v("开启", "Bass" + bundle.getBoolean("Bass", false));
                setBassStrength((short) bundle.getInt("Bass_seekBar", 0));
                Log.v("强度", "Bass" + bundle.getInt("Bass_seekBar", 0));
            }
            //均衡器
            if (bundle.getBoolean("Equalizer", false) == true) {
                mEqualizer.setEnabled(true);
                Log.v("开启", "Equalzier");
                switch (bundle.getInt("Spinner", 0)) {
                    case 0:
                        mEqualizer.setBandLevel((short) 0, (short) 300);
                        mEqualizer.setBandLevel((short) 1, (short) 0);
                        mEqualizer.setBandLevel((short) 2, (short) 0);
                        mEqualizer.setBandLevel((short) 3, (short) 0);
                        mEqualizer.setBandLevel((short) 4, (short) 300);
                        return;
                    case 1:
                        mEqualizer.setBandLevel((short) 0, (short) 500);
                        mEqualizer.setBandLevel((short) 1, (short) 300);
                        mEqualizer.setBandLevel((short) 2, (short) -200);
                        mEqualizer.setBandLevel((short) 3, (short) 400);
                        mEqualizer.setBandLevel((short) 4, (short) 400);
                        return;
                    case 2:
                        mEqualizer.setBandLevel((short) 0, (short) 600);
                        mEqualizer.setBandLevel((short) 1, (short) 0);
                        mEqualizer.setBandLevel((short) 2, (short) 200);
                        mEqualizer.setBandLevel((short) 3, (short) 400);
                        mEqualizer.setBandLevel((short) 4, (short) 100);
                        return;
                    case 3:
                        mEqualizer.setBandLevel((short) 0, (short) 0);
                        mEqualizer.setBandLevel((short) 1, (short) 0);
                        mEqualizer.setBandLevel((short) 2, (short) 0);
                        mEqualizer.setBandLevel((short) 3, (short) 0);
                        mEqualizer.setBandLevel((short) 4, (short) 0);
                        return;
                    case 4:
                        mEqualizer.setBandLevel((short) 0, (short) 300);
                        mEqualizer.setBandLevel((short) 1, (short) 0);
                        mEqualizer.setBandLevel((short) 2, (short) 0);
                        mEqualizer.setBandLevel((short) 3, (short) 200);
                        mEqualizer.setBandLevel((short) 4, (short) -100);
                        return;
                    case 5:
                        mEqualizer.setBandLevel((short) 0, (short) 400);
                        mEqualizer.setBandLevel((short) 1, (short) 100);
                        mEqualizer.setBandLevel((short) 2, (short) 700);
                        mEqualizer.setBandLevel((short) 3, (short) 100);
                        mEqualizer.setBandLevel((short) 4, (short) 0);
                        return;
                    case 6:
                        mEqualizer.setBandLevel((short) 0, (short) 500);
                        mEqualizer.setBandLevel((short) 1, (short) 300);
                        mEqualizer.setBandLevel((short) 2, (short) 0);
                        mEqualizer.setBandLevel((short) 3, (short) 100);
                        mEqualizer.setBandLevel((short) 4, (short) 300);
                        return;
                    case 7:
                        mEqualizer.setBandLevel((short) 0, (short) 400);
                        mEqualizer.setBandLevel((short) 1, (short) 200);
                        mEqualizer.setBandLevel((short) 2, (short) -200);
                        mEqualizer.setBandLevel((short) 3, (short) 200);
                        mEqualizer.setBandLevel((short) 4, (short) 500);
                        return;
                    case 8:
                        mEqualizer.setBandLevel((short) 0, (short) -100);
                        mEqualizer.setBandLevel((short) 1, (short) 200);
                        mEqualizer.setBandLevel((short) 2, (short) 500);
                        mEqualizer.setBandLevel((short) 3, (short) 100);
                        mEqualizer.setBandLevel((short) 4, (short) -200);
                        return;
                    case 9:
                        mEqualizer.setBandLevel((short) 0, (short) 500);
                        mEqualizer.setBandLevel((short) 1, (short) 300);
                        mEqualizer.setBandLevel((short) 2, (short) -100);
                        mEqualizer.setBandLevel((short) 3, (short) 300);
                        mEqualizer.setBandLevel((short) 4, (short) 500);
                        return;
                    case 10:
                        mEqualizer.setBandLevel((short) 0, (short) 0);
                        mEqualizer.setBandLevel((short) 1, (short) 800);
                        mEqualizer.setBandLevel((short) 2, (short) 400);
                        mEqualizer.setBandLevel((short) 3, (short) 100);
                        mEqualizer.setBandLevel((short) 4, (short) 1000);
                        return;
                    case 11:
                        mEqualizer.setBandLevel((short) 0, (short) -170);
                        mEqualizer.setBandLevel((short) 1, (short) 270);
                        mEqualizer.setBandLevel((short) 2, (short) 50);
                        mEqualizer.setBandLevel((short) 3, (short) -220);
                        mEqualizer.setBandLevel((short) 4, (short) 200);
                        return;
                    default:
                }
            } else {
                mEqualizer.setEnabled(false);
            }
            //次要
            if (AcousticEchoCanceler.isAvailable()) {
                canceler.setEnabled(bundle.getBoolean("Canceler", false));
            }
            if (AutomaticGainControl.isAvailable()) {
                control.setEnabled(bundle.getBoolean("AutoGain", false));
            }
            if (NoiseSuppressor.isAvailable()) {
                suppressor.setEnabled(bundle.getBoolean("Suppressor", false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            headset_unplug_handler.sendEmptyMessage(0);
        }
    };

    private Handler headset_unplug_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PlayService.this);
            if (sharedPref.getBoolean("headset_unplug", false) == false) {
                pause(true);
            }
            super.handleMessage(msg);
        }
    };


//    private BroadcastReceiver mediaButtonReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction());//判断是不是耳机按键事件。
////            if (!isActionMediaButton) return;
//            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);  //判断有没有耳机按键事件
////            if (event == null) return;
//            Log.e("测试","耳机事件"+event.getKeyCode()+"KeyEvent.KEYCODE_MEDIA_NEXT是"+KeyEvent.KEYCODE_MEDIA_NEXT+"KeyEvent.KEYCODE_MEDIA_PREVIOUS"+KeyEvent.KEYCODE_MEDIA_PREVIOUS+"KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE"+KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
////            //过滤按下事件
////            boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP);
////            if (!isActionUp) return;
////            //避免在Receiver里做长时间的处理，使得程序在CPU使用率过高的情况下出错，把信息发给handlera处理。
////            int keyCode = event.getKeyCode();
////            long eventTime = event.getEventTime() - event.getDownTime();//按键按下到松开的时长
////            Message msg = Message.obtain();
////            msg.what = 100;
////            Bundle data = new Bundle();
////            data.putInt("key_code", keyCode);
////            data.putLong("event_time", eventTime);
////            msg.setData(data);
////            handler.sendMessage(msg);
//            //终止广播(不让别的程序收到此广播，免受干扰)
//            abortBroadcast();
//        }
//    };

    }