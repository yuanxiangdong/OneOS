package com.eli.oneos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.scan.OnScanDeviceListener;
import com.eli.oneos.model.oneos.scan.ScanDeviceManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
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

    private EditText mUserTxt, mPwdTxt, mPortTxt;
    private Button mLoginBtn, mMoreUserBtn, mMoreIpBtn;
    private RelativeLayout mUserLayout, mIPLayout;
    private EditText mIPTxt;

    private LoginSession mLoginSession;
    private UserInfo mLastLoginUser;
    private List<UserInfo> mHistoryUserList = new ArrayList<UserInfo>();
    private List<DeviceInfo> mHistoryDeviceList = new ArrayList<DeviceInfo>();
    private List<DeviceInfo> mLANDeviceList = new ArrayList<DeviceInfo>();
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

            mLANDeviceList.clear();
            Iterator<String> iterator = mDeviceMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = mDeviceMap.get(key);
                DeviceInfo DeviceInfo = new DeviceInfo(key, value, OneOSAPIs.ONE_API_DEFAULT_PORT, true, System.currentTimeMillis());
                mLANDeviceList.add(DeviceInfo);
            }
        }
    });
    private boolean isWifiAvailable = true;
    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginActivity.this.isWifiAvailable = isWifiAvailable;
            if (!isWifiAvailable) {
                ToastHelper.showToast(R.string.wifi_not_available);
            }
        }
    };

    private View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attemptLogin();
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
                    if (EmptyUtils.isEmpty(mLANDeviceList)) {
                        if (isWifiAvailable) {
                            DialogUtils.showConfirmDialog(LoginActivity.this, R.string.tip_title_research, R.string.tip_search_again,
                                    R.string.research_now, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                                        @Override
                                        public void onClick(boolean isPositiveBtn) {
                                            if (isPositiveBtn) {
                                                mScanManager.start();
                                            } else {
                                                showDeviceSpinnerView(mIPLayout);
                                            }
                                        }
                                    });
                        } else {
                            DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tip, R.string.wifi_not_available, R.string.ok, null);
                        }
                        return;
                    } else {
                        showDeviceSpinnerView(mIPLayout);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initSystemBarStyle();

        initView();
        initLoginHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkStateManager.getInstance().setOnNetworkStateChangedListener(mNetworkListener);
        mScanManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mUserSpinnerView) {
            mUserSpinnerView.dismiss();
        }
        if (null != mDeviceSpinnerView) {
            mDeviceSpinnerView.dismiss();
        }
        LoginManage loginManager = LoginManage.getInstance();
        loginManager.setLoginSession(mLoginSession);
        OneSpaceService service = MyApplication.getTransferService();
        service.startBackupFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkStateManager.getInstance().removeOnNetworkStateChangedListener(mNetworkListener);
        mScanManager.stop();
    }

    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_login);

        mUserLayout = (RelativeLayout) findViewById(R.id.layout_user);
        mUserTxt = (EditText) findViewById(R.id.editext_user);
        mMoreUserBtn = (Button) findViewById(R.id.btn_more_user);
        mMoreUserBtn.setOnClickListener(onMoreClickListener);
        mPwdTxt = (EditText) findViewById(R.id.editext_pwd);
        mPortTxt = (EditText) findViewById(R.id.editext_port);
        mPortTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (attemptLogin()) {
                        InputMethodUtils.hideKeyboard(LoginActivity.this, mPortTxt);
                    }
                }

                return true;
            }
        });
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
            if (!EmptyUtils.isEmpty(mHistoryUserList)) {
                mUserSpinnerView = new SpinnerView(this, view.getWidth());
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Integer> icons = new ArrayList<>();
                for (UserInfo info : mHistoryUserList) {
                    users.add(info.getName());
                    icons.add(R.drawable.btn_clear);
                }

                mUserSpinnerView.addSpinnerItems(users, icons);
                mUserSpinnerView.setOnSpinnerButtonClickListener(new SpinnerView.OnSpinnerButtonClickListener() {
                    @Override
                    public void onClick(View view, int index) {
                        UserInfo UserHistory = mHistoryUserList.get(index);
                        mHistoryUserList.remove(UserHistory);
                        UserInfoKeeper.unActive(UserHistory);
                        mUserSpinnerView.dismiss();
                    }
                });
                mUserSpinnerView.setOnSpinnerItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UserInfo UserHistory = mHistoryUserList.get(position);
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
            if (!EmptyUtils.isEmpty(mLANDeviceList) || !EmptyUtils.isEmpty(mHistoryDeviceList)) {
                mDeviceSpinnerView = new SpinnerView(this, view.getWidth());
                final ArrayList<DeviceInfo> mDeviceList = new ArrayList<>();
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Integer> icons = new ArrayList<>();
                for (DeviceInfo info : mLANDeviceList) {
                    mDeviceList.add(info);
                    users.add(info.getIp());
                    icons.add(R.drawable.btn_clear);
                }
                for (DeviceInfo info : mHistoryDeviceList) {
                    mDeviceList.add(info);
                    users.add(info.getIp());
                    icons.add(R.drawable.btn_clear);
                }
                mDeviceSpinnerView.addSpinnerItems(users, icons);
                mDeviceSpinnerView.setOnSpinnerButtonClickListener(new SpinnerView.OnSpinnerButtonClickListener() {
                    @Override
                    public void onClick(View view, int index) {
                        DeviceInfo device = mDeviceList.get(index);
                        if (device.getIsLAN()) {
                            mLANDeviceList.remove(device);
                        } else {
                            mHistoryUserList.remove(device);
                            DeviceInfoKeeper.delete(device);
                        }

                        mDeviceSpinnerView.dismiss();
                    }
                });
                mDeviceSpinnerView.setOnSpinnerItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DeviceInfo DeviceInfo = mLANDeviceList.get(position);
                        mIPTxt.setText(DeviceInfo.getIp());
                        mPortTxt.setText(DeviceInfo.getPort());
                        mDeviceSpinnerView.dismiss();
                    }
                });
                mDeviceSpinnerView.showPopupDown(view);
            }
        }
    }

    private void initLoginHistory() {
        List<UserInfo> userList = UserInfoKeeper.activeUsers();
        if (!EmptyUtils.isEmpty(userList)) {
            mHistoryUserList.addAll(userList);
            mLastLoginUser = userList.get(0);
            mUserTxt.setText(mLastLoginUser.getName());
            mPwdTxt.setText(mLastLoginUser.getPwd());
        }

        List<DeviceInfo> deviceList = DeviceInfoKeeper.all();
        if (!EmptyUtils.isEmpty(deviceList)) {
            mHistoryDeviceList.addAll(deviceList);
        }
    }

    private boolean attemptLogin() {
        String user = mUserTxt.getText().toString();
        if (EmptyUtils.isEmpty(user)) {
            AnimUtils.sharkEditText(LoginActivity.this, mUserTxt);
            AnimUtils.focusToEnd(mUserTxt);
            return false;
        }

        String pwd = mPwdTxt.getText().toString();
        if (EmptyUtils.isEmpty(pwd)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPwdTxt);
            AnimUtils.focusToEnd(mPwdTxt);
            return false;
        }

        String ip = mIPTxt.getText().toString();
        if (EmptyUtils.isEmpty(ip)) {
            AnimUtils.sharkEditText(LoginActivity.this, mIPTxt);
            AnimUtils.focusToEnd(mIPTxt);
            return false;
        }

        String port = mPortTxt.getText().toString();
        if (EmptyUtils.isEmpty(port)) {
            port = OneOSAPIs.ONE_API_DEFAULT_PORT;
            mPortTxt.setText(port);
        } else if (!Utils.checkPort(port)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPortTxt);
            AnimUtils.focusToEnd(mPortTxt);
            ToastHelper.showToast(R.string.tip_invalid_port);
            return false;
        }

        String mac = getLANDeviceMacByIP(ip);
        doLogin(user, pwd, ip, port, mac);
        return true;
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac) {
        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.logining, false);
            }

            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                dismissLoading();
                mLoginSession = loginSession;
                gotoMainActivity();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                ToastHelper.showToast(errorMsg);
            }
        });
        loginAPI.login();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String getLANDeviceMacByIP(String ip) {
        for (DeviceInfo info : mLANDeviceList) {
            if (info.getIp().equals(ip)) {
                return info.getMac();
            }
        }

        return null;
    }

    private boolean checkIfLastLoginDevice(String mac, String ip) {
        if (mLastLoginUser == null) {
            return false;
        }

        boolean isLast = false;
        String perferMac = mLastLoginUser.getMac();
        if (!EmptyUtils.isEmpty(perferMac)) {
            if (perferMac.equalsIgnoreCase(mac)) {
                mIPTxt.setText(ip);
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
                isLast = true;
            }
        }

        return isLast;
    }
}
