package com.by_syk.apkchecker.util;

import android.os.Build;

/* JADX INFO: loaded from: classes.dex */
public class C {
    public static final int DEFAULT_HIDE_FLAGS = 127713275;
    public static final String LOG_TAG = "APKCHECKER";
    public static final String SP_APK_NAME_FORMAT_ID = "apk_name_format_id";
    public static final String SP_APK_NAME_FORMAT_ID_2 = "apk_name_format_id_2";
    public static final String SP_BATCH_RENAME = "batch_rename";
    public static final String SP_DEV_MODE = "dev_mode";
    public static final String SP_EXTRA_LABEL = "extra_label";
    public static final String SP_HIDE_FLAGS = "hide_flags";
    public static final String SP_HIDE_ICON = "hide_icon";
    public static final String SP_INSTALLER_CLASS_NAME = "installer_class_name";
    public static final String SP_INSTALLER_PACKAGE_NAME = "installer_package_name";
    public static final String SP_LAST_APK_NEW = "last_apk_new";
    public static final String SP_LAST_APK_OLD = "last_apk_old";
    public static final String SP_LAST_APK_SIZE = "last_apk_size";
    public static final String SP_LIGHT_THEME = "light_theme";
    public static final String SP_PIC_NAME_FORMAT_ID = "pic_name_format_id";
    public static final String SP_REMEMBER_APK_NAME_FORMAT = "remember_apk_name_format";
    public static final String SP_REMEMBER_APK_NAME_FORMAT_2 = "remember_apk_name_format_2";
    public static final String SP_REMEMBER_PIC_NAME_FORMAT = "remember_pic_name_format";
    public static final String SP_SLOW_DOWN_TIP_SHOWED = "slow_down_tip_showed";
    public static final String TEMP_FILE_NAME_APK = "temp.apk";
    public static final String TEMP_FILE_NAME_ICON = "temp.png";
    public static final int SDK = Build.VERSION.SDK_INT;
    public static final String[][] INSTALLERS = {new String[]{"com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity"}, new String[]{"com.google.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity"}, new String[]{"com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerCommonActivity"}};
    public static final int[] DEBUG_SIGNS_HASH = {-672009692, -1790780999, -1434565454, -1263674583};
    public static final int[] COLORS = {-29042, -11486384, -8355712};
    public static final int[] COLORS_LIGHT = {-4456448, -16737536, -6908266};
    public static final int[] ITEMS_FLAG_POS = {0, 1, 2, 3, 4, 5, 6, 27, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

    public enum ENUM_FLAGS {
        FI,
        LU,
        IF,
        FP,
        FN,
        FS,
        LI,
        PN,
        AN,
        VN,
        VC,
        CT,
        MSV,
        TSV,
        SA,
        RP,
        P,
        F,
        LA,
        A,
        S,
        CP,
        BR,
        SI,
        CF,
        CS,
        CE,
        IS,
    }

    public static String getSDKIntStr2(int i) {
        switch (i) {
            case 1:
                return "Android 1.0";
            case 2:
                return "Android 1.1";
            case 3:
                return "Android 1.5";
            case 4:
                return "Android 1.6";
            case 5:
                return "Android 2.0";
            case 6:
                return "Android 2.0.1";
            case 7:
                return "Android 2.1";
            case 8:
                return "Android 2.2";
            case 9:
                return "Android 2.3";
            case 10:
                return "Android 2.3.3";
            case 11:
                return "Android 3.0";
            case 12:
                return "Android 3.1";
            case 13:
                return "Android 3.2";
            case 14:
                return "Android 4.0";
            case 15:
                return "Android 4.0.3";
            case 16:
                return "Android 4.1";
            case 17:
                return "Android 4.2";
            case 18:
                return "Android 4.3";
            case 19:
                return "Android 4.4";
            case 20:
                return "Android 4.4W";
            case 21:
                return "Android 5.0";
            case 22:
                return "Android 5.1";
            case 23:
                return "Android 6.0";
            case 24:
                return "Android 7.0";
            case 25:
                return "Android 7.1";
            case 26:
                return "Android 8.0";
            case 27:
                return "Android 8.1";
            case 28:
                return "Android 9 Pie";
            case 29:
                return "Android 10";
            case 30:
                return "Android 11";
            case 31:
                return "Android 12";
            case 32:
                return "Android 12L";
            case 33:
                return "Android 13";
            case 34:
                return "Android 14";
            case 35:
                return "Android 15";
            case 36:
                return "Android 16";
            case 37:
                return "Android 17";

            case 10000:
                return "Android Preview";

            default:
                return "Unknown Android API " + i;
        }
    }
}
