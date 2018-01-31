package com.lixiangsoft.lixiang.music_player;

/**
 * Created by lixiang on 2017/10/2.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.graphics.Palette;

public class ColorUtil {

    public static boolean isColorLight(@ColorInt int color) {
        return getColorDarkness(color) < 0.5;
    }

    public static double getColorDarkness(@ColorInt int color) {
        if (color == Color.BLACK)
            return 1.0;
        else if (color == Color.WHITE || color == Color.TRANSPARENT)
            return 0.0;
        else
            return (1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255);
    }

    @ColorInt
    public static int getInverseColor(@ColorInt int color) {
        return (0xFFFFFF - color) | 0xFF000000;
    }

    public static boolean isColorSaturated(@ColorInt int color) {
        double max = Math.max(0.299 * Color.red(color), Math.max(0.587 * Color.green(color), 0.114 * Color.blue(color)));
        double min = Math.min(0.299 * Color.red(color), Math.min(0.587 * Color.green(color), 0.114 * Color.blue(color)));
        double diff = Math.abs(max - min);
        return diff > 20;
    }

    @ColorInt
    public static int getMixedColor(@ColorInt int color1, @ColorInt int color2) {
        return Color.rgb(
                (Color.red(color1) + Color.red(color2)) / 2,
                (Color.green(color1) + Color.green(color2)) / 2,
                (Color.blue(color1) + Color.blue(color2)) / 2
        );
    }

    public static double getDifference(@ColorInt int color1, @ColorInt int color2) {
        double diff = Math.abs(0.299 * (Color.red(color1) - Color.red(color2)));
        diff += Math.abs(0.587 * (Color.green(color1) - Color.green(color2)));
        diff += Math.abs(0.114 * (Color.blue(color1) - Color.blue(color2)));
        return diff;
    }

    @ColorInt
    public static int getReadableText(@ColorInt int textColor, @ColorInt int backgroundColor) {
        return getReadableText(textColor, backgroundColor, 100);
    }

    @ColorInt
    public static int getReadableText(@ColorInt int textColor, @ColorInt int backgroundColor, int difference) {
        boolean isLight = isColorLight(backgroundColor);
        for (int i = 0; getDifference(textColor, backgroundColor) < difference && i < 100; i++) {
            textColor = getMixedColor(textColor, isLight ? Color.BLACK : Color.WHITE);
        }

        return textColor;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static int getColor (Palette palette){
        if (palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        } else if (palette.getMutedSwatch() != null) {
            return palette.getMutedSwatch().getRgb();
        } else if (palette.getDarkVibrantSwatch() != null) {
            return palette.getDarkVibrantSwatch().getRgb();
        } else if (palette.getDarkMutedSwatch() != null) {
            return palette.getDarkMutedSwatch().getRgb();
        } else if (palette.getLightVibrantSwatch() != null) {
            return palette.getLightVibrantSwatch().getRgb();
        } else if (palette.getLightMutedSwatch() != null) {
            return palette.getLightMutedSwatch().getRgb();
        } else {
            return Color.parseColor("#009688");
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}