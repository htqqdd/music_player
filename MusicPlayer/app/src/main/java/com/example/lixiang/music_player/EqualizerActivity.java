package com.example.lixiang.music_player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.AestheticActivity;

import static android.view.View.GONE;
import static com.example.lixiang.music_player.R.id.equalizer;
import static com.example.lixiang.music_player.R.id.transition_current_scene;

public class EqualizerActivity extends AestheticActivity {
    private PlayService playService;
    private int audioSessionId = 0;
    private Equalizer mEqualizer;
    private SeekBar[] seekBars;
    private SwitchCompat equalizerSwitch;
    private SwitchCompat echoCanceler;
    private SwitchCompat autoGain;
    private SwitchCompat suppressor;
    private Spinner spinner;
    private SwitchCompat bassboost;
    private SeekBar bass_seekbar;
    private SwitchCompat virtualizer;
    private SeekBar virtualizer_seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        //初始化变量
        equalizerSwitch = (SwitchCompat) findViewById(equalizer);
        spinner = (Spinner) findViewById(R.id.equalizer_spinner);
        bassboost = (SwitchCompat) findViewById(R.id.BassBoost);
        bass_seekbar = (SeekBar) findViewById(R.id.bass_seekbar);
        echoCanceler = (SwitchCompat) findViewById(R.id.AcousticEchoCanceler);
        autoGain = (SwitchCompat) findViewById(R.id.AutomaticGainControl);
        suppressor = (SwitchCompat) findViewById(R.id.NoiseSuppressor);
        virtualizer = (SwitchCompat) findViewById(R.id.Virtualizer);
        virtualizer_seekbar = (SeekBar) findViewById(R.id.virtualizer_seekbar);


        //绑定服务
        Intent bindIntent = new Intent(EqualizerActivity.this, PlayService.class);
        bindService(bindIntent, conn, BIND_AUTO_CREATE);


        bass_seekbar.setMax(1000);
        virtualizer_seekbar.setMax(1000);
        getPreference();


