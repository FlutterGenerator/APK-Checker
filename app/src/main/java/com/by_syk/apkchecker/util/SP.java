package com.by_syk.apkchecker.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes.dex */
public class SP {
    private SharedPreferences.Editor editor = (SharedPreferences.Editor) null;
    private SharedPreferences sharedPreferences;

    public SP(Context context) {
        this.sharedPreferences = (SharedPreferences) null;
        this.sharedPreferences = context.getSharedPreferences(context.getPackageName(), 0);
    }

    public boolean save(String str, boolean z) {
        if (this.sharedPreferences == null || str == null) {
            return false;
        }
        if (this.editor == null) {
            this.editor = this.sharedPreferences.edit();
        }
        this.editor.putBoolean(str, z);
        return write();
    }

    public boolean reverseAndSave(String str, boolean z) {
        if (this.sharedPreferences == null || str == null) {
            return false;
        }
        if (this.editor == null) {
            this.editor = this.sharedPreferences.edit();
        }
        this.editor.putBoolean(str, getBoolean(str, z) ? false : true);
        return write();
    }

    public boolean reverseAndSave(String str) {
        return reverseAndSave(str, false);
    }

    public SP put(String str, boolean z) {
        if (this.sharedPreferences != null && str != null) {
            if (this.editor == null) {
                this.editor = this.sharedPreferences.edit();
            }
            this.editor.putBoolean(str, z);
        }
        return this;
    }

    public boolean save(String str, int i) {
        if (this.sharedPreferences == null || str == null) {
            return false;
        }
        if (this.editor == null) {
            this.editor = this.sharedPreferences.edit();
        }
        this.editor.putInt(str, i);
        return write();
    }

    public SP put(String str, int i) {
        if (this.sharedPreferences != null && str != null) {
            if (this.editor == null) {
                this.editor = this.sharedPreferences.edit();
            }
            this.editor.putInt(str, i);
        }
        return this;
    }

    public SP put(String str, long j) {
        if (this.sharedPreferences != null && str != null) {
            if (this.editor == null) {
                this.editor = this.sharedPreferences.edit();
            }
            this.editor.putLong(str, j);
        }
        return this;
    }

    public boolean save(String str, String str2) {
        if (this.sharedPreferences == null || str == null) {
            return false;
        }
        if (this.editor == null) {
            this.editor = this.sharedPreferences.edit();
        }
        this.editor.putString(str, str2);
        return write();
    }

    public SP put(String str, String str2) {
        if (this.sharedPreferences != null && str != null) {
            if (this.editor == null) {
                this.editor = this.sharedPreferences.edit();
            }
            this.editor.putString(str, str2);
        }
        return this;
    }

    @TargetApi(9)
    public boolean write() {
        if (this.editor == null) {
            return false;
        }
        if (C.SDK >= 9) {
            this.editor.apply();
            return true;
        }
        return this.editor.commit();
    }

    public boolean getBoolean(String str, boolean z) {
        return (this.sharedPreferences == null || str == null || !this.sharedPreferences.getBoolean(str, z)) ? false : true;
    }

    public boolean getBoolean(String str) {
        return getBoolean(str, false);
    }

    public int getInt(String str, int i) {
        if (this.sharedPreferences == null || str == null) {
            return 0;
        }
        return this.sharedPreferences.getInt(str, i);
    }

    public long getLong(String str, long j) {
        if (this.sharedPreferences == null || str == null) {
            return 0L;
        }
        return this.sharedPreferences.getLong(str, j);
    }

    public long getLong(String str) {
        return getLong(str, 0L);
    }

    public String getString(String str, String str2) {
        return (this.sharedPreferences == null || str == null) ? "" : this.sharedPreferences.getString(str, str2);
    }

    public String getString(String str) {
        return getString(str, "");
    }

    public boolean contains(String str) {
        return (this.sharedPreferences == null || str == null || !this.sharedPreferences.contains(str)) ? false : true;
    }

    public SP remove(String str) {
        if (this.sharedPreferences != null && str != null) {
            if (this.editor == null) {
                this.editor = this.sharedPreferences.edit();
            }
            this.editor.remove(str);
        }
        return this;
    }
}
