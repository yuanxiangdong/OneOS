package com.eli.oneos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.eli.oneos.MyApplication;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class NetworkStateManager {

    private static NetworkStateManager INSTANCE = new NetworkStateManager();
    private static BroadcastReceiver mNetworkReceiver = null;
    private OnNetworkStateChangedListener listener;

    private NetworkStateManager() {
        mNetworkReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isWifiAvailable = wifiNetInfo.isAvailable();
                boolean isAvailable = isWifiAvailable || mobNetInfo.isAvailable();
                if (null != listener) {
                    listener.onChanged(isAvailable, isWifiAvailable);
                }
            }
        };
        registerNetworkReceiver();
    }

    public static NetworkStateManager getInstance() {
        return NetworkStateManager.INSTANCE;
    }

    private void registerNetworkReceiver() {
        Context context = MyApplication.getAppContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mNetworkReceiver, filter);
    }

    public void unregisterNetworkReceiver() {
        Context context = MyApplication.getAppContext();
        context.unregisterReceiver(mNetworkReceiver);
    }

    public void setOnNetworkStateChangedListener(OnNetworkStateChangedListener listener) {
        this.listener = listener;
    }

    public void removeOnNetworkStateChangedListener(OnNetworkStateChangedListener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    public interface OnNetworkStateChangedListener {
        void onChanged(boolean isAvailable, boolean isWifiAvailable);
    }
}
