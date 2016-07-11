package com.eli.oneos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.eli.oneos.MyApplication;

import java.util.ArrayList;
import java.util.List;

import www.glinkwin.com.glink.ssudp.SSUDPConst;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class NetworkStateManager {

    private static NetworkStateManager INSTANCE = new NetworkStateManager();
    private static BroadcastReceiver mNetworkReceiver = null;
    private List<OnNetworkStateChangedListener> listenerList = new ArrayList<>();

    private NetworkStateManager() {
        mNetworkReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (null != action && action.equals(SSUDPConst.BROADCAST_ACTION_SSUDP)) {
                    boolean connect = intent.getBooleanExtra(SSUDPConst.EXTRA_SSUDP_STATE, true);
                    for (OnNetworkStateChangedListener listener : listenerList) {
                        listener.onSSUDPChanged(connect);
                    }
                } else {
                    ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    NetworkInfo wifiNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    boolean isWifiAvailable = wifiNetInfo.isAvailable();
                    boolean isAvailable = isWifiAvailable || mobNetInfo.isAvailable();
                    for (OnNetworkStateChangedListener listener : listenerList) {
                        listener.onChanged(isAvailable, isWifiAvailable);
                    }
                }
            }
        };
        registerNetworkReceiver();
    }

    public static NetworkStateManager getInstance() {
        return NetworkStateManager.INSTANCE;
    }

    public static void onDestroy() {
        NetworkStateManager.INSTANCE.unregisterNetworkReceiver();
    }

    private void registerNetworkReceiver() {
        Context context = MyApplication.getAppContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(SSUDPConst.BROADCAST_ACTION_SSUDP);
        context.registerReceiver(mNetworkReceiver, filter);
    }

    private void unregisterNetworkReceiver() {
        if (null != mNetworkReceiver) {
            Context context = MyApplication.getAppContext();
            context.unregisterReceiver(mNetworkReceiver);
        }
    }

    public void addNetworkStateChangedListener(OnNetworkStateChangedListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public void removeNetworkStateChangedListener(OnNetworkStateChangedListener listener) {
        this.listenerList.remove(listener);
    }

    public interface OnNetworkStateChangedListener {
        void onChanged(boolean isAvailable, boolean isWifiAvailable);

        void onSSUDPChanged(boolean isConnect);
    }
}
