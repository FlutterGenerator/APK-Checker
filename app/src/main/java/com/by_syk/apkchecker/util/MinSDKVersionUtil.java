package com.by_syk.apkchecker.util;

import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class MinSDKVersionUtil {
    public static int getMinSdkVersion(File file) {
        if (file == null) {
            return -1;
        }
        try {
            XmlResourceParser parserForManifest = getParserForManifest(file);
            if (parserForManifest == null) {
                return -1;
            }
            while (parserForManifest.next() != 1) {
                if (parserForManifest.getEventType() == 2 && "uses-sdk".equals(parserForManifest.getName())) {
                    for (int i = 0; i < parserForManifest.getAttributeCount(); i++) {
                        if (parserForManifest.getAttributeName(i).equals("minSdkVersion")) {
                            return parserForManifest.getAttributeIntValue(i, -1);
                        }
                    }
                }
            }
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            return -1;
        }
    }

    private static XmlResourceParser getParserForManifest(File file) throws IOException {
        Object assetManager = getAssetManager();
        return ((AssetManager) assetManager).openXmlResourceParser(addAssets(file, assetManager), "AndroidManifest.xml");
    }

    private static int addAssets(File file, Object obj) {
        try {
            Class<?> cls = obj.getClass();
            Class<?>[] clsArr = new Class[1];
            try {
                clsArr[0] = Class.forName("java.lang.String");
                return ((Integer) cls.getMethod("addAssetPath", clsArr).invoke(obj, file.getAbsolutePath())).intValue();
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return -1;
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            return -1;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return -1;
        }
    }

    private static Object getAssetManager() {
        try {
            return Class.forName("android.content.res.AssetManager").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return (Object) null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return (Object) null;
        } catch (InstantiationException e3) {
            e3.printStackTrace();
            return (Object) null;
        }
    }
}
