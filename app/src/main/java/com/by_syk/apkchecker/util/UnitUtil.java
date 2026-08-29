package com.by_syk.apkchecker.util;

/* JADX INFO: loaded from: classes.dex */
public class UnitUtil {
    public static String formatMemory(long j) {
        if (j >= 1073741824) {
            return String.format("%.2fGB", new Float(j / 1073741824));
        }
        if (j >= 1048576) {
            return String.format("%.2fMB", new Float(j / 1048576));
        }
        if (j >= 1024) {
            return String.format("%.2fKB", new Float(j / 1024));
        }
        if (j >= 0) {
            return new StringBuffer().append(j).append("B").toString();
        }
        return "";
    }
}
