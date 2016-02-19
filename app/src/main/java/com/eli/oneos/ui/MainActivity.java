package com.eli.oneos.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.ui.nav.cloud.CloudNavFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACTION_SHOW_TRANSFER_DOWNLOAD = "action_show_transfer_download";
    public static final String ACTION_SHOW_TRANSFER_UPLOAD = "action_show_transfer_upload";

    private List<BaseNavFragment> mFragmentList = new ArrayList<>();
    private BaseNavFragment mCurNavFragment;
    private RadioGroup radioGroup;
    private FragmentManager fragmentManager;
    private int mCurPageIndex = 1;

    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginManage mLoginManager = LoginManage.getInstance();
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
        initSystemBarStyle();

        initViews();
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

    @Override
    public void onBackPressed() {
        if (mCurNavFragment != null && mCurNavFragment.onBackPressed()) {
            return;
        }

        super.onBackPressed();
    }

    /**
     * Init Views
     */
    private void initViews() {
        mRootView = findViewById(R.id.layout_root);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radio_local:
                        mCurPageIndex = 0;
                        break;
                    case R.id.radio_cloud:
                        mCurPageIndex = 1;
                        break;
                    case R.id.radio_transfer:
                        mCurPageIndex = 2;
                        break;
                    case R.id.radio_tool:
                        mCurPageIndex = 3;
                        break;
                    default:
                        break;
                }
                Log.d(TAG, "onCheckedChanged: " + mCurPageIndex);
                changFragmentByIndex(mCurPageIndex);
            }
        });

        fragmentManager = getSupportFragmentManager();

        CloudNavFragment cloudFragment = new CloudNavFragment();
        mFragmentList.add(cloudFragment);
        mFragmentList.add(cloudFragment);
        mFragmentList.add(cloudFragment);
        mFragmentList.add(cloudFragment);
        changFragmentByIndex(mCurPageIndex);
    }

    private void changFragmentByIndex(int index) {
        Log.d(TAG, "changFragmentByIndex: " + index);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        BaseNavFragment fragment = getFragmentByIndex(mCurPageIndex);

        if (mCurNavFragment != null) {
            mCurNavFragment.onPause();
        }

        if (!fragment.isAdded()) {
            transaction.add(R.id.content, fragment);
        } else {
            fragment.onResume();
        }

        for (BaseNavFragment ft : mFragmentList) {
            if (fragment == ft) {
                transaction.show(ft);
                mCurNavFragment = fragment;
            } else {
                transaction.hide(ft);
            }
        }

        transaction.commitAllowingStateLoss();
    }

    public BaseNavFragment getFragmentByIndex(int index) {
        BaseNavFragment fragment = mFragmentList.get(index);
        Log.d(TAG, "Get Fragment By Index: " + index);
        return fragment;
    }

    @Override
    public boolean controlActivity(String action) {
        if (action.equals(ACTION_SHOW_TRANSFER_DOWNLOAD)) {

            return true;
        } else if (action.equals(ACTION_SHOW_TRANSFER_UPLOAD)) {

            return true;
        }

        return false;
    }

    public void showNavBar() {
        radioGroup.setVisibility(View.VISIBLE);
    }

    public void hideNavBar(boolean isGone) {
        radioGroup.setVisibility(isGone ? View.GONE : View.INVISIBLE);
    }
}
