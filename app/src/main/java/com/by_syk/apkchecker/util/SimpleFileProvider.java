package com.by_syk.apkchecker.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

public class SimpleFileProvider extends ContentProvider {

    public static Uri getUriForFile(String authority, File file) {

        return new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .path(file.getAbsolutePath())
                .build();
    }

    private static File fileFromUri(Uri uri) {

        if (uri == null) {
            return null;
        }

        String path = uri.getPath();

        if (path == null || path.length() == 0) {
            return null;
        }

        return new File(path);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        File file = fileFromUri(uri);

        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        String[] cols = {
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
        };

        MatrixCursor cursor =
                new MatrixCursor(cols, 1);

        cursor.addRow(
                new Object[]{
                        file.getName(),
                        file.length()
                }
        );

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Uri insert(
            Uri uri,
            ContentValues values) {

        throw new UnsupportedOperationException(
                "Insert not supported."
        );
    }

    @Override
    public int delete(
            Uri uri,
            String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException(
                "Delete not supported."
        );
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException(
                "Update not supported."
        );
    }

    @Override
    public ParcelFileDescriptor openFile(
            Uri uri,
            String mode)
            throws FileNotFoundException {

        File file = fileFromUri(uri);

        if (file == null ||
                !file.exists() ||
                !file.canRead()) {

            throw new FileNotFoundException(
                    "APK not found: " + uri
            );
        }

        return ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
        );
    }
}
