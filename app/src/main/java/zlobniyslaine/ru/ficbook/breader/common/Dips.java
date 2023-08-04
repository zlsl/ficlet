package zlobniyslaine.ru.ficbook.breader.common;

import android.content.res.Resources;

public class Dips {

    public static int spToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int dpToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

// --Commented out by Inspection START (16.07.20 22:32):
//    public static int pxToDp(final int px) {
//        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
//    }
// --Commented out by Inspection STOP (16.07.20 22:32)

// --Commented out by Inspection START (16.07.20 22:59):
//    public static int screenWidth() {
//        return Resources.getSystem().getDisplayMetrics().widthPixels;
//    }
// --Commented out by Inspection STOP (16.07.20 22:59)

// --Commented out by Inspection START (16.07.20 22:25):
//    public static int screenWidthDP() {
//        return pxToDp(screenWidth());
//    }
// --Commented out by Inspection STOP (16.07.20 22:25)

// --Commented out by Inspection START (16.07.20 22:25):
//    public static int screenHeightDP() {
//        return pxToDp(screenHeight());
//    }
// --Commented out by Inspection STOP (16.07.20 22:25)

// --Commented out by Inspection START (16.07.20 22:59):
//    public static int screenHeight() {
//        return Resources.getSystem().getDisplayMetrics().heightPixels;
//    }
// --Commented out by Inspection STOP (16.07.20 22:59)

// --Commented out by Inspection START (16.07.20 22:32):
//    public static int screenMinWH() {
//        return Math.min(screenHeight(), screenWidth());
//    }
// --Commented out by Inspection STOP (16.07.20 22:32)

// --Commented out by Inspection START (16.07.20 22:25):
//    public static boolean isSmallScreen() {
//        // large screens are at least 640dp x 480dp
//        return Dips.screenMinWH() < Dips.dpToPx(450);
//    }
// --Commented out by Inspection STOP (16.07.20 22:25)

}
