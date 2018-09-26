package com.example.janis.maps;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentActivity;

public class Utils extends FragmentActivity {


    protected int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    protected boolean colorBetween(@ColorInt int color, @ColorInt int color2, int r, int g, int b) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int red2 = Color.red(color2);
        int green2 = Color.green(color2);
        int blue2 = Color.blue(color2);

        return between(red, red2 + r, red2 - r) && between(green, green2 + g, green2 - g) && between(blue, blue2 + b, blue2 - b);
    }


    protected boolean between(int i, int maxValueInclusive, int minValueInclusive) {
        return i >= minValueInclusive && i <= maxValueInclusive;
    }
}
