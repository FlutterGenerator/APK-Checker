package com.by_syk.apkchecker.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.InternalZipConstants;

/* JADX INFO: loaded from: classes.dex */
public class AppInfo {
    private String tempStr;
    private Activity activity = (Activity) null;
    private PackageManager packageManager = (PackageManager) null;
    private PackageInfo packageInfo = (PackageInfo) null;
    private ApplicationInfo applicationInfo = (ApplicationInfo) null;
    public boolean is_installed = false;
    public boolean light_theme = false;
    public boolean extra_label = true;
    public int hide_flags = C.DEFAULT_HIDE_FLAGS;
    private StringBuilder buffer = new StringBuilder();
    public boolean is_ok = false;
    public long first_install = -1;
    public String firstInstallStr = "";
    public long last_update = -1;
    public String lastUpdateStr = "";
    public String installer = "";
    public SpannableStringBuilder ssbInstaller = (SpannableStringBuilder) null;
    public File file = (File) null;
    public String filePath = "";
    public SpannableStringBuilder ssbFilePath = (SpannableStringBuilder) null;
    public String fileName = "";
    public SpannableStringBuilder ssbFileName = (SpannableStringBuilder) null;
    public long file_size = -1;
    public String fileSizeStr = "";
    public Drawable ic_launcher = (Drawable) null;
    public int icon_width_px = -1;
    public int icon_height_px = -1;
    public SpannableStringBuilder ssbIconSize = (SpannableStringBuilder) null;
    public String packageName = "";
    public String label = "";
    public String labelEn = "";
    public String appName = "";
    public String verName = "";
    public int ver_code = 0;
    public long compiling_time = -1;
    public String compilingTimeStr = "";
    public int min_sdk = -1;
    public SpannableStringBuilder ssbMinSdk = (SpannableStringBuilder) null;
    public int target_sdk = -1;
    public SpannableStringBuilder ssbTargetSdk = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbSupportedABIs = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbReqPermissions = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbPermissions = (SpannableStringBuilder) null;
    public int flags = 0;
    public String flagsStr = "";
    public String launcherActivity = "";
    public SpannableStringBuilder ssbActivities = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbServices = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbProviders = (SpannableStringBuilder) null;
    public SpannableStringBuilder ssbReceivers = (SpannableStringBuilder) null;
    public String signatures = "";
    public SpannableStringBuilder ssbSignatures = (SpannableStringBuilder) null;
    public int signaturesHashCode = -1;
    public String certificateFingerprints = "";
    public long certificate_start = -1;
    public String certificateStartStr = "";
    public long certificate_end = -1;
    public String certificateEndStr = "";

    public void init(Activity activity, PackageInfo packageInfo, boolean z, boolean z2, int i, boolean z3) {
        this.activity = activity;
        this.packageManager = activity.getPackageManager();
        this.packageInfo = packageInfo;
        this.is_installed = z;
        this.light_theme = z2;
        this.hide_flags = i;
        this.extra_label = z3;
    }

    public boolean initAndAnalysis(Activity activity, PackageInfo packageInfo, boolean z, boolean z2, int i, boolean z3) throws Throwable {
        init(activity, packageInfo, z, z2, i, z3);
        analyse();
        return this.is_ok;
    }

