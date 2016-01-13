package com.eli.oneos.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.eli.oneos.R;
import com.eli.oneos.model.user.LoginManager;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BaseNavFragment mCurNavFragment;


    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = mManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifiAvailable = wifiNetInfo.isAvailable();
            boolean isAvailable = isWifiAvailable || mobNetInfo.isAvailable();

            LoginManager mLoginManager = LoginManager.getInstance();
            if (mLoginManager.isLogin()) {
                boolean isLANDevice = mLoginManager.isLANDevice();
                if (isLANDevice) {
                    if (!isWifiAvailable) {
                        DialogUtils.showNotifyDialog(MainActivity.this, R.string.tip, R.string.wifi_not_available, R.string.ok, null);
                    }
                } else {
                    if (!isAvailable) {
                        DialogUtils.showNotifyDialog(MainActivity.this, R.string.tip, R.string.network_not_available, R.string.ok, null);
                    }
                }
            } else {
                if (!isAvailable) {
                    ToastHelper.showToast(R.string.network_not_available);
                }
            }

            if (mCurNavFragment != null) {
                mCurNavFragment.onNetworkChanged(isAvailable, isWifiAvailable);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initStatusBarStyle();

        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "on destroy.");
        unregisterNetworkReceiver();
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mNetworkReceiver, filter);
    }

    private void unregisterNetworkReceiver() {
        this.unregisterReceiver(mNetworkReceiver);
    }
}
