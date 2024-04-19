package com.genymobile.transferclient.tools;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ScreenUtil {

    //设置状态栏文字颜色
    public static void setStatusBarFontColor(Window window, boolean black) {
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (black) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);
    }

    public static void changeFullScreen(Activity activity, boolean isFullscreen) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        View decorView = activity.getWindow().getDecorView();
        int flag = decorView.getSystemUiVisibility();
        if (isFullscreen) {
            // 状态栏隐藏
            flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            // 导航栏隐藏
            flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            // 布局延伸到导航栏
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            // 布局延伸到状态栏
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            // 全屏时,增加沉浸式体验
            flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            //  部分国产机型适用.不加会导致退出全屏时布局被状态栏遮挡
            // activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            // android P 以下的刘海屏,各厂商都有自己的适配方式,具体在manifest.xml中可以看到
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams pa = activity.getWindow().getAttributes();
                pa.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                activity.getWindow().setAttributes(pa);
            }
        } else {
            flag &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            flag &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            flag &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            flag &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            //            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams pa = activity.getWindow().getAttributes();
                pa.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                activity.getWindow().setAttributes(pa);
            }
        }
        decorView.setSystemUiVisibility(flag);
    }

    public static void setTranslateStatusBar(Window window) {
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
    }

    public static void setStatusBarBgColor(Window window, int color) {
//        setTranslateStatusBar(window);
        //设置状态栏颜色
        window.setStatusBarColor(color);
        // 去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(android.R.id.content);
        if (v != null) {
            v.setForeground(null);
        }
    }
}