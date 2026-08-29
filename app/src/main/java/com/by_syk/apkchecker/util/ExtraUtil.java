package com.by_syk.apkchecker.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.by_syk.apkchecker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExtraUtil {

    public static String convertMillisTime(long j, String str) {
        if (j <= 0) {
            return "";
        }

        try {
            return new SimpleDateFormat(str).format(new Date(j));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String convertMillisTime(long j) {
        return convertMillisTime(j, "yyyy-MM-dd HH:mm:ss");
    }

    @SuppressWarnings("unchecked")
    public static PackageInfo getPackageArchiveInfo(String str, int i) {
        try {
            Class<?> cls = Class.forName("android.content.pm.PackageParser");

            Class<?> packageClass = null;

            for (Class<?> innerClass : cls.getDeclaredClasses()) {
                if ("android.content.pm.PackageParser$Package"
                        .equals(innerClass.getName())) {
                    packageClass = innerClass;
                    break;
                }
            }

            if (packageClass == null) {
                return null;
            }

            Constructor<?> constructor =
                    cls.getConstructor(String.class);

            Method parsePackage = cls.getDeclaredMethod(
                    "parsePackage",
                    File.class,
                    String.class,
                    DisplayMetrics.class,
                    Integer.TYPE
            );

            Method collectCertificates = cls.getDeclaredMethod(
                    "collectCertificates",
                    packageClass,
                    Integer.TYPE
            );

            Method generatePackageInfo = cls.getDeclaredMethod(
                    "generatePackageInfo",
                    packageClass,
                    int[].class,
                    Integer.TYPE,
                    Long.TYPE,
                    Long.TYPE
            );

            constructor.setAccessible(true);
            parsePackage.setAccessible(true);
            collectCertificates.setAccessible(true);
            generatePackageInfo.setAccessible(true);

            Object parser = constructor.newInstance(str);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            displayMetrics.setToDefaults();

            Object pkg = parsePackage.invoke(
                    parser,
                    new File(str),
                    str,
                    displayMetrics,
                    0
            );

            if (pkg == null) {
                return null;
            }

            if ((i & 64) != 0) {
                collectCertificates.invoke(parser, pkg, 0);
            }

            return (PackageInfo) generatePackageInfo.invoke(
                    null,
                    pkg,
                    null,
                    i,
                    0L,
                    0L
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PackageInfo getPackageInfoUninstalled(
            Context context,
            File file,
            int i) {

        if (file == null || !file.isFile()) {
            return null;
        }

        int flags = 0;

        if (getBitsOR(
                i,
                C.ENUM_FLAGS.RP.ordinal(),
                C.ENUM_FLAGS.P.ordinal())) {
            flags |= 4096;
        }

        if (getBit(i, C.ENUM_FLAGS.A.ordinal())) {
            flags |= 1;
        }

        if (getBit(i, C.ENUM_FLAGS.S.ordinal())) {
            flags |= 4;
        }

        if (getBit(i, C.ENUM_FLAGS.CP.ordinal())) {
            flags |= 8;
        }

        if (getBit(i, C.ENUM_FLAGS.BR.ordinal())) {
            flags |= 2;
        }

        if (getBitsOR(
                i,
                C.ENUM_FLAGS.SI.ordinal(),
                C.ENUM_FLAGS.CF.ordinal(),
                C.ENUM_FLAGS.CS.ordinal(),
                C.ENUM_FLAGS.CE.ordinal())) {
            flags |= 64;
        }

        PackageInfo packageInfo;

        if (C.SDK >= 14) {
            packageInfo = context.getPackageManager()
                    .getPackageArchiveInfo(file.getPath(), flags);
        } else {
            packageInfo = getPackageArchiveInfo(file.getPath(), flags);
        }

        if (packageInfo == null) {
            int fallbackFlags = flags & (~64);

            if (C.SDK >= 14) {
                packageInfo = context.getPackageManager()
                        .getPackageArchiveInfo(
                                file.getPath(),
                                fallbackFlags
                        );
            } else {
                packageInfo = getPackageArchiveInfo(
                        file.getPath(),
                        fallbackFlags
                );
            }
        }

        if (packageInfo != null && packageInfo.applicationInfo != null) {
            packageInfo.applicationInfo.sourceDir = file.getPath();
            packageInfo.applicationInfo.publicSourceDir = file.getPath();
        }

        return packageInfo;
    }

    public static PackageInfo getPackageInfoUninstalled(
            Context context,
            File file) {

        if (file == null || !file.isFile()) {
            return null;
        }

        PackageInfo packageInfo;

        if (C.SDK >= 14) {
            packageInfo = context.getPackageManager()
                    .getPackageArchiveInfo(file.getPath(), 0);
        } else {
            packageInfo = getPackageArchiveInfo(file.getPath(), 0);
        }

        if (packageInfo != null && packageInfo.applicationInfo != null) {
            packageInfo.applicationInfo.sourceDir = file.getPath();
            packageInfo.applicationInfo.publicSourceDir = file.getPath();
        }

        return packageInfo;
    }

    public static PackageInfo getPackageInfoInstalled(
            Context context,
            String str,
            int i) {

        if (TextUtils.isEmpty(str)) {
            return null;
        }

        int flags = 0;

        if (getBitsOR(
                i,
                C.ENUM_FLAGS.RP.ordinal(),
                C.ENUM_FLAGS.P.ordinal())) {
            flags |= 4096;
        }

        if (getBit(i, C.ENUM_FLAGS.A.ordinal())) {
            flags |= 1;
        }

        if (getBit(i, C.ENUM_FLAGS.S.ordinal())) {
            flags |= 4;
        }

        if (getBit(i, C.ENUM_FLAGS.CP.ordinal())) {
            flags |= 8;
        }

        if (getBit(i, C.ENUM_FLAGS.BR.ordinal())) {
            flags |= 2;
        }

        if (getBitsOR(
                i,
                C.ENUM_FLAGS.SI.ordinal(),
                C.ENUM_FLAGS.CF.ordinal(),
                C.ENUM_FLAGS.CS.ordinal(),
                C.ENUM_FLAGS.CE.ordinal())) {
            flags |= 64;
        }

        try {
            return context.getPackageManager()
                    .getPackageInfo(str, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkPackageExists(
            Context context,
            String str) {

        if (TextUtils.isEmpty(str)) {
            return false;
        }

        try {
            context.getPackageManager().getPackageInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static File renameApkFileName(
            String str,
            String str2,
            String str3,
            String str4,
            int i,
            String str5,
            boolean z) {

        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str5)) {
            return null;
        }

        File file = new File(str);

        if (!file.exists()) {
            return null;
        }

        if (TextUtils.isEmpty(str2)) {
            str2 = "NULL";
        }

        if (TextUtils.isEmpty(str3)) {
            str3 = "NULL";
        }

        if (TextUtils.isEmpty(str4)) {
            str4 = "NULL";
        }

        File parent = file.getParentFile();

        if (parent == null) {
            return null;
        }

        String fileName = String.format(
                str5,
                str2,
                str3,
                str4,
                i
        ) + ".apk";

        File file2 = new File(parent, fileName);

        if (z) {
            file2 = getUniqueApkFile(file, file2);

            if (file2 == null) {
                return null;
            }
        } else if (file.equals(file2) || file2.exists()) {
            return null;
        }

        if (!file.renameTo(file2)) {
            return null;
        }

        return file2;
    }

    public static File renameApkFileName(
            String str,
            String str2,
            String str3,
            String str4,
            int i,
            String str5) {

        return renameApkFileName(
                str,
                str2,
                str3,
                str4,
                i,
                str5,
                false
        );
    }

    private static File getUniqueApkFile(
            File file,
            File file2) {

        if (file == null || file2 == null) {
            return null;
        }

        String name = file2.getName();

        int dot = name.lastIndexOf('.');

        if (dot > 0) {
            name = name.substring(0, dot);
        }

        int i = 1;

        while (file2.exists()) {

            if (file.equals(file2)) {
                return null;
            }

            file2 = new File(
                    file.getParent(),
                    String.format(
                            "%s-%d.apk",
                            name,
                            i
                    )
            );

            i++;
        }

        return file2;
    }

    public static void undoRenameFile(File[][] fileArr) {

        if (fileArr == null) {
            return;
        }

        for (File[] files : fileArr) {

            if (files != null
                    && files.length >= 2
                    && files[0] != null
                    && files[1] != null) {

                files[1].renameTo(files[0]);
            }
        }
    }

    public static boolean saveIcon(
            Context context,
            Drawable drawable,
            String str,
            String str2,
            String str3) throws Throwable {

        if (drawable == null) {
            return false;
        }

        File externalCacheDir = context.getExternalCacheDir();

        if (externalCacheDir == null) {
            return false;
        }

        File file = new File(
                externalCacheDir,
                C.TEMP_FILE_NAME_ICON
        );

        FileOutputStream outputStream = null;
        boolean compressed = false;

        try {

            outputStream = new FileOutputStream(file);

            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            if (bitmap != null) {
                compressed = bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream
                );
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();
            compressed = false;

        } finally {

            if (outputStream != null) {

                try {
                    outputStream.flush();
                } catch (IOException ignored) {
                }

                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (!compressed || !file.exists()) {
            return false;
        }

        File picturesDirectory =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                );

        if (!picturesDirectory.exists()) {
            picturesDirectory.mkdirs();
        }

        if (TextUtils.isEmpty(str)) {
            str = "NULL";
        }

        if (TextUtils.isEmpty(str2)) {
            str2 = "NULL";
        }

        if (TextUtils.isEmpty(str3)) {
            str3 = "%1$s_%2$s";
        }

        String iconName;

        try {
            iconName = String.format(
                    str3,
                    str,
                    str2
            );
        } catch (Exception e) {
            iconName = str + "_" + str2;
        }

        File uniquePngFile = getUniquePngFile(
                picturesDirectory,
                iconName,
                ".png",
                file.length()
        );

        if (uniquePngFile == null) {
            return false;
        }

        return fileChannelCopy(
                file,
                uniquePngFile
        );
    }

    private static File getUniquePngFile(
            File file,
            String str,
            String str2,
            long j) {

        if (file == null
                || !file.exists()
                || TextUtils.isEmpty(str)) {
            return null;
        }

        File file2 = new File(
                file,
                str + str2
        );

        int i = 1;

        while (file2.exists()) {

            if (file2.length() == j) {
                return null;
            }

            file2 = new File(
                    file,
                    String.format(
                            "%1$s_%2$d%3$s",
                            str,
                            i,
                            str2
                    )
            );

            i++;
        }

        return file2;
    }

    public static boolean fileChannelCopy(
            File source,
            File destination) throws Throwable {

        if (source == null
                || !source.exists()
                || destination == null) {
            return false;
        }

        File parent = destination.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;

        try {

            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(destination);

            inputChannel = inputStream.getChannel();
            outputChannel = outputStream.getChannel();

            inputChannel.transferTo(
                    0,
                    inputChannel.size(),
                    outputChannel
            );

            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {

            if (inputChannel != null) {
                try {
                    inputChannel.close();
                } catch (IOException ignored) {
                }
            }

            if (outputChannel != null) {
                try {
                    outputChannel.close();
                } catch (IOException ignored) {
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean copyFile(
            InputStream inputStream,
            File file) throws Throwable {

        if (inputStream == null || file == null) {
            return false;
        }

        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        FileOutputStream outputStream = null;

        try {

            outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[8192];

            int count;

            while ((count = inputStream.read(buffer)) != -1) {

                if (count > 0) {
                    outputStream.write(
                            buffer,
                            0,
                            count
                    );
                }
            }

            outputStream.flush();

            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {

            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (IOException ignored) {
                }

                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String getCertificateSignatures(
            byte[] bArr,
            String str) {

        StringBuilder sb = new StringBuilder();

        if (bArr == null || str == null) {
            return "";
        }

        try {

            MessageDigest messageDigest =
                    MessageDigest.getInstance(str);

            messageDigest.update(bArr);

            byte[] digest = messageDigest.digest();

            for (byte b : digest) {

                String hexString =
                        Integer.toHexString(b & 255);

                while (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }

                sb.append(
                        hexString.toUpperCase()
                ).append(":");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return sb.toString();
    }

    public static List<String> getAllInstalledPackageNames(
            Context context,
            boolean z) {

        ArrayList<String> result =
                new ArrayList<>();

        List<ApplicationInfo> applications =
                context.getPackageManager()
                        .getInstalledApplications(0);

        if (applications == null) {
            return result;
        }

        for (ApplicationInfo applicationInfo : applications) {

            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && !z) {
                continue;
            }

            result.add(
                    applicationInfo.packageName
            );
        }

        Collections.sort(result);

        return result;
    }

    public static List<String> getAllExportedClassNames(
            Context context,
            String str) {

        ArrayList<String> result =
                new ArrayList<>();

        if (TextUtils.isEmpty(str)) {
            return result;
        }

        try {

            ActivityInfo[] activities =
                    context.getPackageManager()
                            .getPackageInfo(
                                    str,
                                    PackageManager.GET_ACTIVITIES
                            ).activities;

            if (activities != null) {

                for (ActivityInfo activityInfo : activities) {

                    if (activityInfo.exported) {

                        String name = activityInfo.name;

                        if (name != null) {
                            result.add(
                                    name.replace(str, "")
                            );
                        }
                    }
                }
            }

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }

        Collections.sort(result);

        return result;
    }

    public static int guessInstallerActivityPos(
            List<String> list) {

        if (list == null || list.isEmpty()) {
            return 0;
        }

        int size = list.size();

        for (int i = 0; i < size; i++) {

            String value = list.get(i);

            if (value == null) {
                continue;
            }

            String lowerCase =
                    value.toLowerCase();

            if (lowerCase.contains("install")
                    && !lowerCase.contains("uninstall")) {

                return i;
            }
        }

        return 0;
    }

    public static File tryLastRenamedFile(
            String str,
            String str2,
            long j,
            File file) {

        if (str != null
                && str2 != null
                && j > 0
                && file != null
                && file.getPath().equals(str)) {

            File file2 = new File(str2);

            if (file2.exists()
                    && file2.length() == j) {

                return file2;
            }

            File parent = file2.getParentFile();

            if (parent != null && parent.exists()) {

                File[] files = parent.listFiles();

                if (files != null) {

                    for (File candidate : files) {

                        if (!candidate.isDirectory()
                                && candidate.getName()
                                .endsWith(".apk")
                                && candidate.length() == j) {

                            return candidate;
                        }
                    }
                }
            }
        }

        return file;
    }

    public static boolean getBit(int i, int i2) {
        return ((1 << i2) & i) != 0;
    }

    public static boolean getBitsOR(
            int i,
            int... iArr) {

        if (iArr == null) {
            return false;
        }

        for (int bit : iArr) {

            if (getBit(i, bit)) {
                return true;
            }
        }

        return false;
    }

    public static boolean[] getAllBits(
            int i,
            int i2) {

        if (i2 <= 0) {
            return new boolean[0];
        }

        if (i2 > 32) {
            i2 = 32;
        }

        boolean[] result =
                new boolean[i2];

        for (int index = 0;
             index < i2;
             index++) {

            result[index] =
                    ((1 << index) & i) != 0;
        }

        return result;
    }

    public static int writeBits(
            boolean[] zArr) {

        if (zArr == null) {
            return 0;
        }

        int result = 0;

        int length =
                Math.min(zArr.length, 32);

        for (int i = 0; i < length; i++) {

            if (zArr[i]) {
                result |= (1 << i);
            }
        }

        return result;
    }

    @TargetApi(9)
    public static int search(
            String[] strArr,
            int i,
            int i2,
            String str) {

        if (strArr == null || str == null) {
            return -1;
        }

        if (C.SDK >= 9) {
            return Arrays.binarySearch(
                    strArr,
                    i,
                    i2,
                    str
            );
        }

        for (int index = i; index < i2; index++) {

            if (str.equals(strArr[index])) {
                return index;
            }
        }

        return -1;
    }

    public static String removeSpaceInTag(
            String str) {

        if (str == null) {
            return "";
        }

        return str
                .replace(" \n", " ")
                .replace("\u3000\n", "");
    }

    public static void hideComponent(
            Context context,
            ComponentName componentName,
            boolean z) {

        context.getPackageManager()
                .setComponentEnabledSetting(
                        componentName,
                        z ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
    }

    public static String getHelpInfo(
            Context context) {

        return String.format(
                context.getString(R.string.dia_help_desc),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_first_install
                        )
                ),
                context.getString(
                        R.string.tag_help_first_install
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_last_update
                        )
                ),
                context.getString(
                        R.string.tag_help_last_update
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_installed_from
                        )
                ),
                context.getString(
                        R.string.tag_help_installed_from
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_file_path
                        )
                ),
                context.getString(
                        R.string.tag_help_file_path
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_file_name
                        )
                ),
                context.getString(
                        R.string.tag_help_file_name
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_file_size
                        )
                ),
                context.getString(
                        R.string.tag_help_file_size
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_launcher_icon
                        )
                ),
                context.getString(
                        R.string.tag_help_launcher_icon
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_icon_size
                        )
                ),
                context.getString(
                        R.string.tag_help_icon_size
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_package_name
                        )
                ),
                context.getString(
                        R.string.tag_help_package_name
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_app_name
                        )
                ),
                context.getString(
                        R.string.tag_help_app_name
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_ver_name
                        )
                ),
                context.getString(
                        R.string.tag_help_ver_name
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_ver_code
                        )
                ),
                context.getString(
                        R.string.tag_help_ver_code
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_compiling_time
                        )
                ),
                context.getString(
                        R.string.tag_help_compiling_time
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_min_sdk
                        )
                ),
                context.getString(
                        R.string.tag_help_min_sdk
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_target_sdk
                        )
                ),
                context.getString(
                        R.string.tag_help_target_sdk
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_supported_abis
                        )
                ),
                context.getString(
                        R.string.tag_help_supported_abis
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_req_permissions
                        )
                ),
                context.getString(
                        R.string.tag_help_req_permissions
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_permissions
                        )
                ),
                context.getString(
                        R.string.tag_help_permissions
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_flags
                        )
                ),
                context.getString(
                        R.string.tag_help_flags
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_launcher_activity
                        )
                ),
                context.getString(
                        R.string.tag_help_launcher_activity
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_activities
                        )
                ),
                context.getString(
                        R.string.tag_help_activities
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_services
                        )
                ),
                context.getString(
                        R.string.tag_help_services
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_providers
                        )
                ),
                context.getString(
                        R.string.tag_help_providers
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_receivers
                        )
                ),
                context.getString(
                        R.string.tag_help_receivers
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_signatures
                        )
                ),
                context.getString(
                        R.string.tag_help_signatures
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_certificate_fingerprints
                        )
                ),
                context.getString(
                        R.string.tag_help_certificate_fingerprints
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_certificate_start
                        )
                ),
                context.getString(
                        R.string.tag_help_certificate_start
                ),

                removeSpaceInTag(
                        context.getString(
                                R.string.tag_certificate_end
                        )
                ),
                context.getString(
                        R.string.tag_help_certificate_end
                ),

                context.getString(
                        R.string.copyright
                )
        );
    }

    public static String[] findPackageInstaller(
            Context context) {

        PackageManager packageManager =
                context.getPackageManager();

        Intent intent = new Intent();

        for (String[] installer : C.INSTALLERS) {

            if (installer == null
                    || installer.length < 2) {
                continue;
            }

            intent.setClassName(
                    installer[0],
                    installer[1]
            );

            if (packageManager.resolveActivity(
                    intent,
                    0
            ) != null) {

                return installer;
            }
        }

        return null;
    }

    @TargetApi(21)
    public static void cleanlyExit(
            Activity activity) {

        if (activity == null) {
            return;
        }

        if (C.SDK >= 21) {
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
    }

    @TargetApi(11)
    @SuppressWarnings("deprecation")
    public static void copy2Clipboard(
            Context context,
            String str) {

        if (context == null || str == null) {
            return;
        }

        if (C.SDK >= 11) {

            ClipboardManager clipboard =
                    (ClipboardManager) context.getSystemService(
                            Context.CLIPBOARD_SERVICE
                    );

            if (clipboard != null) {

                clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                                "apkchecker",
                                str
                        )
                );
            }

        } else {

            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager)
                            context.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                            );

            if (clipboard != null) {
                clipboard.setText(str);
            }
        }
    }
}
