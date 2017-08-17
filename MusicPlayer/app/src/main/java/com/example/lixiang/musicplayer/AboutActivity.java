package com.example.lixiang.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

import org.polaric.colorful.CActivity;
import org.polaric.colorful.Colorful;


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
                .icon(R.mipmap.ic_launcher)
                .build());

        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(c, getResources().getDrawable(R.drawable.ic_info), "Version", false));

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("更新历史")
                .icon(getResources().getDrawable(R.drawable.ic_changelog))
                .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(c, "更新历史", "https://github.com/htqqdd/lixiangsoft/tree/master", true, false))
                .build());

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("许可信息")
                .icon(getResources().getDrawable(R.drawable.ic_file))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                        alert.setTitle("使用本应用即表示您同意以下条款");
                        alert.setMessage("本应用纯属娱乐，严禁用于任何商业用途。\n\n应用内歌曲搜索功能来自互联网WebView，应用自身不提供歌曲搜索下载功能。\n\n如在不经意间侵犯了您的利益，请通过下方联系方式通知我，我将于24h内删除该功能。");
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
                        Toast.makeText(c, "测试", Toast.LENGTH_SHORT).show();
                    }
                })
                .build());
        appCardBuilder.addItem(ConvenienceBuilder.createRateActionItem(c,getResources().getDrawable(R.drawable.ic_rate), "喜欢它，给个好评吧", null));


        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();

        authorCardBuilder.title("作者信息");

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("李翔")
                .subText("China")
                .icon(getResources().getDrawable(R.drawable.ic_person))
                .build());

        authorCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,getResources().getDrawable(R.drawable.ic_web),
                "访问网站",
                true,
                Uri.parse("https://github.com/htqqdd/lixiangsoft/tree/master")));

        authorCardBuilder.addItem(ConvenienceBuilder.createEmailItem(c,getResources().getDrawable(R.drawable.ic_mail),
                "发送邮件",
                true,
                "htqqdd@gmail.com",
                "关于音乐播放器"));

//        authorCardBuilder.addItem(ConvenienceBuilder.createPhoneItem(c,getResources().getDrawable(R.drawable.ic_phone),
//                "联系方式",
//                true,
//                "+86 159 9696 3273"));



        return new MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build());

    }
    @Override
    protected CharSequence getActivityTitle() {
//        return getString(R.string.mal_title_about);
        return "关于";
    }
}