package com.eli.oneos.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.eli.oneos.R;
import com.eli.oneos.model.user.LoginManager;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BaseNavFragment mCurNavFragment;

    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkStateManager.getInstance().setOnNetworkStateChangedListener(mNetworkListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "on destroy.");
        NetworkStateManager.getInstance().removeOnNetworkStateChangedListener(mNetworkListener);
    }
}
