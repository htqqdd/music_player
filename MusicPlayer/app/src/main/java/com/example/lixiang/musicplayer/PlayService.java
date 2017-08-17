package com.example.lixiang.musicplayer;

import android.app.AlarmManager;
import android.app.Notification;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.Random;

import es.dmoral.toasty.Toasty;

import static android.R.attr.resource;
import static android.media.AudioManager.STREAM_MUSIC;
import static com.example.lixiang.musicplayer.Data.deleteAction;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.isHasInitialized;
import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.nextAction;
import static com.example.lixiang.musicplayer.Data.pauseAction;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previousAction;
import static com.example.lixiang.musicplayer.Data.sc_playAction;
import static com.example.lixiang.musicplayer.getCover.getArtwork;

public class PlayService extends Service implements AudioManager.OnAudioFocusChangeListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {
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
    private int positionNow = 0;
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
            mediaSession.setCallback(new MediaSession.Callback(){

            });
        }

        Data.setServiceStarted(true);
        Log.v("服务是否开启", "onCreate" + Data.getServiceState());
        //动态注册广播
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("service_broadcast");
        registerReceiver(serviceReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Data.setServiceStarted(true);
        Log.v("服务是否开启", "startCommand" + Data.getServiceState());
//        requestAudioFocus();
        if (intent.getIntExtra("ACTION", -2) == initialize) {
            Data.setPosition(0);
        }
        if (intent.getIntExtra("ACTION", -2) == sc_playAction) {
            if(isHasInitialized() == false){
                Data.initialMusicInfo(this);}
            if (Data.getPlayMode() != 1) {
                play(Data.getPosition());
            } else {
                positionNow = randomPosition();
                play(positionNow);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


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
                audioManager.abandonAudioFocus(this);
                //The service lost audio focus, the user probably moved to playing media on another app, so release the media player.
                Log.v("AUDIOFOCUS_LOSS", "焦点丢失");
                if (mediaPlayer.isPlaying()) pause();
                releaseMedia();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v("FOCUS_LOSS_TRANSIENT", "焦点暂时丢失");
                //Focus lost for a short time, pause the MediaPlayer.
                if (mediaPlayer.isPlaying()) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, probably a notification arrived on the device, lower the playback volume.
                Log.v("AUDIOFOCUS_LOSS_CAN", "焦点更短暂丢失");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Boolean lost_focus = sharedPref.getBoolean("lost_focus", false);
                if (lost_focus) {
                    if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        audioManager.abandonAudioFocus(this);
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", nextAction);
        sendBroadcast(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Data.setState(playing);
        if (onetime) {
            Intent intent = new Intent("play_broadcast");
            intent.putExtra("UIChange", initialize);
            sendBroadcast(intent);
        }
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", mediaChangeAction);
        sendBroadcast(intent);
        updateMetaData();
        new buildNotificationTask().execute(playing);
//        buildNotification(playing);
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
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getAudioSessionId() {
        return audioSessionId;
    }

    public void play(final int position) {

        Log.v("play方法执行", "play" + Data.getServiceState());
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
            Log.v("音乐Session", "音乐" + mediaPlayer.getAudioSessionId());
            path = Data.getData(position);
            Data.setPosition(position);
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare(); // 进行缓冲
                Log.v("音乐Session", "音乐" + mediaPlayer.getAudioSessionId());
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
        int random_position = random.nextInt(Data.getTotalNumber() - 1);
        return random_position;
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("ACTION", -2) == playAction) {
                Log.v("接收到playAction", "startCommand" + Data.getServiceState());
                if (Data.getPlayMode() != 1) {
                    play(Data.getPosition());
                    Log.v("发送play方法", "startCommand" + Data.getServiceState());
                } else {
                    positionNow = randomPosition();
                    play(positionNow);
                }
            }
            if (intent.getIntExtra("Control", 0) == previousAction) {
                previous();
            }
            if (intent.getIntExtra("Control", 0) == nextAction) {
                next();
            }
            if (intent.getIntExtra("Control", 0) == pauseAction) {
                removeAudioFocus();
                pause();
            }
            if (intent.getIntExtra("Control", 0) == playAction) {
                resume();
            }
            if (intent.getIntExtra("Control", 0) == deleteAction) {
                deleteService(intent.getIntExtra("DelayControl", 0));
            }
        }
    }

    public void deleteService(int time) {
        if (time != 0) {
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // 设置定时
            int duration = time * 60 * 1000;
            long setTime = duration + SystemClock.elapsedRealtime();
            //设置定时任务
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, setTime, pendingIntent(deleteAction));
        } else {
            pause();
            removeNotification();
        }
    }

    public void previous() {
        new previousTask().execute();
    }

    public void next() {
        new nextTask().execute();
    }

    public void pause() {
        mediaPlayer.pause();
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", pauseAction);
        sendBroadcast(intent);
        if (Data.getState() == playing) {
            new buildNotificationTask().execute(pausing);
        }
        Data.setState(pausing);
    }

    public void resume() {
        if (mediaPlayer != null) {
            requestAudioFocus();
            mediaPlayer.start();
        } else if (mediaPlayer == null) {
            play(Data.getPosition());
        }
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", playAction);
        Data.setState(playing);
        sendBroadcast(intent);
        new buildNotificationTask().execute(playing);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Data.setServiceStarted(false);
        Log.v("服务是否开启", "onDestroy" + Data.getServiceState());
        releaseMedia();
        removeNotification();
        removeAudioFocus();
        unregisterReceiver(serviceReceiver);
        if (mEqualizer != null) {
            mEqualizer.release();
        }
        if(mVirtualizer !=null){
            mVirtualizer.release();
        }
        if (canceler != null) {
            canceler.release();
        }
        if (control != null) {
            control.release();
        }
        if (suppressor != null) {
            suppressor.release();
        }
        if (mBass != null) {
            mBass.release();
        }
        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.v("焦点获得","获得2");
            return true;
        }
        //Could not gain focus
        return false;
    }

    public boolean removeAudioFocus() {
        if (audioManager !=null){
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);}
        return true;
    }


    public class nextTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            //判断是否有用户设置下一曲
            if (Data.getNextMusic() == -1) {
//0:列表重复
                if (Data.getPlayMode() == 0) {
                    if (Data.IsRecent()) {
                        if (Data.getRecent_position() < Data.getDateSublist().size() - 1) {
                            positionNow = Data.getDateSublist().get(Data.getRecent_position() + 1).getPosition();
                            Data.setRecent_position(Data.getRecent_position() + 1);
                            return positionNow;
                        } else {
                            positionNow = Data.getDateSublist().get(0).getPosition();
                            Data.setRecent_position(0);
                            return positionNow;
                        }
                    } else if (Data.IsFavourite()) {

                        if (Data.getFavourite_position() < Data.getTimessublist().size() - 1) {
                            positionNow = Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() + 1).getId());
                            Data.setFavourite_position(Data.getFavourite_position() + 1);
                            return positionNow;
                        } else {
                            positionNow = Data.findPositionById(Data.getTimessublist().get(0).getId());
                            Data.setFavourite_position(0);
                            return positionNow;
                        }

                    } else {

                        if (Data.getPosition() < Data.getTotalNumber() - 1) {
                            positionNow = Data.getPosition() + 1;
                            return positionNow;
                        } else {
                            positionNow = 0;
                            return positionNow;
                        }
                    }

                } else if (Data.getPlayMode() == 1) {//1:随机
                    positionNow = randomPosition();
                    return positionNow;
                } else if (Data.getPlayMode() == 2) {//2:单曲重复
                    return Data.getPosition();
                } else if (Data.getPlayMode() == 3) {//3:顺序
                    if (Data.IsRecent()) {

                        if (Data.getRecent_position() >= Data.getDateSublist().size() - 1) {
                            return null;
                        } else {
                            positionNow = Data.getDateSublist().get(Data.getRecent_position() + 1).getPosition();
                            Data.setRecent_position(Data.getRecent_position() + 1);
                            return positionNow;
                        }

                    } else if (Data.IsFavourite()) {

                        if (Data.getFavourite_position() >= Data.getTimessublist().size() - 1) {
                            return null;
                        } else {
                            positionNow = Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() + 1).getId());
                            Data.setFavourite_position(Data.getFavourite_position() + 1);
                            return positionNow;
                        }

                    } else {

                        if (Data.getPosition() >= Data.getTotalNumber() - 1) {
                            return null;
                        } else {
                            positionNow = Data.getPosition() + 1;
                            return positionNow;
                        }
                    }

                }

            } else {
                positionNow = Data.getNextMusic();
                Data.setNextMusic(-1);
                return positionNow;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                play((int) o);
            } else {
                Toasty.info(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public class previousTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (Data.getPlayMode() == 0) {//0:列表重复
                if (Data.IsRecent()) {

                    if (Data.getRecent_position() <= 0) {
                        positionNow = Data.getDateSublist().get(Data.getDateSublist().size() - 1).getPosition();
                        Data.setRecent_position(Data.getDateSublist().size() - 1);
                        return positionNow;
                    } else {
                        positionNow = Data.getDateSublist().get(Data.getRecent_position() - 1).getPosition();
                        Data.setRecent_position(Data.getRecent_position() - 1);
                        return positionNow;
                    }

                } else if (Data.IsFavourite()) {

                    if (Data.getFavourite_position() == 0) {
                        positionNow = Data.findPositionById(Data.getTimessublist().get(Data.getTimessublist().size() - 1).getId());
                        Data.setFavourite_position(Data.getTimessublist().size() - 1);
                        return positionNow;
                    } else {
                        positionNow = Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() - 1).getId());
                        Data.setFavourite_position(Data.getFavourite_position() - 1);
                        return positionNow;
                    }

                } else {

                    if (Data.getPosition() == 0) {
                        positionNow = Data.getTotalNumber() - 1;
                        return positionNow;
                    } else {
                        positionNow = Data.getPosition() - 1;
                        return positionNow;
                    }
                }

            } else if (Data.getPlayMode() == 1) {// 1:随机
                positionNow = randomPosition();
                return positionNow;
            } else if (Data.getPlayMode() == 2) {//2:单曲重复
                return Data.getPosition();

            } else {//3:顺序
                if (Data.IsRecent()) {

                    if (Data.getRecent_position() == 0) {
                        return null;
                    } else {
                        positionNow = Data.getDateSublist().get(Data.getRecent_position() - 1).getPosition();
                        Data.setRecent_position(Data.getRecent_position() - 1);
                        return positionNow;
                    }

                } else if (Data.IsFavourite()) {

                    if (Data.getFavourite_position() == 0) {
                        return null;
                    } else {
                        positionNow = Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() - 1).getId());
                        Data.setFavourite_position(Data.getFavourite_position() - 1);
                        return positionNow;
                    }

                } else {

                    if (Data.getPosition() == 0) {
                        return null;
                    } else {
                        positionNow = Data.getPosition() - 1;
                        return positionNow;
                    }

                }
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                play((int) o);
            } else {
                Toasty.info(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT, true).show();
            }
        }
    }



    public class savePlayTimesTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            int Playtimes = Data.findPlayTimesById(Data.getId(Data.getPosition()));
            Playtimes++;
            SharedPreferences.Editor editor = getSharedPreferences("playtimes", MODE_PRIVATE).edit();
            editor.putInt(String.valueOf(Data.getId(Data.getPosition())), Playtimes);
            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.v("Playtimes", "播放次数" + Data.findPlayTimesById(Data.getId(Data.getPosition())));
        }
    }

    private void updateMetaData() {
        singerNow = Data.getArtist(Data.getPosition());
        titleNow = Data.getTitle(Data.getPosition());
        albumNow = getArtwork(PlayService.this, Data.getId(Data.getPosition()), Data.getAlbumId(Data.getPosition()), true);
        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumNow)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, singerNow)
                .putString(MediaMetadata.METADATA_KEY_TITLE, titleNow)
                .build());
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
        stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void initialAudioEffect(int audioSessionId) {
        canceler = AcousticEchoCanceler.create(audioSessionId);
        control = AutomaticGainControl.create(audioSessionId);
        suppressor = NoiseSuppressor.create(audioSessionId);
        getPreference();
    }

    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public void setEqualizer(boolean b) {
        mEqualizer.setEnabled(b);
    }

    public void setEqualizerBandLevel(Short band, Short level) {
        mEqualizer.setBandLevel(band, level);
    }

    public void setCanceler(boolean b) {
        canceler.setEnabled(b);
    }

    public void setControl(boolean b) {
        control.setEnabled(b);
    }

    public void setSuppressor(boolean b) {
        suppressor.setEnabled(b);
    }

    public void setBass(boolean b) {
        mBass.setEnabled(b);
        loudnessEnhancer.setEnabled(b || mVirtualizer.getEnabled());
    }

    public void setBassStrength(Short strength) {
        mBass.setStrength(strength);
    }

    public void setVirtualizer(Boolean b) {
        mVirtualizer.setEnabled(b);
        loudnessEnhancer.setEnabled(b || mBass.getEnabled());
    }

    public void setVirtualizerStrength(Short s){mVirtualizer.setStrength(s);}

    private void getPreference(){
        SharedPreferences bundle = this.getSharedPreferences("audioEffect",MODE_PRIVATE);
        //均衡器
        if (!bundle.getBoolean("Equalizer",false)){
            if (mEqualizer !=null){
            mEqualizer.setEnabled(false);
            }
        } else{
            if (mEqualizer == null){
                mEqualizer = new Equalizer(0, audioSessionId);
                mEqualizer.setEnabled(true);
                switch (bundle.getInt("Spinner",0)) {
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
            }
        }

        //低音增强
        if (!bundle.getBoolean("Bass",false)){
            if (mBass !=null){
                mBass.setEnabled(false);
            }
        } else {
            if (mBass == null) {
                mBass = new BassBoost(0, audioSessionId);
                mBass.setEnabled(true);
                mBass.setStrength((short) bundle.getInt("Bass_seekBar", 0));
            }
        }

        //虚拟环绕
        if (!bundle.getBoolean("Virtualizer",false)){
            if (mVirtualizer !=null){
                mVirtualizer.setEnabled(false);
            }
        } else {
            if (mVirtualizer ==null) {
                mVirtualizer = new Virtualizer(0, audioSessionId);
                mVirtualizer.setEnabled(bundle.getBoolean("Virtualizer", false));
                mVirtualizer.setStrength((short) bundle.getInt("Virtualizer_seekBar", 0));
            }
        }

        //超强音量
        if (!(bundle.getBoolean("Bass",false) || bundle.getBoolean("Virtualizer",false))){
            if (loudnessEnhancer !=null){
                loudnessEnhancer.setEnabled(false);
            }
        } else {
            if (loudnessEnhancer ==null) {
                loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
                loudnessEnhancer.setEnabled(true);
                loudnessEnhancer.setTargetGain(500);
            }
        }

        //次要
        if (AcousticEchoCanceler.isAvailable()){
        canceler.setEnabled(bundle.getBoolean("Canceler",false));}
        if (AutomaticGainControl.isAvailable()){
        control.setEnabled(bundle.getBoolean("AutoGain",false));}
        if (NoiseSuppressor.isAvailable()){
        suppressor.setEnabled(bundle.getBoolean("Suppressor",false));}
    }

    private class buildNotificationTask extends AsyncTask<String, Integer, state_color> {
        @Override
        protected state_color doInBackground(String... strings) {
            String state = strings[0];
            Palette p = Palette.from(albumNow).generate();
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
            return new state_color(state, color);
        }

        @Override
        protected void onPostExecute(state_color state_image_color) {
            super.onPostExecute(state_image_color);
            String playState = state_color.getState();
            int color = state_color.getColor();
            int notificationAction = R.drawable.ic_pause_black_24dp;//needs to be initialized
            PendingIntent play_pauseIntent = null;
            //Build a new notification according to the current state of the MediaPlayer
            if (playState == playing) {
                notificationAction = R.drawable.ic_pause_black_24dp;
                //create the pause action
                play_pauseIntent = pendingIntent(pauseAction);
            } else if (playState == pausing) {
                notificationAction = R.drawable.ic_play_arrow_black_24dp;
                //create the play action
                play_pauseIntent = pendingIntent(playAction);
            }
            Intent startMain = new Intent(PlayService.this, MainActivity.class);
            PendingIntent startMainActivity = PendingIntent.getActivity(PlayService.this, 0, startMain, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new Notification.Builder(PlayService.this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(albumNow)
                    .setSmallIcon(R.drawable.ic_album_black_24dp)
                    .setDeleteIntent(pendingIntent(deleteAction))
                    .setColor(color)
                    .setContentIntent(startMainActivity)
                    .setOngoing(Data.getState() == playing)//该选项会导致手表通知不显示
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_fast_rewind_black_24dp, "SkiptoPrevious", pendingIntent(previousAction)) // #0
                    .addAction(notificationAction, "Play or Pause", play_pauseIntent)  // #1
                    .addAction(R.drawable.ic_fast_forward_black_24dp, "SkiptoNext", pendingIntent(nextAction))     // #2
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken())
                    )
                    .setContentTitle(Data.getTitle(Data.getPosition()))
                    .setContentText(Data.getArtist(Data.getPosition()))
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);

        }

    }

    public static class state_color {
        public static int mColor;
        public static String mState;

        public state_color(String state, int color) {
            mColor = color;
            mState = state;
        }

        public static int getColor() {
            return mColor;
        }

        public static String getState() {
            return mState;
        }
    }
}
