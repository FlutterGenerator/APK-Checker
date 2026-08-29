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
                .appendPath(file.getAbsolutePath())
                .build();
    }

    private File getFile(Uri uri) throws FileNotFoundException {

        if (uri == null) {
            throw new FileNotFoundException("URI is null");
        }

        String path = uri.getPath();

        if (path == null || path.length() == 0) {
            throw new FileNotFoundException("Empty URI path");
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(
                    "File does not exist: " + path
            );
        }

        if (!file.isFile()) {
            throw new FileNotFoundException(
                    "Not a file: " + path
            );
        }

        if (!file.canRead()) {
            throw new FileNotFoundException(
                    "File is not readable: " + path
            );
        }

        return file;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        try {

            File file = getFile(uri);

            String[] columns = new String[]{
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
            };

            MatrixCursor cursor =
                    new MatrixCursor(columns, 1);

            cursor.addRow(
                    new Object[]{
                            file.getName(),
                            file.length()
                    }
            );

            return cursor;

        } catch (FileNotFoundException e) {

            return null;
        }
    }

    @Override
    public ParcelFileDescriptor openFile(
            Uri uri,
            String mode) throws FileNotFoundException {

        File file = getFile(uri);

        if (!"r".equals(mode) &&
                !"rt".equals(mode)) {

            throw new FileNotFoundException(
                    "Read only provider"
            );
        }

        return ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
        );
    }

    @Override
    public Uri insert(
            Uri uri,
            ContentValues values) {

        throw new UnsupportedOperationException(
                "Insert not supported"
        );
    }

    @Override
    public int delete(
            Uri uri,
            String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException(
                "Delete not supported"
        );
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException(
                "Update not supported"
        );
    }
}
