package com.by_syk.apkchecker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.by_syk.apkchecker.util.C;
import com.by_syk.apkchecker.util.ExtraUtil;
import com.by_syk.apkchecker.util.SP;

/* JADX INFO: loaded from: classes.dex */
public class HelloActivity extends Activity {
    SP sp = (SP) null;
    boolean warning_showed = false;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_hello);
        init();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        hide();
        super.onDestroy();
    }

    private void init() {
        this.sp = new SP(this);
        ((TextView) findViewById(R.id.tv_help)).setText(ExtraUtil.getHelpInfo(this));

        findViewById(R.id.bt_i_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelloActivity.this.warningDialog();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    @TargetApi(11)
    public void warningDialog() {
        if (this.warning_showed) {
            ExtraUtil.cleanlyExit(this);
            return;
        }
        this.warning_showed = true;
        hide();
        AlertDialog alertDialogCreate = (C.SDK >= 21 ? new AlertDialog.Builder(this, R.style.alert_dialog_style) : new AlertDialog.Builder(this)).setMessage(R.string.hello_warning).setPositiveButton(R.string.i_know, (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.by_syk.apkchecker.HelloActivity.100000001

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                ExtraUtil.cleanlyExit(HelloActivity.this);
            }
        });
        alertDialogCreate.show();
    }

    private void hide() {
        ExtraUtil.hideComponent(this, getComponentName(), true);
        this.sp.save(C.SP_HIDE_ICON, true);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        warningDialog();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }
}
