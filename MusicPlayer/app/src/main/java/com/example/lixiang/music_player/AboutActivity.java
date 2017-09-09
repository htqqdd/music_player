package com.example.lixiang.music_player;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.tencent.bugly.beta.Beta;


public class AboutActivity extends MaterialAboutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull final Context c) {
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();

        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text("音乐播放器")
                .desc("© 2017 LiXiang Soft")
                .icon(R.mipmap.new_launcher)
                .build());

        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(c, getResources().getDrawable(R.drawable.ic_info), "版本", false)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Beta.checkUpgrade();
                    }
                }));

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("更新历史")
                .icon(getResources().getDrawable(R.drawable.ic_changelog))
                .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(c, "更新历史", "https://github.com/htqqdd/music_player/releases", true, false))
                .build());

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("许可信息")
                .icon(getResources().getDrawable(R.drawable.ic_file))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                        alert.setTitle("许可信息");
                        alert.setMessage("使用本应用即表示您同意以下条款：\n\n本应用仅用于个人学习、娱乐，严禁用于任何商业用途。\n应用内聚合功能只提供数据检索服务，版权属于各提供方，应用自身不提供歌曲下载功能。\n如在不经意间侵犯了您的利益，请通过下方联系方式通知我，我将于24h内删除该功能。");
                        alert.setPositiveButton("我同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        alert.show();
                    }
                })
                .build());
        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Bug反馈")
                .icon(getResources().getDrawable(R.drawable.ic_bug))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Uri uri = Uri.parse("mailto:" + "htqqdd@gmail.com");
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "应用使用反馈"); // 主题
                        String phoneInfo = "Product: " + android.os.Build.PRODUCT + "\n";
                        phoneInfo += "CPU_ABI: " + android.os.Build.CPU_ABI + "\n";
                        phoneInfo += "MODEL: " + android.os.Build.MODEL + "\n";
                        phoneInfo += "SDK: " + android.os.Build.VERSION.SDK + "\n";
                        phoneInfo += "VERSION.RELEASE: " + android.os.Build.VERSION.RELEASE + "\n";
                        phoneInfo += "DEVICE: " + android.os.Build.DEVICE + "\n";
                        phoneInfo += "ID: " + android.os.Build.ID + "\n";
                        phoneInfo += "MANUFACTURE: " + android.os.Build.MANUFACTURER + "\n";
                        intent.putExtra(Intent.EXTRA_TEXT, "设备信息:" + "\n" + phoneInfo + "\n" + "\n" + "反馈信息:" + "\n"); // 正文
                        startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
                    }
                })
                .build());
        appCardBuilder.addItem(ConvenienceBuilder.createRateActionItem(c,getResources().getDrawable(R.drawable.ic_rate), "喜欢它，给个好评吧", null));


        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();

        authorCardBuilder.title("作者信息");

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("李翔")
                .subText("JiangSu Province China")
                .icon(getResources().getDrawable(R.drawable.ic_person))
                .build());

        authorCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,getResources().getDrawable(R.drawable.ic_web),
                "访问网站",
                true,
                Uri.parse("https://www.pgyer.com/lx_mp")));

        authorCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,getResources().getDrawable(R.drawable.ic_github),
                "开源地址",
                true,
                Uri.parse("https://github.com/htqqdd/music_player")));

        authorCardBuilder.addItem(ConvenienceBuilder.createEmailItem(c,getResources().getDrawable(R.drawable.ic_mail),
                "发送邮件",
                true,
                "htqqdd@gmail.com",
                "关于音乐播放器"));


        return new MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build());

    }
    @Override
    protected CharSequence getActivityTitle() {
//        return getString(R.string.mal_title_about);
        return "关于";
    }
}
