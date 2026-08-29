package com.by_syk.apkchecker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import com.by_syk.apkchecker.util.C;
import com.by_syk.apkchecker.util.ExtraUtil;
import com.by_syk.apkchecker.util.PoorAppInfo;
import com.by_syk.apkchecker.util.SP;
import com.by_syk.apkchecker.util.UriAnalyser;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BatchRenameActivity extends Activity {
    SP sp = (SP) null;
    boolean light_theme = false;
    boolean dev_mode = false;
    boolean progress_hidden = false;
    boolean is_running = true;

    @Override // android.app.Activity
    @TargetApi(23)
    protected void onCreate(Bundle bundle) {
        this.sp = new SP(this);
        this.dev_mode = this.sp.getBoolean(C.SP_DEV_MODE);
        if (this.dev_mode) {
            this.light_theme = this.sp.getBoolean(C.SP_LIGHT_THEME);
            if (this.light_theme) {
                setTheme(R.style.app_theme_light);
            }
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_batch_rename);
        if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
            if (C.SDK >= 23 && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                if (shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    Toast.makeText(this, R.string.toast_request_permission, 1).show();
                }
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
                return;
            }
            chooseFileNameDialog();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.is_running = false;
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 0 && iArr[0] == 0) {
            chooseFileNameDialog();
        } else {
            finish();
        }
    }

    private class BatchRenameTask extends AsyncTask<String, Integer, String[]> {
        private final BatchRenameActivity this$0;
        ArrayList<Uri> uris;
        int total = 0;
        File[][] filesOldNew = (File[][]) null;
        List<String> failedFileNameList = (List) null;
        boolean rename_cancelled = false;
        ProgressDialog progressDialog = (ProgressDialog) null;

        @Override // android.os.AsyncTask
        protected /* bridge */ String[] doInBackground(String[] strArr) {
            return doInBackground2(strArr);
        }

        @Override // android.os.AsyncTask
        protected /* bridge */ void onPostExecute(String[] strArr) {
            onPostExecute2(strArr);
        }

        @Override // android.os.AsyncTask
        protected /* bridge */ void onProgressUpdate(Integer[] numArr) {
            onProgressUpdate2(numArr);
        }

        public BatchRenameTask(BatchRenameActivity batchRenameActivity, ArrayList<Uri> arrayList) {
            this.this$0 = batchRenameActivity;
            this.uris = (ArrayList) null;
            this.uris = arrayList;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            ProgressDialog progressDialog;
            super.onPreExecute();
            if (this.uris != null) {
                this.total = this.uris.size();
                this.filesOldNew = (File[][]) Array.newInstance((Class<?>) File.class, this.total, 2);
                this.failedFileNameList = new ArrayList(this.total);
                if (C.SDK >= 21) {
                    progressDialog = new ProgressDialog(this.this$0, this.this$0.light_theme ? R.style.alert_dialog_style_light : R.style.alert_dialog_style);
                } else {
                    progressDialog = new ProgressDialog(this.this$0);
                }
                this.progressDialog = progressDialog;
                this.progressDialog.setProgressStyle(1);
                this.progressDialog.setIndeterminate(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.setTitle(R.string.dia_title_renaming);
                this.progressDialog.setMessage("");
                this.progressDialog.setMax(this.total);
                this.progressDialog.setButton(-1, this.this$0.getString(R.string.dia_bt_hide), new DialogInterface.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.BatchRenameTask.100000000

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BatchRenameTask.this.this$0.progress_hidden = true;
                    }
                });
                this.progressDialog.setButton(-2, this.this$0.getString(R.string.dia_bt_cancel), new DialogInterface.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.BatchRenameTask.100000001

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BatchRenameTask.this.rename_cancelled = true;
                    }
                });
                this.progressDialog.show();
                this.this$0.findViewById(R.id.ll_progress).setVisibility(0);
                this.this$0.findViewById(R.id.rl_batch_rename).setOnClickListener(new View.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.BatchRenameTask.100000002

                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        BatchRenameTask.this.this$0.progress_hidden = false;
                    }
                });
            }
        }

        /* JADX INFO: renamed from: doInBackground, reason: avoid collision after fix types in other method */
        protected String[] doInBackground2(String... strArr) {
            PackageInfo packageInfoUninstalled;
            if (this.uris == null) {
                return (String[]) null;
            }
            for (int i = 0; i < this.total; i++) {
                String realPath = UriAnalyser.getRealPath(this.this$0, this.uris.get(i));
                if (realPath != null) {
                    File file = new File(realPath);
                    this.filesOldNew[i][0] = file;
                    this.failedFileNameList.add(file.isDirectory() ? new StringBuffer().append("/").append(file.getName()).toString() : file.getName());
                }
            }
            for (int i2 = 0; i2 < this.total && !this.rename_cancelled && this.this$0.is_running; i2++) {
                publishProgress(new Integer(i2 + 1));
                if (this.filesOldNew[i2][0].getName().endsWith(".apk") && this.filesOldNew[i2][0].isFile() && (packageInfoUninstalled = ExtraUtil.getPackageInfoUninstalled(this.this$0, this.filesOldNew[i2][0])) != null) {
                    PoorAppInfo poorAppInfo = new PoorAppInfo();
                    poorAppInfo.initAndAnalysis(this.this$0, packageInfoUninstalled);
                    File fileRenameApkFileName = ExtraUtil.renameApkFileName(poorAppInfo.filePath, poorAppInfo.label, poorAppInfo.packageName, poorAppInfo.verName, poorAppInfo.ver_code, strArr[0], true);
                    if (fileRenameApkFileName != null) {
                        this.filesOldNew[i2][1] = fileRenameApkFileName;
                        this.failedFileNameList.remove(this.filesOldNew[i2][0].getName());
                    }
                }
            }
            return (String[]) this.failedFileNameList.toArray(new String[this.failedFileNameList.size()]);
        }

        /* JADX INFO: renamed from: onProgressUpdate, reason: avoid collision after fix types in other method */
        protected void onProgressUpdate2(Integer... numArr) {
            super.onProgressUpdate(numArr);
            if (!this.this$0.progress_hidden && numArr[0].intValue() % 2 <= 0) {
                if (!this.progressDialog.isShowing()) {
                    this.progressDialog.show();
                }
                this.progressDialog.setMessage(this.filesOldNew[numArr[0].intValue() - 1][0].getName());
                this.progressDialog.setProgress(numArr[0].intValue());
            }
        }

        /* JADX INFO: renamed from: onPostExecute, reason: avoid collision after fix types in other method */
        protected void onPostExecute2(String[] strArr) {
            super.onPostExecute(strArr);
            if (this.progressDialog != null && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
            this.this$0.findViewById(R.id.ll_progress).setVisibility(8);
            this.this$0.renameResultDialog(strArr, this.filesOldNew);
        }
    }

    @TargetApi(11)
    private void chooseFileNameDialog() {
        AlertDialog.Builder builder;
        final String[] stringArray = getResources().getStringArray(this.dev_mode ? R.array.apk_names_format_2 : R.array.apk_names_format);
        int i = this.dev_mode ? this.sp.getInt(C.SP_APK_NAME_FORMAT_ID_2, 2) : this.sp.getInt(C.SP_APK_NAME_FORMAT_ID, 1);
        final String[] strArr = {stringArray[i]};
        if (C.SDK >= 21) {
            builder = new AlertDialog.Builder(this, this.light_theme ? R.style.alert_dialog_style_light : R.style.alert_dialog_style);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.dia_title_rename_apk_rule).setSingleChoiceItems(dev_mode ? R.array.apk_names_2 : R.array.apk_names, i, new DialogInterface.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.100000003
            private final String[] val$APK_NAMES_FORMAT;
            private final String[] val$CHOSEN_FORMAT;

            {
                this.val$CHOSEN_FORMAT = strArr;
                this.val$APK_NAMES_FORMAT = stringArray;
            }

            @Override
            public void onClick(DialogInterface dialogInterface, int i2) {
                if (i2 == (BatchRenameActivity.this.dev_mode
                        ? BatchRenameActivity.this.sp.getInt(C.SP_APK_NAME_FORMAT_ID_2, 2)
                        : BatchRenameActivity.this.sp.getInt(C.SP_APK_NAME_FORMAT_ID, 1))) {

                    dialogInterface.dismiss();

                    new BatchRenameTask(
                            BatchRenameActivity.this,
                            BatchRenameActivity.this.getIntent()
                                    .<Uri>getParcelableArrayListExtra("android.intent.extra.STREAM")
                    ).execute(this.val$CHOSEN_FORMAT[0]);

                } else {
                    BatchRenameActivity.this.sp.save(
                            BatchRenameActivity.this.dev_mode
                                    ? C.SP_APK_NAME_FORMAT_ID_2
                                    : C.SP_APK_NAME_FORMAT_ID,
                            i2
                    );

                    this.val$CHOSEN_FORMAT[0] = this.val$APK_NAMES_FORMAT[i2];
                }
            }

        }).setPositiveButton(R.string.dia_bt_ok, new DialogInterface.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.100000004
            private final String[] val$CHOSEN_FORMAT;

            {
                this.val$CHOSEN_FORMAT = strArr;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                BatchRenameActivity.this.sp.save(
                        BatchRenameActivity.this.dev_mode
                                ? C.SP_REMEMBER_APK_NAME_FORMAT_2
                                : C.SP_REMEMBER_APK_NAME_FORMAT,
                        true
                );
                new BatchRenameTask(
                        BatchRenameActivity.this,
                        BatchRenameActivity.this.getIntent()
                                .<Uri>getParcelableArrayListExtra("android.intent.extra.STREAM")
                ).execute(this.val$CHOSEN_FORMAT[0]);
            }
        }).setNegativeButton(R.string.dia_bt_cancel, new DialogInterface.OnClickListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.100000005

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.cancel();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.100000006

            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                ExtraUtil.cleanlyExit(BatchRenameActivity.this);
            }
        }).create().show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    @TargetApi(11)
    public void renameResultDialog(String[] strArr, File[][] fileArr) {
        AlertDialog.Builder builder;
        if (this.is_running) {
            if (strArr == null) {
                strArr = new String[0];
            }
            String str = String.format(getString(R.string.dia_title_skipped), new Integer(strArr.length), new Integer(fileArr.length));
            if (C.SDK >= 21) {
                builder = new AlertDialog.Builder(this, this.light_theme ? R.style.alert_dialog_style_light : R.style.alert_dialog_style);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            AlertDialog alertDialogCreate = builder.setTitle(str).setItems(strArr, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.dia_bt_close, (DialogInterface.OnClickListener) null).setNegativeButton(R.string.dia_bt_undo, new AnonymousClass100000008(this, fileArr)).create();
            alertDialogCreate.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.by_syk.apkchecker.BatchRenameActivity.100000009

                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialogInterface) {
                    ExtraUtil.cleanlyExit(BatchRenameActivity.this);
                }
            });
            alertDialogCreate.show();
            alertDialogCreate.getButton(-2).setEnabled(strArr.length < fileArr.length);
        }
    }

    /* JADX INFO: renamed from: com.by_syk.apkchecker.BatchRenameActivity$100000008, reason: invalid class name */
    class AnonymousClass100000008 implements DialogInterface.OnClickListener {
        private final BatchRenameActivity this$0;
        private final File[][] val$filesOldNew;

        AnonymousClass100000008(BatchRenameActivity batchRenameActivity, File[][] fileArr) {
            this.this$0 = batchRenameActivity;
            this.val$filesOldNew = fileArr;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ExtraUtil.undoRenameFile(AnonymousClass100000008.this.val$filesOldNew);
                Toast.makeText(
                        AnonymousClass100000008.this.this$0,
                        R.string.toast_undone,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

    }
    }

        @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }
}
