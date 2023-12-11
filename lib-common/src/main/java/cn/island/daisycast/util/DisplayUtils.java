package cn.island.daisycast.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DisplayUtils {
    private DisplayUtils(){}
    private static DisplayMetrics INTERFACE = Resources.getSystem().getDisplayMetrics();

    public static int widthPixels = INTERFACE.widthPixels;
    public static int heightPixels = INTERFACE.heightPixels;
    public static int densityDpi = INTERFACE.densityDpi;
}
