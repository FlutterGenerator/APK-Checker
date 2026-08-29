package com.by_syk.apkchecker.util;

import android.annotation.SuppressLint;

/* JADX INFO: loaded from: classes.dex */
public class UnitUtil {
    @SuppressLint({"DefaultLocale", "UseValueOf"})
    public static String formatMemory(long j) {
        if (j >= 1073741824) {
            return String.format("%.2fGB", (float) j / 1073741824);
        }
        if (j >= 1048576) {
            return String.format("%.2fMB", (float) (j / 1048576));
        }
        if (j >= 1024) {
            return String.format("%.2fKB", (float) (j / 1024));
        }
        if (j >= 0) {
            return j + "B";
        }
        return "";
    }
}
