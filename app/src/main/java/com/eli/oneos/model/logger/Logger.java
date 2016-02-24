package com.eli.oneos.model.logger;

import android.util.Log;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class Logger {

    /**
     * Mack Log for tester.
     *
     * @param level
     * @param TAG
     * @param msg
     */
    public static void p(LogLevel level, String TAG, String msg) {
        if (level == LogLevel.ERROR) {
            e(TAG, msg);
        } else if (level == LogLevel.WARN) {
            w(TAG, msg);
        } else if (level == LogLevel.INFO) {
            i(TAG, msg);
        } else if (level == LogLevel.DEBUG) {
            d(TAG, msg);
        } else {
            v(TAG, msg);
        }
    }

    private static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    private static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    private static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    private static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    private static void v(String tag, String msg) {
        Log.v(tag, msg);
    }
}
