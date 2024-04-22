package com.genymobile.transferclient.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ScreenUtils {

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.x;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.y;
    }


    /**
     * 获取ContentView宽度
     */
    public static int getContentViewWidth(Activity activity) {
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        return contentView.getWidth();
    }

    /**
     * 获取ContentView高度
     */
    public static int getContentViewHeight(Activity activity) {
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        return contentView.getHeight();
    }
}
