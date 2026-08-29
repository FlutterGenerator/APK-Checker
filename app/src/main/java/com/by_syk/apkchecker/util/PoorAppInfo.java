package com.by_syk.apkchecker.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/* JADX INFO: loaded from: classes.dex */
public class PoorAppInfo {
    private Context context = (Context) null;
    private PackageManager packageManager = (PackageManager) null;
    private PackageInfo packageInfo = (PackageInfo) null;
    private ApplicationInfo applicationInfo = (ApplicationInfo) null;
    public boolean is_ok = false;
    public String filePath = "";
    public String packageName = "";
    public String label = "";
    public String verName = "";
    public int ver_code = -1;

    public void init(Context context, PackageInfo packageInfo) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.packageInfo = packageInfo;
    }

    public boolean initAndAnalysis(Context context, PackageInfo packageInfo) {
        init(context, packageInfo);
        analyse();
        return this.is_ok;
    }

    public boolean analyse() {
        this.is_ok = false;
        if (this.context == null || this.packageInfo == null) {
            return false;
        }
        this.applicationInfo = this.packageInfo.applicationInfo;
        this.packageName = this.packageInfo.packageName;
        this.filePath = this.applicationInfo.publicSourceDir;
        this.label = this.applicationInfo.loadLabel(this.packageManager).toString();
        if (this.label.equals(this.packageName)) {
            this.label = "";
        }
        this.verName = this.packageInfo.versionName == null ? "" : this.packageInfo.versionName;
        this.ver_code = this.packageInfo.versionCode;
        this.is_ok = true;
        return true;
    }
}
