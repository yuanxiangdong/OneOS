package com.eli.oneos.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.eli.oneos.MyApplication;

/**
 * Created by Administrator on 2016/1/12.
 */
public class AppVersionUtils {
    /**
     * get app version
     *
     * @return app version toPath
     */
    public static String getAppVersion() {
        String curVersion = null;
        try {
            PackageManager packageManager = MyApplication.getAppContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(MyApplication.getAppContext()
                    .getPackageName(), 0);
            curVersion = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Utils", "get current version toPath failed");
        }

        return curVersion;
    }

    /**
     * format output app version toPath
     *
     * @return format app version toPath
     */
    public static String formatAppVersion(String versionName) {
        if (versionName != null) {
            versionName = "V " + versionName;// + " beta";
        }

        return versionName;
    }
}
