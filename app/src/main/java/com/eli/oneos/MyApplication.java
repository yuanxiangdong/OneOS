package com.eli.oneos;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.eli.oneos.service.OneSpaceService;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static Context context = null;

    private static boolean mIsServiceBound = false;
    private static OneSpaceService mTransferService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "service connected, get download manager service instance");
            OneSpaceService.ServiceBinder binder = (OneSpaceService.ServiceBinder) service;
            mTransferService = binder.getService();

            mIsServiceBound = true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create MyApplication");
        MyApplication.context = getApplicationContext();
        bindTransferService();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "On Terminate");
        unBindTrasferService();
    }


    /** bind transfer service for download and upload */
    private void bindTransferService() {
        Log.i(TAG, "Bind Transfer Service");
        Intent intent = new Intent(this, OneSpaceService.class);
        if (this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "bind service success");
        } else {
            Log.e(TAG, "bind service fialed");
        }
    }

    private void unBindTrasferService() {
        if (mIsServiceBound) {
            this.unbindService(mConnection);
            mIsServiceBound = false;
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static OneSpaceService getTransferService() {
        if (mIsServiceBound && mTransferService != null) {
            return mTransferService;
        }

        return null;
    }
}
