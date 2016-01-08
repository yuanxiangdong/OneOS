package com.eli.oneos;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static Context context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create MyApplication");
        MyApplication.context = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "On Terminate");
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}
