package com.by_syk.apkchecker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.by_syk.apkchecker.util.AppInfo;
import com.by_syk.apkchecker.util.C;
import com.by_syk.apkchecker.util.ExtraUtil;
import com.by_syk.apkchecker.util.SP;
import com.by_syk.apkchecker.util.SimpleFileProvider;
import com.by_syk.apkchecker.util.UriAnalyser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private SP sp;

    private Uri rawUri;
    private File apkFile;

    private AppInfo appInfoUninstalled = new AppInfo();
    private AppInfo appInfoInstalled = new AppInfo();

    private boolean fail_to_rename = false;
    private boolean fail_to_save_icon = false;
    private boolean fail_to_goto_market = false;

    private boolean is_running = true;
    private boolean is_installing = false;

    private boolean rename_firstly = false;
    private boolean save_icon_firstly_un = false;
    private boolean save_icon_firstly_in = false;

    private boolean light_theme = false;
    private boolean dev_mode = false;

    private int hide_flags = C.DEFAULT_HIDE_FLAGS;

    private ProgressDialog progressDialog;
    private ProgressBar progressBar;


    @Override
    @TargetApi(23)
    protected void onCreate(Bundle bundle) {

        sp = new SP(this);

        dev_mode = sp.getBoolean(C.SP_DEV_MODE);

        if (dev_mode) {

            light_theme = sp.getBoolean(
                    C.SP_LIGHT_THEME
            );

            if (light_theme) {
                setTheme(R.style.app_theme_light);
            }
        }

        super.onCreate(bundle);

        if (C.SDK < 21) {
            requestWindowFeature(5);
        }

        setContentView(R.layout.activity_main);

        if (C.SDK >= 23 && C.SDK <= 32 &&
                checkSelfPermission(
                        "android.permission.WRITE_EXTERNAL_STORAGE"
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    "android.permission.WRITE_EXTERNAL_STORAGE"
            )) {

                Toast.makeText(
                        this,
                        R.string.toast_request_permission,
                        Toast.LENGTH_SHORT
                ).show();
            }

            requestPermissions(
                    new String[]{
                            "android.permission.WRITE_EXTERNAL_STORAGE"
                    },
                    0
            );

        } else {

            init();

            new LoadDataTask(
                    this,
                    false
            ).execute(new String[0]);
        }
    }


    @Override
    protected void onDestroy() {

        is_running = false;

        super.onDestroy();

        File externalCacheDir = getExternalCacheDir();

        if (externalCacheDir != null) {

            File[] files = externalCacheDir.listFiles();

            if (files != null) {

                for (File file : files) {

                    try {
                        file.delete();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 0 &&
                grantResults != null &&
                grantResults.length > 0 &&
                grantResults[0] ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {

            init();

            new LoadDataTask(
                    this,
                    false
            ).execute(new String[0]);
        }
    }


    private void init() {

        hide_flags = sp.getInt(
                C.SP_HIDE_FLAGS,
                C.DEFAULT_HIDE_FLAGS
        );

        if (!sp.getBoolean(C.SP_HIDE_ICON)) {

            try {

                ExtraUtil.hideComponent(
                        this,
                        new ComponentName(
                                this,
                                Class.forName(
                                        "com.by_syk.apkchecker.HelloActivity"
                                )
                        ),
                        true
                );

                sp.save(
                        C.SP_HIDE_ICON,
                        true
                );

            } catch (ClassNotFoundException e) {

                throw new NoClassDefFoundError(
                        e.getMessage()
                );
            }
        }

        Button button =
                findViewById(R.id.bt_install);

        if (button == null) {
            return;
        }

        button.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (C.SDK >= 21) {
                            preInstallAPK();
                        } else {
                            installAPK();
                        }
                    }
                }
        );

        button.setOnLongClickListener(
                new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {

                        if (dev_mode) {
                            setInstallerDialog();
                        }

                        return true;
                    }
                }
        );
    }


    @TargetApi(21)
    private void preInstallAPK() {

        final View root =
                findViewById(R.id.ll_root);

        if (root == null) {
            installAPK();
            return;
        }

        final int width =
                root.getMeasuredWidth();

        final int height =
                root.getMeasuredHeight();

        if (width <= 0 || height <= 0) {
            installAPK();
            return;
        }

        final int radius =
                ((int) Math.sqrt(
                        (width * width) +
                                (height * height)
                )) + 1;

        Animator animator =
                ViewAnimationUtils.createCircularReveal(
                        root,
                        0,
                        height,
                        radius,
                        0
                );

        animator.setInterpolator(
                new AccelerateInterpolator()
        );

        animator.setDuration(
                getResources().getInteger(
                        android.R.integer.config_mediumAnimTime
                )
        );

        animator.addListener(
                new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(
                            Animator animation) {

                        super.onAnimationEnd(animation);

                        root.setVisibility(
                                View.INVISIBLE
                        );

                        if (installAPK()) {
                            return;
                        }

                        root.setVisibility(
                                View.VISIBLE
                        );

                        ViewAnimationUtils
                                .createCircularReveal(
                                        root,
                                        0,
                                        height,
                                        0,
                                        radius
                                )
                                .start();
                    }
                }
        );

        animator.start();
    }


    /**
     * Converts an APK File into a URI that can safely
     * be passed to another application.
     *
     * Android 7.0+ does not allow exposing file:// URI
     * to another application.
     */
    private Uri fileToShareableUri(File file) {

        if (file == null ||
                !file.exists() ||
                !file.isFile() ||
                !file.canRead()) {

            return null;
        }

        if (C.SDK >= 24) {

            try {

                return SimpleFileProvider.getUriForFile(
                        getPackageName() + ".fileprovider",
                        file
                );

            } catch (Throwable e) {

                Log.e(
                        C.LOG_TAG,
                        "Unable to create FileProvider URI",
                        e
                );

                return null;
            }
        }

        return Uri.fromFile(file);
    }


    /**
     * Starts APK installation.
     *
     * Android 8.0+ requires REQUEST_INSTALL_PACKAGES
     * permission to be granted by the user.
     */
    private boolean installAPK() {

        /*
         * Android 8.0+
         */
        if (C.SDK >= 26 &&
                !getPackageManager().canRequestPackageInstalls()) {

            try {

                Intent settingsIntent =
                        new Intent(
                                android.provider.Settings
                                        .ACTION_MANAGE_UNKNOWN_APP_SOURCES
                        );

                settingsIntent.setData(
                        Uri.parse(
                                "package:" +
                                        getPackageName()
                        )
                );

                startActivity(settingsIntent);

            } catch (Throwable e) {

                Log.e(
                        C.LOG_TAG,
                        "Cannot open unknown sources settings",
                        e
                );

                Toast.makeText(
                        this,
                        "Разрешите установку неизвестных приложений",
                        Toast.LENGTH_LONG
                ).show();
            }

            return false;
        }


        /*
         * Получаем APK.
         */
        File file = this.apkFile;

        if (file == null ||
                !file.exists() ||
                !file.isFile() ||
                !file.canRead()) {

            Uri inputUri =
                    getIntent().getData();

            if (inputUri != null) {

                /*
                 * Сначала пытаемся получить настоящий путь.
                 */
                try {

                    String realPath =
                            UriAnalyser.getRealPath(
                                    this,
                                    inputUri
                            );

                    if (!TextUtils.isEmpty(realPath)) {

                        File candidate =
                                new File(realPath);

                        if (candidate.exists() &&
                                candidate.isFile() &&
                                candidate.canRead()) {

                            file = candidate;
                        }
                    }

                } catch (Throwable e) {

                    Log.e(
                            C.LOG_TAG,
                            "getRealPath failed",
                            e
                    );
                }


                /*
                 * Android 10/11+:
                 * если реальный путь получить невозможно,
                 * копируем content:// URI в cache.
                 */
                if (file == null ||
                        !file.exists() ||
                        !file.isFile() ||
                        !file.canRead()) {

                    try {

                        file =
                                UriAnalyser.extractFile(
                                        this,
                                        inputUri,
                                        null
                                );

                    } catch (Throwable e) {

                        Log.e(
                                C.LOG_TAG,
                                "Failed to extract APK",
                                e
                        );

                        file = null;
                    }
                }
            }
        }


        /*
         * Проверяем APK.
         */
        if (file == null ||
                !file.exists() ||
                !file.isFile() ||
                !file.canRead()) {

            Log.e(
                    C.LOG_TAG,
                    "APK file is not available"
            );

            Toast.makeText(
                    this,
                    R.string.toast_no_system_installer,
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }


        this.apkFile = file;


        /*
         * Получаем безопасный URI.
         */
        Uri uriFromFile =
                fileToShareableUri(file);

        if (uriFromFile == null) {

            Log.e(
                    C.LOG_TAG,
                    "Unable to create APK content URI"
            );

            Toast.makeText(
                    this,
                    R.string.toast_no_system_installer,
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }


        Log.d(
                C.LOG_TAG,
                "APK file: " +
                        file.getAbsolutePath()
        );

        Log.d(
                C.LOG_TAG,
                "APK size: " +
                        file.length()
        );

        Log.d(
                C.LOG_TAG,
                "APK URI: " +
                        uriFromFile
        );


        /*
         * =====================================================
         * 1. Пользовательский установщик
         * =====================================================
         */
        if (sp.contains(
                C.SP_INSTALLER_PACKAGE_NAME
        )) {

            String packageName =
                    sp.getString(
                            C.SP_INSTALLER_PACKAGE_NAME,
                            null
                    );

            String className =
                    sp.getString(
                            C.SP_INSTALLER_CLASS_NAME,
                            null
                    );

            if (!TextUtils.isEmpty(packageName) &&
                    !TextUtils.isEmpty(className)) {

                Intent explicitIntent =
                        new Intent(
                                Intent.ACTION_INSTALL_PACKAGE
                        );

                explicitIntent.setClassName(
                        packageName,
                        className
                );

                explicitIntent.setDataAndType(
                        uriFromFile,
                        "application/vnd.android.package-archive"
                );

                explicitIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                explicitIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );


                /*
                 * Явно выдаём установщику доступ
                 * к APK.
                 */
                if (C.SDK >= 23) {

                    try {

                        grantUriPermission(
                                packageName,
                                uriFromFile,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                    } catch (Throwable e) {

                        Log.e(
                                C.LOG_TAG,
                                "grantUriPermission failed",
                                e
                        );
                    }
                }


                try {

                    startActivity(explicitIntent);

                    is_installing = true;

                    finish();

                    return true;

                } catch (Throwable e) {

                    Log.e(
                            C.LOG_TAG,
                            "Custom installer failed: " +
                                    packageName +
                                    " / " +
                                    className,
                            e
                    );
                }
            }
        }


        /*
         * =====================================================
         * 2. Системный установщик
         * =====================================================
         */
        Intent intent =
                new Intent(
                        Intent.ACTION_INSTALL_PACKAGE
                );

        intent.setDataAndType(
                uriFromFile,
                "application/vnd.android.package-archive"
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );


        /*
         * Передаём исходный URI.
         */
        if (C.SDK >= 17) {

            Uri originatingUri =
                    getIntent().getData();

            if (originatingUri != null) {

                intent.putExtra(
                        Intent.EXTRA_ORIGINATING_URI,
                        originatingUri
                );
            }
        }


        try {

            startActivity(intent);

            is_installing = true;

            finish();

            return true;

        } catch (ActivityNotFoundException e) {

            Log.e(
                    C.LOG_TAG,
                    "No APK installer found",
                    e
            );

        } catch (SecurityException e) {

            Log.e(
                    C.LOG_TAG,
                    "SecurityException starting APK installer",
                    e
            );

        } catch (Throwable e) {

            Log.e(
                    C.LOG_TAG,
                    "Failed to start APK installer",
                    e
            );
        }


        /*
         * =====================================================
         * 3. ACTION_VIEW fallback
         * =====================================================
         */
        try {

            Intent fallback =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            fallback.setDataAndType(
                    uriFromFile,
                    "application/vnd.android.package-archive"
            );

            fallback.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            fallback.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(fallback);

            is_installing = true;

            finish();

            return true;

        } catch (Throwable e) {

            Log.e(
                    C.LOG_TAG,
                    "Fallback installer failed",
                    e
            );
        }


        Toast.makeText(
                this,
                R.string.toast_no_system_installer,
                Toast.LENGTH_LONG
        ).show();

        return false;
    }


    private class LoadDataTask
            extends AsyncTask<String, Integer, Boolean> {

        private long cost_start;
        private boolean is_installed;
        private final MainActivity activity;


        LoadDataTask(
                MainActivity activity,
                boolean installed) {

            this.activity = activity;
            this.is_installed = installed;
        }


        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            cost_start =
                    System.currentTimeMillis();

            activity.waiting(
                    is_installed,
                    true
            );
        }


        @Override
        protected Boolean doInBackground(
                String[] params) {

            activity.findApkFile();

            if (activity.apkFile == null) {
                return false;
            }

            boolean result = false;

            try {

                if (!is_installed) {

                    result =
                            activity.appInfoUninstalled
                                    .initAndAnalysis(
                                            activity,
                                            ExtraUtil
                                                    .getPackageInfoUninstalled(
                                                            activity,
                                                            activity.apkFile,
                                                            activity.hide_flags
                                                    ),
                                            false,
                                            activity.light_theme,
                                            activity.hide_flags,
                                            activity.sp.getBoolean(
                                                    C.SP_EXTRA_LABEL,
                                                    true
                                            )
                                    );

                } else if (
                        activity.appInfoUninstalled.is_ok) {

                    result =
                            activity.appInfoInstalled
                                    .initAndAnalysis(
                                            activity,
                                            ExtraUtil
                                                    .getPackageInfoInstalled(
                                                            activity,
                                                            activity
                                                                    .appInfoUninstalled
                                                                    .packageName,
                                                            activity
                                                                    .appInfoUninstalled
                                                                    .hide_flags
                                                    ),
                                            true,
                                            activity
                                                    .appInfoUninstalled
                                                    .light_theme,
                                            activity
                                                    .appInfoUninstalled
                                                    .hide_flags,
                                            activity
                                                    .appInfoUninstalled
                                                    .extra_label
                                    );
                }

            } catch (Throwable e) {

                Log.e(
                        C.LOG_TAG,
                        "APK analysis failed",
                        e
                );

                result = false;
            }

            return result;
        }


        @Override
        protected void onPostExecute(
                Boolean result) {

            super.onPostExecute(result);

            if (result) {

                if (!is_installed) {

                    if (ExtraUtil.checkPackageExists(
                            activity,
                            activity
                                    .appInfoUninstalled
                                    .packageName
                    )) {

                        activity.enableShowInstalled();
                    }

                    activity.fillData(
                            activity.findViewById(
                                    R.id.include_uninstalled
                            ),
                            activity.appInfoUninstalled
                    );

                    if (activity.fail_to_rename) {

                        activity.renameDialog(
                                activity.appInfoUninstalled
                        );
                    }

                    if (activity.fail_to_save_icon) {

                        activity.saveIconDialog(
                                activity.appInfoUninstalled
                        );
                    }

                    if (activity.fail_to_goto_market) {

                        activity.gotoMarket(
                                activity
                                        .appInfoUninstalled
                                        .packageName,
                                false
                        );
                    }

                } else {

                    ViewStub stub =
                            (ViewStub) activity.findViewById(
                                    R.id.vs_installed
                            );

                    if (stub != null) {

                        activity.fillData(
                                stub.inflate(),
                                activity.appInfoInstalled
                        );
                    }
                }


                new Handler().postDelayed(
                        new Runnable() {

                            @Override
                            public void run() {

                                View hsv =
                                        activity.findViewById(
                                                R.id.hsv_content
                                        );

                                if (hsv instanceof HorizontalScrollView) {

                                    ((HorizontalScrollView) hsv)
                                            .smoothScrollTo(0, 0);
                                }

                                if (is_installed) {

                                    View sv =
                                            activity.findViewById(
                                                    R.id.sv_content
                                            );

                                    View include =
                                            activity.findViewById(
                                                    R.id.include_uninstalled
                                            );

                                    if (sv instanceof ScrollView &&
                                            include != null) {

                                        ((ScrollView) sv)
                                                .smoothScrollTo(
                                                        0,
                                                        include
                                                                .getMeasuredHeight()
                                                );
                                    }
                                }
                            }
                        },
                        600
                );
            }

            activity.waiting(
                    is_installed,
                    false
            );
        }
    }


    private void waiting(
            boolean installed,
            boolean show) {

        if (C.SDK >= 21) {

            if (show) {

                if (!installed) {

                    progressDialog =
                            new ProgressDialog(
                                    this,
                                    light_theme
                                            ? R.style.alert_dialog_style_light
                                            : R.style.alert_dialog_style
                            );

                    progressDialog.setMessage(
                            getString(R.string.dia_loading)
                    );

                    progressDialog.setIndeterminate(
                            false
                    );

                    progressDialog.show();

                } else {

                    progressBar =
                            (ProgressBar) findViewById(
                                    R.id.pb_loading
                            );

                    if (progressBar != null) {

                        progressBar.setIndeterminate(
                                true
                        );

                        progressBar.setVisibility(
                                View.VISIBLE
                        );
                    }
                }

            } else {

                if (!installed) {

                    if (progressDialog != null &&
                            progressDialog.isShowing()) {

                        progressDialog.dismiss();
                    }

                    progressDialog = null;

                } else {

                    if (progressBar != null) {

                        progressBar.setVisibility(
                                View.GONE
                        );

                        progressBar = null;
                    }
                }
            }

        } else {

            setProgressBarIndeterminateVisibility(
                    show
            );
        }
    }


    private void findApkFile() {

        rawUri =
                getIntent().getData();

        File file = null;

        if (rawUri != null) {

            try {

                String realPath =
                        UriAnalyser.getRealPath(
                                this,
                                rawUri
                        );

                if (!TextUtils.isEmpty(realPath)) {
                    file = new File(realPath);
                }

            } catch (Throwable e) {

                Log.e(
                        C.LOG_TAG,
                        "Unable to resolve APK path",
                        e
                );
            }
        }


        if (file != null &&
                !file.exists()) {

            file =
                    ExtraUtil.tryLastRenamedFile(
                            sp.getString(
                                    C.SP_LAST_APK_OLD,
                                    null
                            ),
                            sp.getString(
                                    C.SP_LAST_APK_NEW,
                                    null
                            ),
                            sp.getLong(
                                    C.SP_LAST_APK_SIZE,
                                    -1
                            ),
                            file
                    );
        }


        if (file == null ||
                !file.canRead()) {

            try {

                if (rawUri != null) {

                    file =
                            UriAnalyser.extractFile(
                                    this,
                                    rawUri,
                                    file
                            );
                }

            } catch (Throwable e) {

                Log.e(
                        C.LOG_TAG,
                        "Failed to extract APK",
                        e
                );

                file = null;
            }
        }

        apkFile = file;
    }


    @TargetApi(11)
    private void UriDialog(
            Uri uri,
            String title) {

        String details =
                UriAnalyser.getUriDetails(uri);

        if (TextUtils.isEmpty(details)) {
            return;
        }

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }

        final String errorInfo = details;

        builder.setTitle(title)
                .setMessage(details)
                .setPositiveButton(
                        R.string.dia_bt_close,
                        null
                )
                .setNegativeButton(
                        R.string.dia_bt_copy,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                ExtraUtil.copy2Clipboard(
                                        MainActivity.this,
                                        errorInfo
                                );
                            }
                        }
                )
                .create()
                .show();
    }


    private void enableShowInstalled() {

        CheckBox checkBox =
                (CheckBox) findViewById(
                        R.id.cb_show_installed
                );

        if (checkBox == null) {
            return;
        }

        checkBox.setVisibility(
                View.VISIBLE
        );

        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(
                            CompoundButton button,
                            boolean checked) {

                        button.setVisibility(
                                View.GONE
                        );

                        new LoadDataTask(
                                MainActivity.this,
                                true
                        ).execute(new String[0]);
                    }
                }
        );
    }


    @SuppressWarnings("deprecation")
    private void fillData(
            View view,
            final AppInfo appInfo) {

        if (view == null ||
                appInfo == null ||
                !appInfo.is_ok) {

            return;
        }

        int[] rows = {

                R.id.tr_first_install,
                R.id.tr_last_update,
                R.id.tr_installed_from,
                R.id.tr_file_path,
                R.id.tr_file_name,
                R.id.tr_file_size,
                R.id.tr_launcher_icon,
                R.id.tr_package_name,
                R.id.tr_app_name,
                R.id.tr_ver_name,
                R.id.tr_ver_code,
                R.id.tr_compiling_time,
                R.id.tr_min_sdk,
                R.id.tr_target_sdk,
                R.id.tr_supported_abis,
                R.id.tr_req_permissions,
                R.id.tr_permissions,
                R.id.tr_flags,
                R.id.tr_launcher_activity,
                R.id.tr_activities,
                R.id.tr_services,
                R.id.tr_providers,
                R.id.tr_receivers,
                R.id.tr_signatures,
                R.id.tr_certificate_fingerprints,
                R.id.tr_certificate_start,
                R.id.tr_certificate_end,
                R.id.tr_icon_size
        };

        int[] tags = {

                R.id.tv_tag_first_install,
                R.id.tv_tag_last_update,
                R.id.tv_tag_installed_from,
                R.id.tv_tag_file_path,
                R.id.tv_tag_file_name,
                R.id.tv_tag_file_size,
                R.id.tv_tag_launcher_icon,
                R.id.tv_tag_package_name,
                R.id.tv_tag_app_name,
                R.id.tv_tag_ver_name,
                R.id.tv_tag_ver_code,
                R.id.tv_tag_compiling_time,
                R.id.tv_tag_min_sdk,
                R.id.tv_tag_target_sdk,
                R.id.tv_tag_supported_abis,
                R.id.tv_tag_req_permissions,
                R.id.tv_tag_permissions,
                R.id.tv_tag_flags,
                R.id.tv_tag_launcher_activity,
                R.id.tv_tag_activities,
                R.id.tv_tag_services,
                R.id.tv_tag_providers,
                R.id.tv_tag_receivers,
                R.id.tv_tag_signatures,
                R.id.tv_tag_certificate_fingerprints,
                R.id.tv_tag_certificate_start,
                R.id.tv_tag_certificate_end,
                R.id.tv_tag_icon_size
        };

        int[] values = {

                R.id.tv_first_install,
                R.id.tv_last_update,
                R.id.tv_installed_from,
                R.id.tv_file_path,
                R.id.tv_file_name,
                R.id.tv_file_size,
                R.id.iv_launcher_icon,
                R.id.tv_package_name,
                R.id.tv_app_name,
                R.id.tv_ver_name,
                R.id.tv_ver_code,
                R.id.tv_compiling_time,
                R.id.tv_min_sdk,
                R.id.tv_target_sdk,
                R.id.tv_supported_abis,
                R.id.tv_req_permissions,
                R.id.tv_permissions,
                R.id.tv_flags,
                R.id.tv_launcher_activity,
                R.id.tv_activities,
                R.id.tv_services,
                R.id.tv_providers,
                R.id.tv_receivers,
                R.id.tv_signatures,
                R.id.tv_certificate_fingerprints,
                R.id.tv_certificate_start,
                R.id.tv_certificate_end,
                R.id.tv_icon_size
        };

        boolean[] span = {

                false,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                true
        };

        String[] infos =
                appInfo.getInfosArray();

        SpannableStringBuilder[] spans =
                appInfo.getSpanInfosArray();

        int flagLi =
                C.ENUM_FLAGS.LI.ordinal();


        for (int i = 0;
             i < values.length;
             i++) {

            if (i == flagLi) {
                continue;
            }

            View row =
                    view.findViewById(
                            rows[i]
                    );

            if (row == null) {
                continue;
            }

            if (!span[i]) {

                if (infos == null ||
                        i >= infos.length ||
                        TextUtils.isEmpty(infos[i])) {

                    row.setVisibility(
                            View.GONE
                    );

                } else {

                    TextView text =
                            (TextView) view.findViewById(
                                    values[i]
                            );

                    if (text != null) {

                        text.setText(
                                infos[i]
                        );

                        setOldAndroidCopyListener(
                                text
                        );
                    }
                }

            } else {

                if (spans == null ||
                        i >= spans.length ||
                        spans[i] == null ||
                        spans[i].length() <= 0) {

                    row.setVisibility(
                            View.GONE
                    );

                } else {

                    TextView text =
                            (TextView) view.findViewById(
                                    values[i]
                            );

                    if (text != null) {

                        text.setText(
                                spans[i]
                        );

                        setOldAndroidCopyListener(
                                text
                        );
                    }
                }
            }
        }


        /*
         * Long click on information labels.
         */
        for (int i = 0;
             i < tags.length;
             i++) {

            View row =
                    view.findViewById(
                            rows[i]
                    );

            final TextView tagView =
                    (TextView) view.findViewById(
                            tags[i]
                    );

            if (row != null &&
                    tagView != null &&
                    row.getVisibility() != View.GONE) {

                tagView.setOnLongClickListener(
                        new View.OnLongClickListener() {

                            @Override
                            public boolean onLongClick(
                                    View v) {

                                tagHelpDialog(
                                        String.format(
                                                getString(
                                                        R.string
                                                                .dia_item_desc
                                                ),
                                                ExtraUtil
                                                        .removeSpaceInTag(
                                                                ((TextView) v)
                                                                        .getText()
                                                                        .toString()
                                                        ),
                                                v.getTag()
                                        )
                                );

                                return true;
                            }
                        }
                );
            }
        }


        /*
         * Launcher icon.
         */
        if (appInfo.ic_launcher != null &&
                ExtraUtil.getBit(
                        hide_flags,
                        C.ENUM_FLAGS.LI.ordinal()
                )) {

            ImageView imageView =
                    (ImageView) view.findViewById(
                            R.id.iv_launcher_icon
                    );

            if (imageView != null) {

                imageView.setImageDrawable(
                        appInfo.ic_launcher
                );

                imageView.setOnLongClickListener(
                        new View.OnLongClickListener() {

                            private final AppInfo info =
                                    appInfo;

                            @Override
                            public boolean onLongClick(
                                    View v) {

                                saveIconDialog(info);

                                return true;
                            }
                        }
                );
            }

        } else {

            View iconRow =
                    view.findViewById(
                            R.id.tr_launcher_icon
                    );

            if (iconRow != null) {

                iconRow.setVisibility(
                        View.GONE
                );
            }
        }
    }


    @SuppressWarnings("deprecation")
    private void setOldAndroidCopyListener(
            TextView textView) {

        if (textView == null ||
                C.SDK >= 11) {

            return;
        }

        textView.setOnLongClickListener(
                new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(
                            View view) {

                        ClipboardManager clipboard =
                                (ClipboardManager)
                                        getSystemService(
                                                CLIPBOARD_SERVICE
                                        );

                        clipboard.setText(
                                ((TextView) view).getText()
                        );

                        Toast.makeText(
                                MainActivity.this,
                                R.string.toast_copied,
                                Toast.LENGTH_SHORT
                        ).show();

                        return true;
                    }
                }
        );
    }


    @TargetApi(11)
    private void tagHelpDialog(
            String text) {

        if (TextUtils.isEmpty(text)) {
            return;
        }

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }

        AlertDialog dialog =
                builder.setTitle(
                                R.string.dia_title_help
                        )
                        .setMessage(text)
                        .setPositiveButton(
                                R.string.dia_bt_got_it,
                                null
                        )
                        .create();

        dialog.show();

        Button button =
                dialog.getButton(
                        DialogInterface.BUTTON_POSITIVE
                );

        if (button != null) {

            button.setOnLongClickListener(
                    new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(
                                View v) {

                            v.performClick();

                            activateDevMode();

                            return true;
                        }
                    }
            );
        }
    }


    @TargetApi(11)
    private void activateDevMode() {

        if (dev_mode) {

            Toast.makeText(
                    this,
                    R.string.toast_dev_already,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        sp.put(
                C.SP_APK_NAME_FORMAT_ID_2,
                sp.getInt(
                        C.SP_APK_NAME_FORMAT_ID,
                        1
                ) + 1
        ).put(
                C.SP_REMEMBER_APK_NAME_FORMAT_2,
                sp.getBoolean(
                        C.SP_REMEMBER_APK_NAME_FORMAT
                )
        ).write();

        dev_mode = true;

        sp.save(
                C.SP_DEV_MODE,
                true
        );

        if (C.SDK >= 11) {
            invalidateOptionsMenu();
        }

        Toast.makeText(
                this,
                R.string.toast_dev_mode,
                Toast.LENGTH_SHORT
        ).show();
    }


    private void refreshData(
            File file) {

        if (appInfoUninstalled.is_ok &&
                file != null &&
                file.exists()) {

            apkFile = file;

            appInfoUninstalled.updateFileName(
                    file
            );

            fillData(
                    findViewById(
                            R.id.include_uninstalled
                    ),
                    appInfoUninstalled
            );
        }
    }


    @TargetApi(11)
    private void saveIconDialog(
            final AppInfo appInfo) {

        if (appInfo == null ||
                !appInfo.is_ok) {

            fail_to_save_icon = true;

            return;
        }

        if (!is_running) {

            saveIcon(appInfo);

            return;
        }


        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        int selected =
                sp.getInt(
                        C.SP_PIC_NAME_FORMAT_ID,
                        1
                );

        builder.setTitle(
                R.string.dia_title_save_icon
        );


        builder.setSingleChoiceItems(
                R.array.pic_names,
                selected,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        sp.save(
                                C.SP_PIC_NAME_FORMAT_ID,
                                which
                        );

                        dialog.dismiss();

                        saveIcon(appInfo);
                    }
                }
        );


        builder.setPositiveButton(
                R.string.dia_bt_always,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        sp.save(
                                C.SP_REMEMBER_PIC_NAME_FORMAT,
                                true
                        );

                        saveIcon(appInfo);
                    }
                }
        );


        builder.setNegativeButton(
                R.string.dia_bt_once,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        sp.save(
                                C.SP_REMEMBER_PIC_NAME_FORMAT,
                                false
                        );

                        saveIcon(appInfo);
                    }
                }
        );


        builder.create().show();
    }


    private void saveIcon(
            final AppInfo appInfo) {

        if (appInfo == null) {
            return;
        }

        final String[] formats =
                getResources().getStringArray(
                        R.array.pic_names_format
                );

        final int formatId =
                sp.getInt(
                        C.SP_PIC_NAME_FORMAT_ID,
                        1
                );

        if (formatId < 0 ||
                formatId >= formats.length) {

            return;
        }

        new Handler().post(
                new Runnable() {

                    @Override
                    public void run() {

                        boolean saved = false;

                        try {

                            saved =
                                    ExtraUtil.saveIcon(
                                            MainActivity.this,
                                            appInfo
                                                    .getLauncherIcon(true),
                                            appInfo.label,
                                            appInfo.packageName,
                                            formats[formatId]
                                    );

                        } catch (Throwable e) {

                            Log.e(
                                    C.LOG_TAG,
                                    "Failed to save icon",
                                    e
                            );
                        }

                        if (saved) {

                            Toast.makeText(
                                    MainActivity.this,
                                    R.string.toast_saved_ic,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }


    @TargetApi(11)
    private void renameDialog(
            final AppInfo appInfo) {

        if (appInfo == null ||
                !appInfo.is_ok) {

            fail_to_rename = true;

            return;
        }

        if (!is_running) {

            if (!is_installing) {
                rename(appInfo);
            }

            return;
        }


        boolean remember =
                sp.getBoolean(
                        dev_mode
                                ? C.SP_REMEMBER_APK_NAME_FORMAT_2
                                : C.SP_REMEMBER_APK_NAME_FORMAT
                );


        if (remember) {

            rename_firstly =
                    !rename_firstly;

            if (rename_firstly) {

                rename(appInfo);

                return;
            }
        }


        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setTitle(
                R.string.dia_title_rename_apk_rule
        );


        builder.setSingleChoiceItems(
                dev_mode
                        ? R.array.apk_names_2
                        : R.array.apk_names,

                dev_mode
                        ? sp.getInt(
                                C.SP_APK_NAME_FORMAT_ID_2,
                                2
                        )
                        : sp.getInt(
                                C.SP_APK_NAME_FORMAT_ID,
                                1
                        ),

                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        int currentId =
                                dev_mode
                                        ? sp.getInt(
                                                C.SP_APK_NAME_FORMAT_ID_2,
                                                2
                                        )
                                        : sp.getInt(
                                                C.SP_APK_NAME_FORMAT_ID,
                                                1
                                        );

                        if (which == currentId) {

                            dialog.dismiss();

                            rename(appInfo);

                        } else {

                            sp.save(
                                    dev_mode
                                            ? C.SP_APK_NAME_FORMAT_ID_2
                                            : C.SP_APK_NAME_FORMAT_ID,
                                    which
                            );
                        }
                    }
                }
        );


        builder.setPositiveButton(
                R.string.dia_bt_always,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        sp.save(
                                dev_mode
                                        ? C.SP_REMEMBER_APK_NAME_FORMAT_2
                                        : C.SP_REMEMBER_APK_NAME_FORMAT,
                                true
                        );

                        rename(appInfo);
                    }
                }
        );


        builder.setNegativeButton(
                R.string.dia_bt_once,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        sp.save(
                                dev_mode
                                        ? C.SP_REMEMBER_APK_NAME_FORMAT_2
                                        : C.SP_REMEMBER_APK_NAME_FORMAT,
                                false
                        );

                        rename(appInfo);
                    }
                }
        );


        builder.create().show();
    }


    private void rename(
            AppInfo appInfo) {

        String[] formats =
                getResources().getStringArray(
                        dev_mode
                                ? R.array.apk_names_format_2
                                : R.array.apk_names_format
                );


        int formatId =
                dev_mode
                        ? sp.getInt(
                                C.SP_APK_NAME_FORMAT_ID_2,
                                2
                        )
                        : sp.getInt(
                                C.SP_APK_NAME_FORMAT_ID,
                                1
                        );


        if (formatId < 0 ||
                formatId >= formats.length) {

            return;
        }


        int selectedFormat =
                formatId;


        if (formatId == 4 &&
                ExtraUtil.getBitsOR(
                        hide_flags,
                        C.ENUM_FLAGS.SI.ordinal(),
                        C.ENUM_FLAGS.CF.ordinal(),
                        C.ENUM_FLAGS.CS.ordinal(),
                        C.ENUM_FLAGS.CE.ordinal()
                )) {

            selectedFormat = 3;
        }


        File renamed =
                ExtraUtil.renameApkFileName(
                        appInfo.filePath,
                        appInfo.label,
                        appInfo.packageName,
                        appInfo.verName,
                        appInfo.ver_code,
                        formats[selectedFormat]
                );


        if (renamed != null) {

            if (sp.getLong(
                    C.SP_LAST_APK_SIZE
            ) == renamed.length()) {

                sp.save(
                        C.SP_LAST_APK_NEW,
                        renamed.getPath()
                );

            } else {

                sp.put(
                        C.SP_LAST_APK_OLD,
                        apkFile != null
                                ? apkFile.getPath()
                                : appInfo.filePath
                ).put(
                        C.SP_LAST_APK_NEW,
                        renamed.getPath()
                ).put(
                        C.SP_LAST_APK_SIZE,
                        renamed.length()
                ).write();
            }


            refreshData(renamed);


            Toast.makeText(
                    this,
                    String.format(
                            getString(
                                    R.string.toast_renamed
                            ),
                            renamed.getName()
                    ),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    @TargetApi(11)
    private void batchRenameDescDialog() {

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setTitle(
                        R.string.dia_title_batch_rename
                )
                .setMessage(
                        R.string.dia_batch_rename_desc
                )
                .setPositiveButton(
                        R.string.dia_bt_close,
                        null
                )
                .create()
                .show();
    }


    @TargetApi(11)
    private void makeLinesDialog() {

        final int length =
                C.ENUM_FLAGS.values().length;

        final boolean[] selected =
                new boolean[length];

        final boolean[] allBits =
                ExtraUtil.getAllBits(
                        hide_flags,
                        length
                );


        for (int i = 0;
             i < length;
             i++) {

            selected[i] =
                    allBits[
                            C.ITEMS_FLAG_POS[i]
                    ];
        }


        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setTitle(
                R.string.dia_title_make_lines
        );


        builder.setMultiChoiceItems(
                R.array.items,
                selected,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which,
                            boolean checked) {

                        selected[which] = checked;
                    }
                }
        );


        builder.setPositiveButton(
                R.string.dia_bt_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        for (int i = 0;
                             i < length;
                             i++) {

                            allBits[
                                    C.ITEMS_FLAG_POS[i]
                            ] = selected[i];
                        }


                        hide_flags =
                                ExtraUtil.writeBits(
                                        allBits
                                );


                        sp.save(
                                C.SP_HIDE_FLAGS,
                                hide_flags
                        );


                        Toast.makeText(
                                MainActivity.this,
                                R.string.toast_reboot,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );


        builder.setNeutralButton(
                R.string.dia_bt_reset,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        hide_flags =
                                C.DEFAULT_HIDE_FLAGS;

                        sp.save(
                                C.SP_HIDE_FLAGS,
                                hide_flags
                        );


                        new Handler().postDelayed(
                                new Runnable() {

                                    @Override
                                    public void run() {

                                        if (is_running) {
                                            makeLinesDialog();
                                        }
                                    }
                                },
                                600
                        );
                    }
                }
        );


        builder.setNegativeButton(
                R.string.dia_bt_cancel,
                null
        );


        builder.create().show();


        new Handler().postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        if (!is_running ||
                                sp.getBoolean(
                                        C.SP_SLOW_DOWN_TIP_SHOWED
                                )) {

                            return;
                        }

                        slowDownTipDialog();
                    }
                },
                2000
        );
    }


    @TargetApi(11)
    private void slowDownTipDialog() {

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setMessage(
                        R.string.dia_slow_down_desc
                )
                .setPositiveButton(
                        R.string.dia_bt_no_show,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                sp.save(
                                        C.SP_SLOW_DOWN_TIP_SHOWED,
                                        true
                                );
                            }
                        }
                )
                .create()
                .show();
    }


    @TargetApi(11)
    private void setInstallerDialog() {

        AlertDialog.Builder builder;

        String[] foundInstaller;

        ViewGroup viewGroup =
                (ViewGroup) getLayoutInflater().inflate(
                        (C.SDK >= 11 || !light_theme)
                                ? R.layout.dialog_set_installer
                                : R.layout.dialog_set_installer_light,
                        null
                );


        final Spinner packageSpinner =
                (Spinner) viewGroup.findViewById(
                        R.id.sp_packages
                );

        final Spinner classSpinner =
                (Spinner) viewGroup.findViewById(
                        R.id.sp_classes
                );


        if (!sp.contains(
                C.SP_INSTALLER_PACKAGE_NAME
        ) &&
                (foundInstaller =
                        ExtraUtil.findPackageInstaller(
                                this
                        )) != null) {

            sp.put(
                    C.SP_INSTALLER_PACKAGE_NAME,
                    foundInstaller[0]
            ).put(
                    C.SP_INSTALLER_CLASS_NAME,
                    foundInstaller[1]
            ).write();
        }


        final String[] installer =
                new String[]{
                        sp.getString(
                                C.SP_INSTALLER_PACKAGE_NAME,
                                C.INSTALLERS[0][0]
                        ),
                        sp.getString(
                                C.SP_INSTALLER_CLASS_NAME,
                                C.INSTALLERS[0][1]
                        )
                };


        installer[1] =
                installer[1].replace(
                        installer[0],
                        ""
                );


        final String[] selected =
                new String[2];


        final List<String> packageList =
                new ArrayList<String>();

        final List<String> classList =
                new ArrayList<String>();


        final ArrayAdapter<String> packageAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        packageList
                );

        packageAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        final ArrayAdapter<String> classAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );

        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        packageSpinner.setAdapter(
                (SpinnerAdapter) packageAdapter
        );

        classSpinner.setAdapter(
                (SpinnerAdapter) classAdapter
        );


        packageSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position < 0 ||
                                position >= packageList.size()) {

                            return;
                        }


                        selected[0] =
                                packageList.get(position);


                        classList.clear();


                        classList.addAll(
                                ExtraUtil.getAllExportedClassNames(
                                        MainActivity.this,
                                        selected[0]
                                )
                        );


                        if (selected[0].equals(
                                installer[0]
                        )) {

                            int index =
                                    classList.indexOf(
                                            installer[1]
                                    );

                            if (index < 0) {
                                index = 0;
                            }

                            if (!classList.isEmpty()) {

                                classSpinner.setSelection(
                                        index
                                );
                            }

                        } else if (!classList.isEmpty()) {

                            classSpinner.setSelection(
                                    ExtraUtil
                                            .guessInstallerActivityPos(
                                                    classList
                                            )
                            );
                        }


                        classAdapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                        selected[0] = null;
                    }
                }
        );


        classSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position >= 0 &&
                                position < classList.size()) {

                            selected[1] =
                                    classList.get(position);
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                        selected[1] = null;
                    }
                }
        );


        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setTitle(
                        R.string.dia_title_set_installer
                )
                .setView(viewGroup)
                .setPositiveButton(
                        R.string.dia_bt_ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                if (selected[0] == null ||
                                        selected[1] == null) {

                                    setInstallerDialog();

                                } else {

                                    String className =
                                            selected[1].startsWith(".")
                                                    ? selected[0] +
                                                    selected[1]
                                                    : selected[1];


                                    sp.put(
                                            C.SP_INSTALLER_PACKAGE_NAME,
                                            selected[0]
                                    ).put(
                                            C.SP_INSTALLER_CLASS_NAME,
                                            className
                                    ).write();
                                }
                            }
                        }
                )
                .setNeutralButton(
                        R.string.dia_bt_reset,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                sp.remove(
                                        C.SP_INSTALLER_PACKAGE_NAME
                                ).remove(
                                        C.SP_INSTALLER_CLASS_NAME
                                ).write();


                                new Handler().postDelayed(
                                        new Runnable() {

                                            @Override
                                            public void run() {

                                                if (is_running) {
                                                    setInstallerDialog();
                                                }
                                            }
                                        },
                                        600
                                );
                            }
                        }
                )
                .setNegativeButton(
                        R.string.dia_bt_cancel,
                        null
                )
                .create()
                .show();


        new Handler().post(
                new Runnable() {

                    @Override
                    public void run() {

                        packageList.addAll(
                                ExtraUtil
                                        .getAllInstalledPackageNames(
                                                MainActivity.this,
                                                true
                                        )
                        );


                        int index =
                                packageList.indexOf(
                                        installer[0]
                                );

                        if (index < 0) {
                            index = 0;
                        }


                        if (!packageList.isEmpty()) {

                            packageSpinner.setSelection(
                                    index
                            );
                        }


                        packageAdapter.notifyDataSetChanged();
                    }
                }
        );
    }


    @TargetApi(9)
    private void clearDefaults() {

        if (C.SDK >= 9) {

            Intent intent =
                    new Intent();

            intent.setAction(
                    "android.settings.APPLICATION_DETAILS_SETTINGS"
            );

            intent.setData(
                    Uri.parse(
                            "package:" +
                                    getPackageName()
                    )
            );

            startActivity(intent);

        } else {

            getPackageManager()
                    .clearPackagePreferredActivities(
                            getPackageName()
                    );

            Toast.makeText(
                    this,
                    R.string.toast_cleared_defaults,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    @TargetApi(14)
    private void uninstall(
            String packageName) {

        Intent intent =
                new Intent();

        if (C.SDK >= 14) {

            intent.setAction(
                    "android.intent.action.UNINSTALL_PACKAGE"
            );

        } else {

            intent.setAction(
                    "android.intent.action.DELETE"
            );
        }


        intent.setData(
                Uri.parse(
                        "package:" +
                                packageName
                )
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );


        try {

            startActivity(intent);

        } catch (Throwable e) {

            Log.e(
                    C.LOG_TAG,
                    "Uninstall failed",
                    e
            );

            Toast.makeText(
                    this,
                    R.string.toast_uninstall_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    @TargetApi(11)
    private void helpDialog() {

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        AlertDialog dialog =
                builder.setTitle(
                                R.string.dia_title_help
                        )
                        .setMessage(
                                ExtraUtil.getHelpInfo(this)
                        )
                        .setPositiveButton(
                                R.string.dia_bt_close,
                                null
                        )
                        .setNegativeButton(
                                R.string.dia_bt_market,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {

                                        gotoMarket(
                                                getPackageName(),
                                                false
                                        );
                                    }
                                }
                        )
                        .setNeutralButton(
                                R.string.dia_bt_thanks,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {

                                        thanksDialog();
                                    }
                                }
                        )
                        .create();


        dialog.show();


        Button button =
                dialog.getButton(
                        DialogInterface.BUTTON_POSITIVE
                );


        if (button != null) {

            button.setOnLongClickListener(
                    new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(
                                View v) {

                            v.performClick();

                            activateDevMode();

                            return true;
                        }
                    }
            );
        }
    }


    @TargetApi(11)
    private void thanksDialog() {

        AlertDialog.Builder builder;

        if (C.SDK >= 21) {

            builder =
                    new AlertDialog.Builder(
                            this,
                            light_theme
                                    ? R.style.alert_dialog_style_light
                                    : R.style.alert_dialog_style
                    );

        } else {

            builder =
                    new AlertDialog.Builder(this);
        }


        builder.setTitle(
                        R.string.dia_title_thanks
                )
                .setMessage(
                        R.string.dia_thanks_desc
                )
                .setPositiveButton(
                        R.string.dia_bt_close,
                        null
                )
                .create()
                .show();
    }


    private void gotoMarket(
            String packageName,
            boolean web) {

        if ((!is_running || is_installing) &&
                TextUtils.isEmpty(packageName)) {

            return;
        }

        if (TextUtils.isEmpty(packageName)) {
            return;
        }


        String url =
                String.format(
                        web
                                ? "https://play.google.com/store/apps/details?id=%s"
                                : "market://details?id=%s",
                        packageName
                );


        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW
                );

        intent.setData(
                Uri.parse(url)
        );


        try {

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            Log.d(
                    C.LOG_TAG,
                    String.format(
                            "No any markets to deal with %1$s.",
                            url
                    )
            );

            if (!web) {
                gotoMarket(
                        packageName,
                        true
                );
            }
        }
    }


    @Override
    public void onConfigurationChanged(
            Configuration configuration) {

        super.onConfigurationChanged(
                configuration
        );
    }


    @Override
    public boolean onCreateOptionsMenu(
            Menu menu) {

        getMenuInflater().inflate(
                (C.SDK < 11 || !dev_mode)
                        ? R.menu.menu_main
                        : R.menu.menu_main_dev,
                menu
        );

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(
            Menu menu) {

        if (!dev_mode) {
            return super.onPrepareOptionsMenu(menu);
        }


        if (menu.size() <= 3) {
            return true;
        }


        SubMenu subMenu =
                menu.getItem(3).getSubMenu();


        if (subMenu == null) {
            return true;
        }


        if (C.SDK < 11) {

            subMenu.setGroupVisible(
                    R.id.group_dev_mode,
                    true
            );
        }


        if (subMenu.size() >= 3) {

            subMenu.getItem(0).setChecked(
                    sp.getBoolean(
                            C.SP_LIGHT_THEME
                    )
            );

            subMenu.getItem(1).setChecked(
                    sp.getBoolean(
                            C.SP_BATCH_RENAME
                    )
            );

            subMenu.getItem(2).setChecked(
                    sp.getBoolean(
                            C.SP_EXTRA_LABEL,
                            true
                    )
            );
        }


        return true;
    }


    @Override
    @TargetApi(11)
    public boolean onOptionsItemSelected(
            MenuItem menuItem) {

        int menuId =
                menuItem.getItemId();


        if (menuId == R.id.menu_rename) {

            renameDialog(
                    appInfoUninstalled
            );

            return true;


        } else if (menuId == R.id.menu_save_icon) {

            View scroll =
                    findViewById(
                            R.id.sv_content
                    );

            View include =
                    findViewById(
                            R.id.include_uninstalled
                    );

            if (scroll instanceof ScrollView &&
                    include != null &&
                    ((ScrollView) scroll)
                            .getScrollY()
                            >= include.getMeasuredHeight()) {

                saveIconDialog(
                        appInfoInstalled
                );

            } else {

                saveIconDialog(
                        appInfoUninstalled
                );
            }

            return true;


        } else if (menuId ==
                R.id.menu_view_in_market) {

            if (!TextUtils.isEmpty(
                    appInfoUninstalled.packageName
            )) {

                gotoMarket(
                        appInfoUninstalled.packageName,
                        false
                );

            } else {

                fail_to_goto_market = true;
            }

            return true;


        } else if (menuId ==
                R.id.menu_light_theme) {

            sp.reverseAndSave(
                    C.SP_LIGHT_THEME
            );

            Toast.makeText(
                    this,
                    R.string.toast_reboot,
                    Toast.LENGTH_SHORT
            ).show();

            return true;


        } else if (menuId ==
                R.id.menu_batch_rename) {

            sp.reverseAndSave(
                    C.SP_BATCH_RENAME
            );

            try {

                ExtraUtil.hideComponent(
                        this,
                        new ComponentName(
                                this,
                                Class.forName(
                                        "com.by_syk.apkchecker.BatchRenameActivity"
                                )
                        ),
                        menuItem.isChecked()
                );


                if (!menuItem.isChecked()) {

                    batchRenameDescDialog();

                } else {

                    Toast.makeText(
                            this,
                            R.string.toast_batch_rename_removed,
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } catch (ClassNotFoundException e) {

                throw new NoClassDefFoundError(
                        e.getMessage()
                );
            }

            return true;


        } else if (menuId ==
                R.id.menu_extra_label) {

            sp.reverseAndSave(
                    C.SP_EXTRA_LABEL,
                    true
            );

            Toast.makeText(
                    this,
                    R.string.toast_reboot,
                    Toast.LENGTH_SHORT
            ).show();

            return true;


        } else if (menuId ==
                R.id.menu_make_lines) {

            makeLinesDialog();

            return true;


        } else if (menuId ==
                R.id.menu_set_installer) {

            setInstallerDialog();

            return true;


        } else if (menuId ==
                R.id.menu_clear_defaults) {

            clearDefaults();

            return true;


        } else if (menuId ==
                R.id.menu_uninstall) {

            uninstall(
                    getPackageName()
            );

            return true;


        } else if (menuId ==
                R.id.menu_help) {

            helpDialog();

            return true;


        } else {

            return super.onOptionsItemSelected(
                    menuItem
            );
        }
    }
}
