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

/**
 * Лёгкий провайдер для передачи APK-файлов другим приложениям
 * (системному установщику пакетов) через content:// Uri.
 *
 * Начиная с Android 7.0 (API 24) система запрещает передавать
 * "голые" file:// Uri другим приложениям через Intent —
 * это вызывает FileUriExposedException. Стандартное решение —
 * androidx.core.content.FileProvider, но проект не использует
 * AndroidX (minSdkVersion 7 конфликтует с современными версиями
 * этой библиотеки), поэтому здесь реализован собственный
 * минимальный аналог без внешних зависимостей.
 */
public class SimpleFileProvider extends ContentProvider {

    public static Uri getUriForFile(String authority, File file) {

        return new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .path(file.getAbsolutePath())
                .build();
    }

    private static File fileFromUri(Uri uri) {
        return new File(uri.getPath());
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

        String[] cols = {
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
        };

        MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(new Object[]{file.getName(), file.length()});
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported.");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported.");
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException("Update not supported.");
    }

    @Override
    public ParcelFileDescriptor openFile(
            Uri uri,
            String mode) throws FileNotFoundException {

        File file = fileFromUri(uri);

        return ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
        );
    }
}