        //均衡器
        try {
            mEqualizer = new Equalizer(0, audioSessionId);
            seekBars = new SeekBar[5];
            equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    LinearLayout equalizer_layout = (LinearLayout) findViewById(R.id.equalizer_seekbar);
                    if (b == false) {
                        equalizer_layout.setVisibility(GONE);
                    } else {
                        equalizer_layout.setVisibility(View.VISIBLE);
                    }
                    spinner.setEnabled(b);
                    playService.setEqualizer(b);
                }
            });

            // 获取均衡控制器支持最小值和最大值
            final short minEQLevel = mEqualizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
            final short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
            // 获取均衡控制器支持的所有频率
            short brands = mEqualizer.getNumberOfBands();
            LinearLayout layout = (LinearLayout) findViewById(R.id.equalizer_seekbar);
            for (short i = 0; i < brands; i++) {
                TextView eqTextView = new TextView(this);
                // 创建一个TextView，用于显示频率
                eqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                // 设置该均衡控制器的频率
                eqTextView.setText((mEqualizer.getCenterFreq(i) / 1000)
                        + " Hz");
                layout.addView(eqTextView);
                // 创建一个水平排列组件的LinearLayout
                LinearLayout tmpLayout = new LinearLayout(this);
                tmpLayout.setOrientation(LinearLayout.HORIZONTAL);
                // 创建显示均衡控制器最小值的TextView
                TextView minDbTextView = new TextView(this);
                minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                // 显示均衡控制器的最小值
                minDbTextView.setText((minEQLevel / 100) + " dB");
                // 创建显示均衡控制器最大值的TextView
                TextView maxDbTextView = new TextView(this);
                maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                // 显示均衡控制器的最大值
                maxDbTextView.setText((maxEQLevel / 100) + " dB");
                LinearLayout.LayoutParams layoutParams = new
                        LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                SeekBar bar = new SeekBar(this);
                seekBars[i] = bar;
                bar.setLayoutParams(layoutParams);
                bar.setMax(maxEQLevel - minEQLevel);
                final short band = i;
                // 为SeekBar的拖动事件设置事件监听器
                bar.setOnSeekBarChangeListener(new SeekBar
                        .OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.v("均衡器改变", "是");
                        if (fromUser) {
                            Spinner spinner = (Spinner) findViewById(R.id.equalizer_spinner);
                            spinner.setSelection(12);
                        }
                        // 设置该频率的均衡值
                        playService.setEqualizerBandLevel(band, (short) (progress + minEQLevel));

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                // 使用水平排列组件的LinearLayout“盛装”3个组件
                tmpLayout.addView(minDbTextView);
                tmpLayout.addView(bar);
                tmpLayout.addView(maxDbTextView);
                // 将水平排列组件的LinearLayout添加到myLayout容器中
                layout.addView(tmpLayout);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //音场
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (seekBars !=null){
                switch (i) {
                    case 0:
                        seekBars[0].setProgress(1800);
                        seekBars[1].setProgress(1500);
                        seekBars[2].setProgress(1500);
                        seekBars[3].setProgress(1500);
                        seekBars[4].setProgress(1800);
                        return;
                    case 1:
                        seekBars[0].setProgress(2000);
                        seekBars[1].setProgress(1800);
                        seekBars[2].setProgress(1300);
                        seekBars[3].setProgress(1900);
                        seekBars[4].setProgress(1900);
                        return;
                    case 2:
                        seekBars[0].setProgress(2100);
                        seekBars[1].setProgress(1500);
                        seekBars[2].setProgress(1700);
                        seekBars[3].setProgress(1900);
                        seekBars[4].setProgress(1600);
                        return;
                    case 3:
                        seekBars[0].setProgress(1500);
                        seekBars[1].setProgress(1500);
                        seekBars[2].setProgress(1500);
                        seekBars[3].setProgress(1500);
                        seekBars[4].setProgress(1500);
                        return;
                    case 4:
                        seekBars[0].setProgress(1800);
                        seekBars[1].setProgress(1500);
                        seekBars[2].setProgress(1500);
                        seekBars[3].setProgress(1700);
                        seekBars[4].setProgress(1400);
                        return;
                    case 5:
                        seekBars[0].setProgress(1900);
                        seekBars[1].setProgress(1600);
                        seekBars[2].setProgress(2600);
                        seekBars[3].setProgress(1800);
                        seekBars[4].setProgress(1500);
                        return;
                    case 6:
                        seekBars[0].setProgress(2000);
                        seekBars[1].setProgress(1800);
                        seekBars[2].setProgress(1500);
                        seekBars[3].setProgress(1600);
                        seekBars[4].setProgress(1800);
                        return;
                    case 7:
                        seekBars[0].setProgress(1900);
                        seekBars[1].setProgress(1700);
                        seekBars[2].setProgress(1300);
                        seekBars[3].setProgress(1700);
                        seekBars[4].setProgress(2000);
                        return;
                    case 8:
                        seekBars[0].setProgress(1400);
                        seekBars[1].setProgress(1700);
                        seekBars[2].setProgress(2000);
                        seekBars[3].setProgress(1600);
                        seekBars[4].setProgress(1300);
                        return;
                    case 9:
                        seekBars[0].setProgress(2000);
                        seekBars[1].setProgress(1800);
                        seekBars[2].setProgress(1400);
                        seekBars[3].setProgress(1800);
                        seekBars[4].setProgress(2000);
                        return;
                    case 10:
                        seekBars[0].setProgress(1500);
                        seekBars[1].setProgress(2300);
                        seekBars[2].setProgress(1900);
                        seekBars[3].setProgress(1600);
                        seekBars[4].setProgress(2500);
                        return;
                    case 11:
                        seekBars[0].setProgress(1330);
                        seekBars[1].setProgress(1770);
                        seekBars[2].setProgress(1550);
                        seekBars[3].setProgress(1280);
                        seekBars[4].setProgress(1700);
                        return;
                    case 12:
//                        seekBars[0].setProgress(1500);
//                        seekBars[1].setProgress(1500);
//                        seekBars[2].setProgress(1500);
//                        seekBars[3].setProgress(1500);
//                        seekBars[4].setProgress(1500);
                        return;
                    default:
                }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        bassboost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SeekBar bass_seekbar = (SeekBar) findViewById(R.id.bass_seekbar);
                playService.setBass(b);
                bass_seekbar.setEnabled(b);
            }
        });



        bass_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playService.setBassStrength((short) i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        virtualizer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                playService.setVirtualizer(b);
                virtualizer_seekbar.setEnabled(b);
            }
        });
        virtualizer_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playService.setVirtualizerStrength((short) i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


//次要

        echoCanceler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    if (AcousticEchoCanceler.isAvailable()) {
                        playService.setCanceler(b);
                    } else {
                        Toast.makeText(EqualizerActivity.this, "您的手机不支持回声消除", Toast.LENGTH_SHORT).show();
                        echoCanceler.setChecked(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        autoGain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    if (AutomaticGainControl.isAvailable()) {
                        playService.setControl(b);
                    } else {
                        Toast.makeText(EqualizerActivity.this, "您的手机不支持自动增强", Toast.LENGTH_SHORT).show();
                        autoGain.setChecked(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        suppressor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    if (NoiseSuppressor.isAvailable()) {
                        playService.setSuppressor(b);
                    } else {
                        Toast.makeText(EqualizerActivity.this, "您的手机不支持智能降噪", Toast.LENGTH_SHORT).show();
                        suppressor.setChecked(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            playService = ((PlayService.musicBinder) service).getService();
            audioSessionId = playService.getAudioSessionId();
        }
    };

    private void getPreference(){
        SharedPreferences bundle = EqualizerActivity.this.getSharedPreferences("audioEffect",EqualizerActivity.this.MODE_PRIVATE);
        //均衡器
        LinearLayout equalizer_layout = (LinearLayout) findViewById(R.id.equalizer_seekbar);
        try {
            if (bundle.getBoolean("Equalizer", false) == false) {
                equalizer_layout.setVisibility(GONE);
            } else {
                spinner.setSelection(bundle.getInt("Spinner", 0));
                equalizer_layout.setVisibility(View.VISIBLE);
            }
            equalizerSwitch.setChecked(bundle.getBoolean("Equalizer", false));
            spinner.setEnabled(bundle.getBoolean("Equalizer", false));
            //低音增强
            bassboost.setChecked(bundle.getBoolean("Bass", false));
            bass_seekbar.setProgress(bundle.getInt("Bass_seekBar", 0));
            bass_seekbar.setEnabled(bundle.getBoolean("Bass", false));
            //虚拟环绕
            virtualizer.setChecked(bundle.getBoolean("Virtualizer", false));
            virtualizer_seekbar.setProgress(bundle.getInt("Virtualizer_seekBar", 0));
            virtualizer_seekbar.setEnabled(bundle.getBoolean("Virtualizer", false));
            //次要
            echoCanceler.setChecked(bundle.getBoolean("Canceler", false));
            autoGain.setChecked(bundle.getBoolean("AutoGain", false));
            suppressor.setChecked(bundle.getBoolean("Suppressor", false));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        SharedPreferences.Editor editor = getSharedPreferences("audioEffect", MODE_PRIVATE).edit();
        editor.putBoolean("Equalizer",equalizerSwitch.isChecked());
        editor.putInt("Spinner",spinner.getSelectedItemPosition());
        editor.putBoolean("Bass",bassboost.isChecked());
        editor.putInt("Bass_seekBar",bass_seekbar.getProgress());
        editor.putBoolean("Virtualizer",virtualizer.isChecked());
        editor.putInt("Virtualizer_seekBar",virtualizer_seekbar.getProgress());
        Log.v("低音增强","值"+bass_seekbar.getProgress());
        editor.putBoolean("Canceler",echoCanceler.isChecked());
        editor.putBoolean("AutoGain",autoGain.isChecked());
        editor.putBoolean("Suppressor",suppressor.isChecked());
        editor.apply();
        Log.v("储存","储存");
    }
}
