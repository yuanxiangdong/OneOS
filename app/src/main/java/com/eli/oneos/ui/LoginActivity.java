package com.eli.oneos.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceHistoryKeeper;
import com.eli.oneos.db.UserHistoryKeeper;
import com.eli.oneos.db.greendao.DeviceHistory;
import com.eli.oneos.db.greendao.UserHistory;
import com.eli.oneos.model.api.OneOSLoginAPI;
import com.eli.oneos.model.scan.OnScanDeviceListener;
import com.eli.oneos.model.scan.ScanDeviceManager;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.ClearEditText;
import com.eli.oneos.widget.SpinnerView;
import com.eli.oneos.widget.TitleBackLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Activity for User Login OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private ClearEditText mUserTxt, mPwdTxt, mPortTxt;
    private Button mLoginBtn, mMoreUserBtn, mMoreIpBtn;
    private RelativeLayout mUserLayout, mIPLayout;
    private EditText mIPTxt;

    private DeviceHistory mLastLoginDevice;
    private List<UserHistory> mUserHistoryList = new ArrayList<UserHistory>();
    private List<DeviceHistory> mDeviceList = new ArrayList<DeviceHistory>();
    private SpinnerView mUserSpinnerView, mDeviceSpinnerView;
    private ScanDeviceManager mScanManager = new ScanDeviceManager(this, new OnScanDeviceListener() {
        @Override
        public void onScanStart() {
            showLoading(R.string.scanning_device);
        }

        @Override
        public void onScanning(String mac, String ip) {
            checkIfLastLoginDevice(mac, ip);
        }

        @Override
        public void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp) {
            dismissLoading();

            Iterator<String> iter = mDeviceMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = mDeviceMap.get(key);
                DeviceHistory deviceHistory = new DeviceHistory(value, key, OneOSAPIs.ONE_API_DEFAULT_PORT, System.currentTimeMillis(), true);
                mDeviceList.add(deviceHistory);
            }
        }
    });

    private View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attempLogin();
        }
    };
    private View.OnClickListener onMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_more_user:
                    showUserSpinnerView(mUserLayout);
                    break;
                case R.id.btn_more_ip:
                    showDeviceSpinnerView(mIPLayout);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initStatusBarStyle();

        initView();
        initLoginHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanManager.stop();
    }

    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_login);

        mUserLayout = (RelativeLayout) findViewById(R.id.layout_user);
        mUserTxt = (ClearEditText) findViewById(R.id.editext_user);
        mMoreUserBtn = (Button) findViewById(R.id.btn_more_user);
        mMoreUserBtn.setOnClickListener(onMoreClickListener);
        mPwdTxt = (ClearEditText) findViewById(R.id.editext_pwd);
        mPortTxt = (ClearEditText) findViewById(R.id.editext_port);
        mIPLayout = (RelativeLayout) findViewById(R.id.layout_server);
        mIPTxt = (EditText) findViewById(R.id.editext_ip);
        mMoreIpBtn = (Button) findViewById(R.id.btn_more_ip);
        mMoreIpBtn.setOnClickListener(onMoreClickListener);
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(onLoginClickListener);
    }

    private void showUserSpinnerView(View view) {
        if (mUserSpinnerView != null && mUserSpinnerView.isShown()) {
            mUserSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mUserHistoryList)) {
                mUserSpinnerView = new SpinnerView(this, view.getWidth());
                ArrayList<String> users = new ArrayList<String>();
                ArrayList<Integer> icons = new ArrayList<Integer>();
                for (UserHistory info : mUserHistoryList) {
                    users.add(info.getName());
                    icons.add(R.drawable.ic_btn_clear);
                }

                mUserSpinnerView.addSpinnerItems(users, icons);
                mUserSpinnerView.setOnSpinnerButtonClickListener(new SpinnerView.OnSpinnerButtonClickListener() {
                    @Override
                    public void onClick(View view, int index) {
                        UserHistory UserHistory = mUserHistoryList.get(index);
                        mUserHistoryList.remove(UserHistory);
                        UserHistoryKeeper.delete(UserHistory);
                        mUserSpinnerView.dismiss();
                    }
                });
                mUserSpinnerView.setOnSpinnerItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UserHistory UserHistory = mUserHistoryList.get(position);
                        mUserTxt.setText(UserHistory.getName());
                        mPwdTxt.setText(UserHistory.getPwd());
                        mUserSpinnerView.dismiss();
                    }
                });
                mUserSpinnerView.showPopupDown(view);
            }
        }
    }
    private void showDeviceSpinnerView(View view) {
        if (mDeviceSpinnerView != null && mDeviceSpinnerView.isShown()) {
            mDeviceSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mDeviceList)) {
                mDeviceSpinnerView = new SpinnerView(this, view.getWidth());
                ArrayList<String> users = new ArrayList<String>();
                ArrayList<Integer> icons = new ArrayList<Integer>();
                for (DeviceHistory info : mDeviceList) {
                    users.add(info.getIp());
                    icons.add(R.drawable.ic_btn_clear);
                }

                mDeviceSpinnerView.addSpinnerItems(users, icons);
                mDeviceSpinnerView.setOnSpinnerButtonClickListener(new SpinnerView.OnSpinnerButtonClickListener() {
                    @Override
                    public void onClick(View view, int index) {
                        DeviceHistory deviceHistory = mDeviceList.get(index);
//                        mUserHistoryList.remove(deviceHistory);
//                        DeviceHistoryKeeper.delete(deviceHistory);
                        mDeviceSpinnerView.dismiss();
                    }
                });
                mDeviceSpinnerView.setOnSpinnerItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DeviceHistory deviceHistory = mDeviceList.get(position);
                        mIPTxt.setText(deviceHistory.getIp());
                        mPortTxt.setText(deviceHistory.getPort());
                        mDeviceSpinnerView.dismiss();
                    }
                });
                mDeviceSpinnerView.showPopupDown(view);
            }
        }
    }

    private void initLoginHistory() {
        List<UserHistory> userList = UserHistoryKeeper.all();
        if (!EmptyUtils.isEmpty(userList)) {
            mUserHistoryList.addAll(userList);
            UserHistory UserHistory = userList.get(0);
            mUserTxt.setText(UserHistory.getName());
            mPwdTxt.setText(UserHistory.getPwd());
        }

        List<DeviceHistory> deviceList = DeviceHistoryKeeper.all();
        if (!EmptyUtils.isEmpty(deviceList)) {
            mDeviceList.addAll(deviceList);
            mLastLoginDevice = mDeviceList.get(0);
        }
    }

    private void attempLogin() {
        String user = mUserTxt.getText().toString();
        if (EmptyUtils.isEmpty(user)) {
            AnimUtils.sharkEditText(LoginActivity.this, mUserTxt);
            mUserTxt.requestFocus();
            return;
        }

        String pwd = mPwdTxt.getText().toString();
        if (EmptyUtils.isEmpty(pwd)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPwdTxt);
            mPwdTxt.requestFocus();
            return;
        }

        String ip = mIPTxt.getText().toString();
        if (EmptyUtils.isEmpty(ip)) {
            AnimUtils.sharkEditText(LoginActivity.this, mIPTxt);
            mIPTxt.requestFocus();
            return;
        }

        String port = mPortTxt.getText().toString();
        if (EmptyUtils.isEmpty(port)) {
            port = OneOSAPIs.ONE_API_DEFAULT_PORT;
            mPortTxt.setText(port);
        } else if (!Utils.checkPort(port)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPortTxt);
            mPortTxt.requestFocus();
            ToastHelper.showToast(R.string.tip_invaild_port);
            return;
        }

        String mac = getLANDeviceMacByIP(ip);
        doLogin(user, pwd, ip, port, mac);
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac) {
        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.loginning, false);
            }

            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                dismissLoading();
                ToastHelper.showToast("Login Success!");
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                ToastHelper.showToast(errorMsg);
            }
        });
        loginAPI.login();
    }

    private String getLANDeviceMacByIP(String ip) {
        for (DeviceHistory info : mDeviceList) {
            if (info.getIp().equals(ip)) {
                return info.getMac();
            }
        }

        return null;
    }

    private boolean checkIfLastLoginDevice(String mac, String ip) {
        if (mLastLoginDevice == null) {
            return false;
        }

        boolean isLast = false;
        String perferMac = mLastLoginDevice.getMac();
        if (!TextUtils.isEmpty(perferMac)) {
            if (perferMac.equalsIgnoreCase(mac)) {
                mIPTxt.setText(ip);
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
                isLast = true;
            }
        }

        return isLast;
    }
}
