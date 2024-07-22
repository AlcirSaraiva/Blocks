package com.awesome.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by awesome_robot on 2.04.18.
 */

public class Common {
    public static void fullScreen(Activity activityReference, View decorView0){
        if (Build.VERSION.SDK_INT < 19) {
            try
            {
                activityReference.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            catch(Exception e0) {
                System.out.println(e0.getMessage());
            }
        } else {
            try
            {
                View decorView = activityReference.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            }
            catch(Exception e0)
            {
                System.out.println(e0.getMessage());
                try
                {
                    View decorView = decorView0;
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    decorView.setSystemUiVisibility(uiOptions);
                }
                catch(Exception e1)
                {
                    System.out.println(e1.getMessage());
                }
            }
        }
    }

    public static boolean isLargeScreen(Context context) {
        boolean largeScreen;
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                largeScreen = true;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                largeScreen = false;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                largeScreen = false;
                break;
            default:
                largeScreen = false;
        }
        return largeScreen;
    }
}
