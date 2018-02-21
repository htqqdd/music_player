package com.lixiangsoft.lixiang.music_player;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 2018/2/18.
 */

public class playNowCoverPagerAdapter extends PagerAdapter {
    private AppCompatActivity activity;
    private List<musicInfo> musicListNow = MyApplication.getMusicListNow();
    private Bitmap resource;
    public playNowCoverPagerAdapter(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        if (musicListNow!=null) {
            return musicListNow.size();
        }else return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT);
        ImageView view = new ImageView(activity);
        view.setLayoutParams(params);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        container.addView(view);
        musicInfo nowMusic = musicListNow.get(position);
        if (!nowMusic.getMusicLink().equals("")) {//网络
            Glide.with(activity).load(nowMusic.getMusicLargeAlbum()).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.default_album).into(view);
        } else {//本地
            if (nowMusic.getAlbumLink() != null) {//本地下载
                Glide.with(activity).load(nowMusic.getAlbumLink()).placeholder(R.drawable.default_album).centerCrop().into(view);
            } else {//本地原有
              Glide.with(activity).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), nowMusic.getMusicAlbumId())).centerCrop().placeholder(R.drawable.default_album).into(view);
            }
        }
        view.setOnClickListener(MyApplication.getlyric_onClickListener());
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView imageView = (ImageView) object;
        if (imageView == null)
            return;
        Glide.clear(imageView);     //核心，解决OOM
        container.removeView(imageView);
    }
}