    /* JADX WARN: Code duplicated, block: B:96:0x0232 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:98:0x0267 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @TargetApi(9)
    public boolean analyse() throws Throwable {
        InputStream byteArrayInputStream;
        Throwable th;
        CertificateException e;
        Intent launchIntentForPackage;
        this.is_ok = false;
        if (this.activity == null || this.packageInfo == null) {
            return false;
        }
        this.applicationInfo = this.packageInfo.applicationInfo;
        this.packageName = this.packageInfo.packageName;
        if (C.SDK >= 9 && this.is_installed) {
            this.first_install = this.packageInfo.firstInstallTime;
            this.firstInstallStr = ExtraUtil.convertMillisTime(this.first_install);
            this.last_update = this.packageInfo.lastUpdateTime;
            this.lastUpdateStr = ExtraUtil.convertMillisTime(this.last_update);
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.IF.ordinal())) {
            analyseInstalledFrom();
        }
        analyseFileAttributes();
        analysisLauncherIcon();
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.IS.ordinal())) {
            analyseIconSize();
        }
        analyseAppName();
        this.verName = this.packageInfo.versionName == null ? "" : this.packageInfo.versionName;
        this.ver_code = this.packageInfo.versionCode;
        analyseMinSDK();
        analyseTargetSDK();
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.CT.ordinal())) {
            if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.SA.ordinal())) {
                analyseSupportedABIs(true);
            } else {
                analyseCompilingTime();
            }
        } else if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.SA.ordinal())) {
            analyseSupportedABIs(false);
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.RP.ordinal())) {
            analyseReqPermissions();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.P.ordinal())) {
            analysePermissions();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.F.ordinal())) {
            analyseFlags();
        }
        if (this.is_installed && (launchIntentForPackage = this.packageManager.getLaunchIntentForPackage(this.packageName)) != null) {
            this.launcherActivity = launchIntentForPackage.getComponent().getShortClassName();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.A.ordinal())) {
            analyseActivities();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.S.ordinal())) {
            analyseServices();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.CP.ordinal())) {
            analyseProviders();
        }
        if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.BR.ordinal())) {
            analyseReceivers();
        }
        if (ExtraUtil.getBitsOR(this.hide_flags, C.ENUM_FLAGS.SI.ordinal(), C.ENUM_FLAGS.CF.ordinal(), C.ENUM_FLAGS.CS.ordinal(), C.ENUM_FLAGS.CE.ordinal())) {
            InputStream inputStream = (InputStream) null;
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Signature[] signatureArr = this.packageInfo.signatures;
                if (signatureArr != null && signatureArr.length > 0) {
                    byte[] byteArray = signatureArr[0].toByteArray();
                    this.signaturesHashCode = signatureArr[0].hashCode();
                    byteArrayInputStream = new ByteArrayInputStream(byteArray);
                    try {
                        try {
                            X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
                            if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.SI.ordinal())) {
                                analyseSignatures(x509Certificate.getIssuerDN(), this.signaturesHashCode);
                            }
                            if (ExtraUtil.getBit(this.hide_flags, C.ENUM_FLAGS.CF.ordinal())) {
                                this.buffer.setLength(0);
                                this.buffer.append(x509Certificate.getSigAlgName());
                                this.buffer.append(" v").append(x509Certificate.getVersion());
                                this.buffer.append("\nMD5: ").append(ExtraUtil.getCertificateSignatures(byteArray, "MD5"));
                                this.buffer.append("\nSHA1: ").append(ExtraUtil.getCertificateSignatures(byteArray, "SHA1"));
                                this.buffer.append("\nSHA256: ").append(ExtraUtil.getCertificateSignatures(byteArray, "SHA256"));
                                this.certificateFingerprints = this.buffer.toString();
                            }
                            this.certificate_start = x509Certificate.getNotBefore().getTime();
                            this.certificate_end = x509Certificate.getNotAfter().getTime();
                            this.certificateStartStr = ExtraUtil.convertMillisTime(this.certificate_start, "yyyy-MM-dd");
                            this.certificateEndStr = ExtraUtil.convertMillisTime(this.certificate_end, "yyyy-MM-dd");
                            inputStream = byteArrayInputStream;
                        } catch (Throwable th2) {
                            th = th2;
                            if (byteArrayInputStream != null) {
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e2) {
                                }
                            }
                            throw th;
                        }
                    } catch (CertificateException e3) {
                        e = e3;
                        e.printStackTrace();
                        inputStream = byteArrayInputStream;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        this.is_ok = true;
                        return true;
                    }
                }
            } catch (CertificateException e5) {
                byteArrayInputStream = inputStream;
                e = e5;
            } catch (Throwable th3) {
                byteArrayInputStream = inputStream;
                th = th3;
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                throw th;
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        this.is_ok = true;
        return true;
    }

    @TargetApi(InternalZipConstants.ENDHDR)
    private void analyseInstalledFrom() {
        Uri referrer;
        if (this.is_installed) {
            this.installer = this.packageManager.getInstallerPackageName(this.packageName);
        } else if (C.SDK >= 17) {
            if (C.SDK >= 22) {
                referrer = this.activity.getReferrer();
            } else {
                referrer = (Uri) this.activity.getIntent().getParcelableExtra("android.intent.extra.REFERRER");
            }
            if (referrer != null && ("android-app".equals(referrer.getScheme()) || "android-app://".equals(referrer.getScheme()))) {
                this.installer = referrer.getAuthority();
            }
        }
        if (!TextUtils.isEmpty(this.installer)) {
            CharSequence applicationLabel = (CharSequence) null;
            try {
                applicationLabel = this.packageManager.getApplicationLabel(this.packageManager.getApplicationInfo(this.installer, 0));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            this.ssbInstaller = new SpannableStringBuilder();
            if (!this.extra_label || TextUtils.isEmpty(applicationLabel)) {
                this.ssbInstaller.append((CharSequence) this.installer);
            } else {
                this.ssbInstaller.append((CharSequence) String.format("%1$s (%2$s)", this.installer, applicationLabel));
                this.ssbInstaller.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), this.installer.length() + 1, this.ssbInstaller.length(), 33);
            }
        }
    }

    private void analyseFileAttributes() {
        this.filePath = this.applicationInfo.publicSourceDir;
        this.ssbFilePath = new SpannableStringBuilder(this.filePath);
        this.file = new File(this.filePath);
        if (!this.is_installed) {
            this.fileName = this.file.getName();
            this.ssbFileName = new SpannableStringBuilder(this.fileName);
            if (this.file.getParentFile().equals(this.activity.getExternalCacheDir())) {
                this.ssbFilePath.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[0] : C.COLORS[0]), 0, this.ssbFilePath.length(), 33);
                this.ssbFileName.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[0] : C.COLORS[0]), 0, this.ssbFileName.length(), 33);
            }
        }
        this.file_size = this.file.length();
        this.fileSizeStr = UnitUtil.formatMemory(this.file_size);
    }

    private void analysisLauncherIcon() {
        this.ic_launcher = getLauncherIcon(false);
    }

    private void analyseIconSize() {
        if (this.ic_launcher != null) {
            this.ssbIconSize = new SpannableStringBuilder();
            this.icon_width_px = this.ic_launcher.getIntrinsicWidth();
            this.icon_height_px = this.ic_launcher.getIntrinsicHeight();
            float f = this.activity.getResources().getDisplayMetrics().density;
            this.ssbIconSize.append((CharSequence) String.format("%1$.0f*%2$.0fdp", new Float(this.icon_width_px / f), new Float(this.icon_height_px / f)));
            if (this.extra_label) {
                int length = this.ssbIconSize.length() + 1;
                this.ssbIconSize.append((CharSequence) String.format(" (%1$d*%2$dpx)", new Integer(this.icon_width_px), new Integer(this.icon_height_px)));
                this.ssbIconSize.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), length, this.ssbIconSize.length(), 33);
            }
        }
    }

    private void analyseAppName() {
        this.label = this.applicationInfo.loadLabel(this.packageManager).toString();
        if (this.label.equals(this.packageName)) {
            this.label = "";
            return;
        }
        try {
            Configuration configuration = new Configuration();
            configuration.locale = Locale.ENGLISH;
            Resources resourcesForApplication = this.packageManager.getResourcesForApplication(this.applicationInfo);
            resourcesForApplication.updateConfiguration(configuration, this.activity.getResources().getDisplayMetrics());
            int i = ((PackageItemInfo) this.applicationInfo).labelRes;
            if (i != 0) {
                this.labelEn = resourcesForApplication.getString(i);
            }
            configuration.locale = Locale.getDefault();
            resourcesForApplication.updateConfiguration(configuration, this.activity.getResources().getDisplayMetrics());
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
        this.appName = ("".equals(this.labelEn) || this.label.equals(this.labelEn)) ? this.label : String.format("%1$s\n%2$s", this.label, this.labelEn);
    }

    private void analyseCompilingTime() {
        try {
            this.compiling_time = new JarFile(this.filePath).getEntry("AndroidManifest.xml").getTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.compiling_time > 1201795200019L) {
            this.compilingTimeStr = ExtraUtil.convertMillisTime(this.compiling_time, "yyyy-MM-dd");
        }
    }

    private void analyseMinSDK() {
        this.min_sdk = MinSDKVersionUtil.getMinSdkVersion(this.file);
        if (this.min_sdk > 0) {
            this.ssbMinSdk = new SpannableStringBuilder();
            String SDKIntStr2 = C.getSDKIntStr2(this.min_sdk);
            if (!this.extra_label || "".equals(SDKIntStr2)) {
                this.ssbMinSdk.append((CharSequence) String.valueOf(this.min_sdk));
            } else {
                this.ssbMinSdk.append((CharSequence) String.format("%1$d (%2$s)", new Integer(this.min_sdk), SDKIntStr2));
                this.ssbMinSdk.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), 2, this.ssbMinSdk.length(), 33);
            }
        }
    }

    private void analyseTargetSDK() {
        this.ssbTargetSdk = new SpannableStringBuilder();
        this.target_sdk = this.applicationInfo.targetSdkVersion;
        String SDKIntStr2 = C.getSDKIntStr2(this.target_sdk);
        if (!this.extra_label || "".equals(SDKIntStr2)) {
            this.ssbTargetSdk.append((CharSequence) String.valueOf(this.target_sdk));
        } else {
            this.ssbTargetSdk.append((CharSequence) String.format("%1$d (%2$s)", new Integer(this.target_sdk), SDKIntStr2));
            this.ssbTargetSdk.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), 2, this.ssbTargetSdk.length(), 33);
        }
    }

    /* JADX WARN: Code duplicated, block: B:56:0x011e  */
    private void analyseSupportedABIs(boolean z) throws ZipException {
        List fileHeaders;
        byte b;
        List list = (List) null;
        try {
            fileHeaders = new ZipFile(this.filePath).getFileHeaders();
        } catch (ZipException e) {
            e.printStackTrace();
            fileHeaders = list;
        }
        if (fileHeaders == null) {
            if (z) {
                analyseCompilingTime();
                return;
            }
            return;
        }
        this.ssbSupportedABIs = new SpannableStringBuilder();
        this.ssbSupportedABIs.append((CharSequence) "armeabi/armeabi-v7a/arm64-v8a\nx86/x86_64\nmips/mips64");
        byte b2 = (byte) 0;
        int size = fileHeaders.size();
        int i = 0;
        int i2 = 0;
        while (i < size) {
            FileHeader fileHeader = (FileHeader) fileHeaders.get(i);
            int lastModFileTime = (int) fileHeader.getLastModifiedTime();
            if (lastModFileTime <= i2) {
                lastModFileTime = i2;
            }
            this.tempStr = fileHeader.getFileName();
            if (this.tempStr.charAt(0) == 'l') {
                String[] strArrSplit = this.tempStr.split("/");
                if (strArrSplit.length < 3) {
                    b = b2;
                } else {
                    String str = strArrSplit[1];
                    if (str.equals("armeabi")) {
                        b = (byte) (b2 | 112);
                    } else if (str.equals("armeabi-v7a")) {
                        b = (byte) (b2 | 48);
                    } else if (str.equals("arm64-v8a")) {
                        b = (byte) (b2 | 16);
                    } else if (str.equals("x86")) {
                        b = (byte) (b2 | 12);
                    } else if (str.equals("x86_64")) {
                        b = (byte) (b2 | 4);
                    } else if (str.equals("mips")) {
                        b = (byte) (b2 | 3);
                    } else if (str.equals("mips64")) {
                        b = (byte) (b2 | 1);
                    } else {
                        b = b2;
                    }
                }
            } else {
                b = b2;
            }
            i++;
            b2 = b;
            i2 = lastModFileTime;
        }
        if (b2 == 0) {
            b2 = (byte) 127;
        }
        int[] iArr = {0, 7, 8, 19, 20, 29, 30, 33, 34, 40, 41, 45, 46, 52};
        for (int i3 = 0; i3 < 7; i3++) {
            if (((64 >>> i3) & b2) == 0) {
                this.ssbSupportedABIs.setSpan(new StrikethroughSpan(), iArr[i3 * 2], iArr[(i3 * 2) + 1], 33);
            }
        }
        if (z) {
            if (this.compiling_time > 1201795200019L) {
                this.compilingTimeStr = new StringBuffer().append(this.compilingTimeStr).append(ExtraUtil.convertMillisTime(this.compiling_time, "yyyy-MM-dd")).toString();
            }
        }
    }

