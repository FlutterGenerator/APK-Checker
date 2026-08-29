package com.by_syk.apkchecker.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class UriAnalyser {
    @TargetApi(19)
    public static String getRealPath(Context context, Uri uri) {
        if (uri == null) {
            return (String) null;
        }
        String path = (String) null;
        String authority = uri.getAuthority();
        String scheme = uri.getScheme();
        if (C.SDK >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (authority.equals("com.android.externalstorage.documents")) {
                String[] strArrSplit = documentId.split(":");
                if (strArrSplit.length != 1 && strArrSplit[0].equalsIgnoreCase("primary")) {
                    path = String.format("%1$s/%2$s", Environment.getExternalStorageDirectory(), documentId.substring(strArrSplit[0].length() + 1));
                }
            } else if (authority.equals("com.android.providers.downloads.documents")) {
                path = getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId).longValue()), (String) null, (String[]) null);
            } else if (authority.equals("com.android.providers.media.documents")) {
                String[] strArrSplit2 = documentId.split(":");
                if (strArrSplit2.length != 1) {
                    Uri uri2 = (Uri) null;
                    String lowerCase = strArrSplit2[0].toLowerCase();
                    if (lowerCase.equals("image")) {
                        uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if (lowerCase.equals("video")) {
                        uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if (lowerCase.equals("audio")) {
                        uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    path = getDataColumn(context, uri2, "_id=?", new String[]{strArrSplit2[1]});
                }
            }
        } else if ("content".equalsIgnoreCase(scheme)) {
            if (authority.equals("com.google.android.bluetooth.fileprovider")) {
                String path2 = uri.getPath();
                if (path2.startsWith("/bluetooth")) {
                    path = path2.replaceFirst("/bluetooth", Environment.getExternalStorageDirectory().getPath());
                }
            } else if (authority.equals("downloads")) {
                path = getDataColumn(context, Uri.parse(uri.toString().replace("content://downloads/my_downloads/", "content://downloads/public_downloads/")), (String) null, (String[]) null);
            } else if (authority.equals("com.android.email.provider")) {
                path = uri.getQueryParameter("filePath");
            } else if (!authority.equals("com.android.email.attachmentprovider")) {
                path = getDataColumn(context, uri, (String) null, (String[]) null);
            }
        } else if ("file".equalsIgnoreCase(scheme)) {
            path = uri.getPath();
        }
        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String str, String[] strArr) {
        String str2;
        if (uri == null) {
            return (String) null;
        }
        String string = (String) null;
        Cursor cursorQuery = (Cursor) null;
        try {
            cursorQuery = context.getContentResolver().query(uri, new String[]{"_data"}, str, strArr, (String) null);
            if (cursorQuery != null && cursorQuery.moveToFirst()) {
                string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("_data"));
            }
            str2 = string;
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
            str2 = string;
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
        return str2;
    }

    /* JADX WARN: Code duplicated, block: B:44:0x0048 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v11 */
    /* JADX WARN: Type inference failed for: r3v12 */
    /* JADX WARN: Type inference failed for: r3v4, types: [java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r3v7, types: [java.io.InputStream] */
    public static File extractFile(Context context, Uri uri, File file) throws Throwable {
        if (uri == null) {
            return null;
        }

        String name;

        if (file == null) {
            name = uri.getLastPathSegment();
        } else {
            name = file.getName();
        }

        if (TextUtils.isEmpty(name)) {
            name = C.TEMP_FILE_NAME_APK;
        }

        File externalCacheDir = context.getExternalCacheDir();

        if (externalCacheDir == null) {
            return null;
        }

        File file2 = new File(externalCacheDir, name);

        InputStream inputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                return null;
            }

            boolean copyFile = ExtraUtil.copyFile(inputStream, file2);

            return copyFile ? file2 : null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String getUriDetails(Uri uri) {
        if (uri == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Uri: ").append(Uri.decode(uri.toString()));
        sb.append("\n\nScheme: ").append(uri.getScheme());
        sb.append("\n\nAuthority: ").append(uri.getAuthority());
        sb.append("\n\n* User info: ").append(uri.getUserInfo());
        sb.append("\n\n* Host: ").append(uri.getHost());
        sb.append("\n\n* Port: ").append(uri.getPort());
        sb.append("\n\nPath: ").append(uri.getPath());
        sb.append("\n\n* Path segments: ").append(uri.getPathSegments());
        sb.append("\n\nQuery: ").append(uri.getQuery());
        sb.append("\n\nFragment: ").append(uri.getFragment());
        return sb.toString();
    }
}