    @TargetApi(9)
    private void analyseReqPermissions() {
        this.ssbReqPermissions = new SpannableStringBuilder();
        String[] strArr = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.BODY_SENSORS", "android.permission.CALL_PHONE", "android.permission.CAMERA", "android.permission.GET_ACCOUNTS", "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.READ_CALENDAR", "android.permission.READ_CALL_LOG", "android.permission.READ_CONTACTS", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.READ_SMS", "android.permission.RECEIVE_MMS", "android.permission.RECEIVE_SMS", "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECORD_AUDIO", "android.permission.SEND_SMS", "android.permission.USE_SIP", "android.permission.WRITE_CALENDAR", "android.permission.WRITE_CALL_LOG", "android.permission.WRITE_CONTACTS", "android.permission.WRITE_EXTERNAL_STORAGE", "com.android.voicemail.permission.ADD_VOICEMAIL"};
        String[] strArr2 = this.packageInfo.requestedPermissions;
        if (strArr2 == null || strArr2.length <= 0) {
            return;
        }
        Arrays.sort(strArr2, new ReqPermissionsComparator(this, "android.permission"));
        this.ssbReqPermissions.append((CharSequence) String.valueOf(strArr2.length));
        int length = strArr.length;
        int i = 0;
        for (String strSubstring : strArr2) {
            if (strSubstring.startsWith(this.packageName)) {
                strSubstring = strSubstring.substring(this.packageName.length());
            }
            this.ssbReqPermissions.append((CharSequence) "\n").append((CharSequence) strSubstring);
            int iSearch = ExtraUtil.search(strArr, i, length, strSubstring);
            if (iSearch >= 0) {
                this.ssbReqPermissions.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[0] : C.COLORS[0]), this.ssbReqPermissions.length() - strSubstring.length(), this.ssbReqPermissions.length(), 33);
                i = iSearch + 1;
            }
            if (this.extra_label) {
                try {
                    PermissionInfo permissionInfo = this.packageManager.getPermissionInfo(strSubstring, 0);
                    this.tempStr = permissionInfo.loadLabel(this.packageManager).toString();
                    if (!"".equals(this.tempStr) && !this.tempStr.equals(((PackageItemInfo) permissionInfo).name)) {
                        this.ssbReqPermissions.append((CharSequence) String.format(" (%s)", this.tempStr));
                        this.ssbReqPermissions.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbReqPermissions.length() - this.tempStr.length()) - 3, this.ssbReqPermissions.length(), 33);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
    }

    class ReqPermissionsComparator implements Comparator<String> {
        int len_prefix;
        String namePrefix;
        private final AppInfo this$0;

        @Override // java.util.Comparator
        public /* bridge */ int compare(String str, String str2) {
            return compare2(str, str2);
        }

        public ReqPermissionsComparator(AppInfo appInfo, String str) {
            this.this$0 = appInfo;
            this.namePrefix = "";
            this.len_prefix = 0;
            this.namePrefix = str;
            this.len_prefix = str.length();
        }

        /* JADX INFO: renamed from: compare, reason: avoid collision after fix types in other method */
        public int compare2(String str, String str2) {
            if (str.startsWith(this.namePrefix)) {
                str = str.substring(this.len_prefix);
            }
            if (str2.startsWith(this.namePrefix)) {
                str2 = str2.substring(this.len_prefix);
            }
            return str.compareTo(str2);
        }
    }

    @TargetApi(9)
    private void analysePermissions() {
        this.ssbPermissions = new SpannableStringBuilder();
        PermissionInfo[] permissionInfoArr = this.packageInfo.permissions;
        if (permissionInfoArr == null || permissionInfoArr.length <= 0) {
            return;
        }
        if (permissionInfoArr.length > 1) {
            Arrays.sort(permissionInfoArr, new PermissionsComparator(this, this.packageName));
        } else if (((PackageItemInfo) permissionInfoArr[0]).name.startsWith(this.packageName)) {
            ((PackageItemInfo) permissionInfoArr[0]).name = ((PackageItemInfo) permissionInfoArr[0]).name.substring(this.packageName.length());
        }
        this.ssbPermissions.append((CharSequence) String.valueOf(permissionInfoArr.length));
        for (PermissionInfo permissionInfo : permissionInfoArr) {
            this.ssbPermissions.append((CharSequence) "\n").append((CharSequence) ((PackageItemInfo) permissionInfo).name);
            if (this.extra_label) {
                this.tempStr = permissionInfo.loadLabel(this.packageManager).toString();
                if (!"".equals(this.tempStr) && !this.tempStr.equals(((PackageItemInfo) permissionInfo).name)) {
                    this.ssbPermissions.append((CharSequence) String.format(" (%s)", this.tempStr));
                    this.ssbPermissions.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbPermissions.length() - this.tempStr.length()) - 3, this.ssbPermissions.length(), 33);
                }
            }
        }
    }

    class PermissionsComparator implements Comparator<PermissionInfo> {
        int len_prefix;
        String name_prefix;
        private final AppInfo this$0;

        @Override // java.util.Comparator
        public /* bridge */ int compare(PermissionInfo permissionInfo, PermissionInfo permissionInfo2) {
            return compare2(permissionInfo, permissionInfo2);
        }

        public PermissionsComparator(AppInfo appInfo, String str) {
            this.this$0 = appInfo;
            this.name_prefix = "";
            this.len_prefix = 0;
            this.name_prefix = str;
            this.len_prefix = str.length();
        }

        /* JADX INFO: renamed from: compare, reason: avoid collision after fix types in other method */
        public int compare2(PermissionInfo permissionInfo, PermissionInfo permissionInfo2) {
            if (((PackageItemInfo) permissionInfo).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) permissionInfo).name = ((PackageItemInfo) permissionInfo).name.substring(this.len_prefix);
            }
            if (((PackageItemInfo) permissionInfo2).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) permissionInfo2).name = ((PackageItemInfo) permissionInfo2).name.substring(this.len_prefix);
            }
            return ((PackageItemInfo) permissionInfo).name.compareTo(((PackageItemInfo) permissionInfo2).name);
        }
    }

    @TargetApi(23)
    private void analyseFlags() {
        this.buffer.setLength(0);
        int[] iArr = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, InternalZipConstants.BUFF_SIZE, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, Integer.MIN_VALUE};
        String[] strArr = {"FLAG_SYSTEM", "FLAG_DEBUGGABLE", "FLAG_HAS_CODE", "FLAG_PERSISTENT", "FLAG_FACTORY_TEST", "FLAG_ALLOW_TASK_REPARENTING", "FLAG_ALLOW_CLEAR_USER_DATA", "FLAG_UPDATED_SYSTEM_APP", "FLAG_TEST_ONLY", "FLAG_SUPPORTS_SMALL_SCREENS", "FLAG_SUPPORTS_NORMAL_SCREENS", "FLAG_SUPPORTS_LARGE_SCREENS", "FLAG_RESIZEABLE_FOR_SCREENS", "FLAG_SUPPORTS_SCREEN_DENSITIES", "FLAG_VM_SAFE_MODE", "FLAG_ALLOW_BACKUP", "FLAG_KILL_AFTER_RESTORE", "FLAG_RESTORE_ANY_VERSION", "FLAG_EXTERNAL_STORAGE", "FLAG_SUPPORTS_XLARGE_SCREENS", "FLAG_LARGE_HEAP", "FLAG_STOPPED", "FLAG_SUPPORTS_RTL", "FLAG_INSTALLED", "FLAG_IS_DATA_ONLY", "FLAG_IS_GAME", "FLAG_FULL_BACKUP_ONLY", "FLAG_USES_CLEARTEXT_TRAFFIC", "FLAG_MULTIARCH"};
        this.flags = this.applicationInfo.flags;
        int length = iArr.length;
        for (int i = 0; i < length; i++) {
            if ((this.flags & iArr[i]) == iArr[i]) {
                this.buffer.append(strArr[i]).append("\n");
            }
        }
        if (this.buffer.length() > 0) {
            this.buffer.setLength(this.buffer.length() - 1);
            this.flagsStr = this.buffer.toString();
        }
    }

    private void analyseActivities() {
        this.ssbActivities = new SpannableStringBuilder();
        ActivityInfo[] activityInfoArr = this.packageInfo.activities;
        if (activityInfoArr == null || activityInfoArr.length <= 0) {
            return;
        }
        if (activityInfoArr.length > 1) {
            Arrays.sort(activityInfoArr, new ActivitiesComparator(this, this.packageName));
        } else if (activityInfoArr[0] != null && ((PackageItemInfo) activityInfoArr[0]).name.startsWith(this.packageName)) {
            ((PackageItemInfo) activityInfoArr[0]).name = ((PackageItemInfo) activityInfoArr[0]).name.substring(this.packageName.length());
        }
        this.ssbActivities.append((CharSequence) String.valueOf(activityInfoArr.length));
        for (ActivityInfo activityInfo : activityInfoArr) {
            if (activityInfo != null) {
                this.ssbActivities.append((CharSequence) "\n").append((CharSequence) ((PackageItemInfo) activityInfo).name);
                if (((ComponentInfo) activityInfo).exported) {
                    this.ssbActivities.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[1] : C.COLORS[1]), this.ssbActivities.length() - ((PackageItemInfo) activityInfo).name.length(), this.ssbActivities.length(), 33);
                }
                if (this.extra_label) {
                    this.tempStr = activityInfo.loadLabel(this.packageManager).toString();
                    if (!"".equals(this.tempStr) && !this.tempStr.equals(this.label)) {
                        this.ssbActivities.append((CharSequence) String.format(" (%s)", this.tempStr));
                        this.ssbActivities.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbActivities.length() - this.tempStr.length()) - 3, this.ssbActivities.length(), 33);
                    }
                }
            }
        }
    }

    class ActivitiesComparator implements Comparator<ActivityInfo> {
        int len_prefix;
        String name_prefix;
        private final AppInfo this$0;

        @Override // java.util.Comparator
        public /* bridge */ int compare(ActivityInfo activityInfo, ActivityInfo activityInfo2) {
            return compare2(activityInfo, activityInfo2);
        }

        public ActivitiesComparator(AppInfo appInfo, String str) {
            this.this$0 = appInfo;
            this.name_prefix = "";
            this.len_prefix = 0;
            this.name_prefix = str;
            this.len_prefix = str.length();
        }

        /* JADX INFO: renamed from: compare, reason: avoid collision after fix types in other method */
        public int compare2(ActivityInfo activityInfo, ActivityInfo activityInfo2) {
            if (activityInfo == null || activityInfo2 == null) {
                return 0;
            }
            if (((PackageItemInfo) activityInfo).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) activityInfo).name = ((PackageItemInfo) activityInfo).name.substring(this.len_prefix);
            }
            if (((PackageItemInfo) activityInfo2).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) activityInfo2).name = ((PackageItemInfo) activityInfo2).name.substring(this.len_prefix);
            }
            return ((PackageItemInfo) activityInfo).name.compareTo(((PackageItemInfo) activityInfo2).name);
        }
    }

    private void analyseServices() {
        this.ssbServices = new SpannableStringBuilder();
        ServiceInfo[] serviceInfoArr = this.packageInfo.services;
        if (serviceInfoArr == null || serviceInfoArr.length <= 0) {
            return;
        }
        if (serviceInfoArr.length > 1) {
            Arrays.sort(serviceInfoArr, new ServicesComparator(this, this.packageName));
        } else if (serviceInfoArr[0] != null && ((PackageItemInfo) serviceInfoArr[0]).name.startsWith(this.packageName)) {
            ((PackageItemInfo) serviceInfoArr[0]).name = ((PackageItemInfo) serviceInfoArr[0]).name.substring(this.packageName.length());
        }
        this.ssbServices.append((CharSequence) String.valueOf(serviceInfoArr.length));
        for (ServiceInfo serviceInfo : serviceInfoArr) {
            if (serviceInfo != null) {
                this.ssbServices.append((CharSequence) "\n").append((CharSequence) ((PackageItemInfo) serviceInfo).name);
                if (((ComponentInfo) serviceInfo).exported) {
                    this.ssbServices.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[1] : C.COLORS[1]), this.ssbServices.length() - ((PackageItemInfo) serviceInfo).name.length(), this.ssbServices.length(), 33);
                }
                if (this.extra_label) {
                    this.tempStr = serviceInfo.loadLabel(this.packageManager).toString();
                    if (!"".equals(this.tempStr) && !this.tempStr.equals(this.label)) {
                        this.ssbServices.append((CharSequence) String.format(" (%s)", this.tempStr));
                        this.ssbServices.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbServices.length() - this.tempStr.length()) - 3, this.ssbServices.length(), 33);
                    }
                }
            }
        }
    }

    class ServicesComparator implements Comparator<ServiceInfo> {
        int len_prefix;
        String name_prefix;
        private final AppInfo this$0;

        @Override // java.util.Comparator
        public /* bridge */ int compare(ServiceInfo serviceInfo, ServiceInfo serviceInfo2) {
            return compare2(serviceInfo, serviceInfo2);
        }

        public ServicesComparator(AppInfo appInfo, String str) {
            this.this$0 = appInfo;
            this.name_prefix = "";
            this.len_prefix = 0;
            this.name_prefix = str;
            this.len_prefix = str.length();
        }

        /* JADX INFO: renamed from: compare, reason: avoid collision after fix types in other method */
        public int compare2(ServiceInfo serviceInfo, ServiceInfo serviceInfo2) {
            if (serviceInfo == null || serviceInfo2 == null) {
                return 0;
            }
            if (((PackageItemInfo) serviceInfo).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) serviceInfo).name = ((PackageItemInfo) serviceInfo).name.substring(this.len_prefix);
            }
            if (((PackageItemInfo) serviceInfo2).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) serviceInfo2).name = ((PackageItemInfo) serviceInfo2).name.substring(this.len_prefix);
            }
            return ((PackageItemInfo) serviceInfo).name.compareTo(((PackageItemInfo) serviceInfo2).name);
        }
    }

    private void analyseProviders() {
        this.ssbProviders = new SpannableStringBuilder();
        ProviderInfo[] providerInfoArr = this.packageInfo.providers;
        if (providerInfoArr == null || providerInfoArr.length <= 0) {
            return;
        }
        if (providerInfoArr.length > 1) {
            Arrays.sort(providerInfoArr, new ProvidersComparator(this, this.packageName));
        } else if (providerInfoArr[0] != null && ((PackageItemInfo) providerInfoArr[0]).name.startsWith(this.packageName)) {
            ((PackageItemInfo) providerInfoArr[0]).name = ((PackageItemInfo) providerInfoArr[0]).name.substring(this.packageName.length());
        }
        this.ssbProviders.append((CharSequence) String.valueOf(providerInfoArr.length));
        for (ProviderInfo providerInfo : providerInfoArr) {
            if (providerInfo != null) {
                this.ssbProviders.append((CharSequence) "\n").append((CharSequence) ((PackageItemInfo) providerInfo).name);
                if (((ComponentInfo) providerInfo).exported) {
                    this.ssbProviders.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[1] : C.COLORS[1]), this.ssbProviders.length() - ((PackageItemInfo) providerInfo).name.length(), this.ssbProviders.length(), 33);
                }
                if (this.extra_label) {
                    this.tempStr = providerInfo.loadLabel(this.packageManager).toString();
                    if (!"".equals(this.tempStr) && !this.tempStr.equals(this.label)) {
                        this.ssbProviders.append((CharSequence) String.format(" (%s)", this.tempStr));
                        this.ssbProviders.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbProviders.length() - this.tempStr.length()) - 3, this.ssbProviders.length(), 33);
                    }
                }
            }
        }
    }

    class ProvidersComparator implements Comparator<ProviderInfo> {
        int len_prefix;
        String name_prefix;
        private final AppInfo this$0;

        @Override // java.util.Comparator
        public /* bridge */ int compare(ProviderInfo providerInfo, ProviderInfo providerInfo2) {
            return compare2(providerInfo, providerInfo2);
        }

        public ProvidersComparator(AppInfo appInfo, String str) {
            this.this$0 = appInfo;
            this.name_prefix = "";
            this.len_prefix = 0;
            this.name_prefix = str;
            this.len_prefix = str.length();
        }

        /* JADX INFO: renamed from: compare, reason: avoid collision after fix types in other method */
        public int compare2(ProviderInfo providerInfo, ProviderInfo providerInfo2) {
            if (providerInfo == null || providerInfo2 == null) {
                return 0;
            }
            if (((PackageItemInfo) providerInfo).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) providerInfo).name = ((PackageItemInfo) providerInfo).name.substring(this.len_prefix);
            }
            if (((PackageItemInfo) providerInfo2).name.startsWith(this.name_prefix)) {
                ((PackageItemInfo) providerInfo2).name = ((PackageItemInfo) providerInfo2).name.substring(this.len_prefix);
            }
            return ((PackageItemInfo) providerInfo).name.compareTo(((PackageItemInfo) providerInfo2).name);
        }
    }

    private void analyseReceivers() {
        this.ssbReceivers = new SpannableStringBuilder();
        ActivityInfo[] activityInfoArr = this.packageInfo.receivers;
        if (activityInfoArr == null || activityInfoArr.length <= 0) {
            return;
        }
        if (activityInfoArr.length > 1) {
            Arrays.sort(activityInfoArr, new ActivitiesComparator(this, this.packageName));
        } else if (activityInfoArr[0] != null && ((PackageItemInfo) activityInfoArr[0]).name.startsWith(this.packageName)) {
            ((PackageItemInfo) activityInfoArr[0]).name = ((PackageItemInfo) activityInfoArr[0]).name.substring(this.packageName.length());
        }
        this.ssbReceivers.append((CharSequence) String.valueOf(activityInfoArr.length));
        for (ActivityInfo activityInfo : activityInfoArr) {
            if (activityInfo != null) {
                this.ssbReceivers.append((CharSequence) "\n").append((CharSequence) ((PackageItemInfo) activityInfo).name);
                if (((ComponentInfo) activityInfo).exported) {
                    this.ssbReceivers.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[1] : C.COLORS[1]), this.ssbReceivers.length() - ((PackageItemInfo) activityInfo).name.length(), this.ssbReceivers.length(), 33);
                }
                if (this.extra_label) {
                    this.tempStr = activityInfo.loadLabel(this.packageManager).toString();
                    if (!"".equals(this.tempStr) && !this.tempStr.equals(this.label)) {
                        this.ssbReceivers.append((CharSequence) String.format(" (%s)", this.tempStr));
                        this.ssbReceivers.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[2] : C.COLORS[2]), (this.ssbReceivers.length() - this.tempStr.length()) - 3, this.ssbReceivers.length(), 33);
                    }
                }
            }
        }
    }

    private void analyseSignatures(Principal principal, int i) {
        if (principal != null) {
            String string = principal.toString();
            int iHashCode = string.hashCode();
            this.ssbSignatures = new SpannableStringBuilder();
            this.buffer.setLength(0);
            this.buffer.append(string.replaceAll(", ", "\n"));
            int iIndexOf = 0;
            while (iIndexOf >= 0) {
                int iIndexOf2 = this.buffer.indexOf("\n", iIndexOf);
                if (iIndexOf2 < 0) {
                    break;
                }
                if (!this.buffer.substring(iIndexOf, iIndexOf2).contains("=")) {
                    this.buffer.replace(iIndexOf - 1, iIndexOf, ", ");
                }
                iIndexOf = this.buffer.indexOf("\n", iIndexOf) + 1;
            }
            this.ssbSignatures.append((CharSequence) this.buffer);
            if (iHashCode == 1717115081) {
                this.ssbSignatures.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[0] : C.COLORS[0]), 0, this.ssbSignatures.length(), 33);
            } else {
                for (int i2 : C.DEBUG_SIGNS_HASH) {
                    if (i == i2) {
                        this.ssbSignatures.setSpan(new ForegroundColorSpan(this.light_theme ? C.COLORS_LIGHT[0] : C.COLORS[0]), 0, this.ssbSignatures.length(), 33);
                        break;
                    }
                }
            }
            this.signatures = this.buffer.toString();
        }
    }

    @TargetApi(21)
    @SuppressWarnings("deprecation")
    public Drawable getLauncherIcon(boolean z) {
        if (this.applicationInfo == null) {
            return (Drawable) null;
        }
        Drawable drawableForDensity = (Drawable) null;
        if (C.SDK >= 15 && z) {
            try {
                Resources resourcesForApplication = this.packageManager.getResourcesForApplication(this.applicationInfo);
                drawableForDensity = C.SDK >= 21 ? resourcesForApplication.getDrawableForDensity(((PackageItemInfo) this.applicationInfo).icon, 640, (Resources.Theme) null) : resourcesForApplication.getDrawableForDensity(((PackageItemInfo) this.applicationInfo).icon, 640);
                return drawableForDensity;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return drawableForDensity;
            }
        }
        return this.packageManager.getApplicationIcon(this.applicationInfo);
    }

    public String[] getInfosArray() {
        String[] strArr = new String[28];
        strArr[0] = this.firstInstallStr;
        strArr[1] = this.lastUpdateStr;
        strArr[2] = (String) null;
        strArr[3] = (String) null;
        strArr[4] = (String) null;
        strArr[5] = this.fileSizeStr;
        strArr[6] = (String) null;
        strArr[7] = this.packageName;
        strArr[8] = this.appName;
        strArr[9] = this.verName;
        strArr[10] = String.valueOf(this.ver_code);
        strArr[11] = this.compilingTimeStr;
        strArr[12] = (String) null;
        strArr[13] = (String) null;
        strArr[14] = (String) null;
        strArr[15] = (String) null;
        strArr[16] = (String) null;
        strArr[17] = this.flagsStr;
        strArr[18] = this.launcherActivity;
        strArr[19] = (String) null;
        strArr[20] = (String) null;
        strArr[21] = (String) null;
        strArr[22] = (String) null;
        strArr[23] = (String) null;
        strArr[24] = this.certificateFingerprints;
        strArr[25] = this.certificateStartStr;
        strArr[26] = this.certificateEndStr;
        strArr[27] = (String) null;
        boolean[] allBits = ExtraUtil.getAllBits(this.hide_flags, C.ENUM_FLAGS.values().length);
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            if (!allBits[i]) {
                strArr[i] = (String) null;
            }
        }
        return strArr;
    }

    public SpannableStringBuilder[] getSpanInfosArray() {
        SpannableStringBuilder[] spannableStringBuilderArr = new SpannableStringBuilder[28];
        spannableStringBuilderArr[0] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[1] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[2] = this.ssbInstaller;
        spannableStringBuilderArr[3] = this.ssbFilePath;
        spannableStringBuilderArr[4] = this.ssbFileName;
        spannableStringBuilderArr[5] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[6] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[7] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[8] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[9] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[10] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[11] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[12] = this.ssbMinSdk;
        spannableStringBuilderArr[13] = this.ssbTargetSdk;
        spannableStringBuilderArr[14] = this.ssbSupportedABIs;
        spannableStringBuilderArr[15] = this.ssbReqPermissions;
        spannableStringBuilderArr[16] = this.ssbPermissions;
        spannableStringBuilderArr[17] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[18] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[19] = this.ssbActivities;
        spannableStringBuilderArr[20] = this.ssbServices;
        spannableStringBuilderArr[21] = this.ssbProviders;
        spannableStringBuilderArr[22] = this.ssbReceivers;
        spannableStringBuilderArr[23] = this.ssbSignatures;
        spannableStringBuilderArr[24] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[25] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[26] = (SpannableStringBuilder) null;
        spannableStringBuilderArr[27] = this.ssbIconSize;
        boolean[] allBits = ExtraUtil.getAllBits(this.hide_flags, C.ENUM_FLAGS.values().length);
        int length = spannableStringBuilderArr.length;
        for (int i = 0; i < length; i++) {
            if (!allBits[i]) {
                spannableStringBuilderArr[i] = (SpannableStringBuilder) null;
            }
        }
        return spannableStringBuilderArr;
    }

    public void updateFileName(File file) {
        if (file != null && file.exists()) {
            this.applicationInfo.sourceDir = file.getPath();
            this.applicationInfo.publicSourceDir = file.getPath();
            analyseFileAttributes();
        }
    }
}
