package com.eli.oneos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.lib.magicdialog.MagicDialog;
import com.eli.lib.magicdialog.OnMagicDialogClickCallback;
import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.api.OneOSSSUDPClientIDAPI;
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

import www.glinkwin.com.glink.ssudp.SSUDPManager;

/**
 * Activity for User Login OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mUserTxt, mPwdTxt, mPortTxt;
    private Button mLoginBtn;
    private ImageButton mMoreUserBtn, mMoreIpBtn;
    private RelativeLayout mUserLayout, mIPLayout, mPortLayout;
    private EditText mIPTxt;

    private Intent uploadIntent = null;
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
                DeviceInfo DeviceInfo = new DeviceInfo(key, null, value, OneOSAPIs.ONE_API_DEFAULT_PORT, null, null, null, null,
                        Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis());
                mLANDeviceList.add(DeviceInfo);
            }
        }
    });
    private boolean isWifiAvailable = true;
    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginActivity.this.isWifiAvailable = isWifiAvailable;
            if (!isAvailable) {
                DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.network_not_available, R.string.ok, null);
            }
        }

        @Override
        public void onSSUDPChanged(boolean isConnect) {
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
//                        if (isWifiAvailable) {
                        DialogUtils.showConfirmDialog(LoginActivity.this, R.string.tip_title_research, R.string.tip_search_again,
                                R.string.research_now, R.string.remote_login, new DialogUtils.OnDialogClickListener() {
                                    @Override
                                    public void onClick(boolean isPositiveBtn) {
                                        if (isPositiveBtn) {
                                            mScanManager.start();
                                        } else {
                                            showDeviceSpinnerView(mIPLayout);
                                        }
                                    }
                                });
//                        } else {
//                            DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.wifi_not_available, R.string.ok, null);
//                        }
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

        Intent intent = getIntent();
        if (null != intent) {
            uploadIntent = intent.getParcelableExtra(MainActivity.EXTRA_UPLOAD_INTENT);
        }

        initView();
        initLoginHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
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
        if (loginManager.isLogin()) {
            OneSpaceService service = MyApplication.getService();
            service.notifyUserLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
        mScanManager.stop();
    }

    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setBackVisible(false);
        mTitleLayout.setTitle(R.string.title_login);
        mRootView = mTitleLayout;

        mUserLayout = (RelativeLayout) findViewById(R.id.layout_user);
        mUserTxt = (EditText) findViewById(R.id.editext_user);
        mMoreUserBtn = (ImageButton) findViewById(R.id.btn_more_user);
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
        mPortLayout = (RelativeLayout) findViewById(R.id.layout_port);
        mIPTxt = (EditText) findViewById(R.id.editext_ip);
        mMoreIpBtn = (ImageButton) findViewById(R.id.btn_more_ip);
        mMoreIpBtn.setOnClickListener(onMoreClickListener);
        mMoreIpBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mScanManager.start();
                return true;
            }
        });
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(onLoginClickListener);
    }

    private void showUserSpinnerView(View view) {
        if (mUserSpinnerView != null && mUserSpinnerView.isShown()) {
            mUserSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mHistoryUserList)) {
                TextView mPreTxt = (TextView) findViewById(R.id.txt_name);
                mUserSpinnerView = new SpinnerView(this, view.getWidth(), mPreTxt.getWidth());

                ArrayList<SpinnerView.SpinnerItem<UserInfo>> spinnerItems = new ArrayList<>();
                for (int i = 0; i < mHistoryUserList.size(); i++) {
                    UserInfo info = mHistoryUserList.get(i);
                    SpinnerView.SpinnerItem<UserInfo> item = new SpinnerView.SpinnerItem<>(i, 0, R.drawable.btn_clear, info.getName(), true, info);
                    spinnerItems.add(item);
                }
                mUserSpinnerView.addSpinnerItems(spinnerItems);
                mUserSpinnerView.setOnSpinnerClickListener(new SpinnerView.OnSpinnerClickListener<UserInfo>() {
                    @Override
                    public void onButtonClick(View view, SpinnerView.SpinnerItem<UserInfo> item) {
                        mHistoryUserList.remove(item.obj);
                        UserInfoKeeper.unActive(item.obj);
                        mUserSpinnerView.dismiss();
                    }

                    @Override
                    public void onItemClick(View view, SpinnerView.SpinnerItem<UserInfo> item) {
                        UserInfo userInfo = item.obj;
                        mUserTxt.setText(userInfo.getName());
                        mPwdTxt.setText(userInfo.getPwd());
                        mUserSpinnerView.dismiss();
                    }
                });
                InputMethodUtils.hideKeyboard(LoginActivity.this);
                mUserSpinnerView.showPopupDown(view);
            }
        }
    }

    private void showDeviceSpinnerView(View view) {
        if (mDeviceSpinnerView != null && mDeviceSpinnerView.isShown()) {
            mDeviceSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mLANDeviceList) || !EmptyUtils.isEmpty(mHistoryDeviceList)) {
                mDeviceSpinnerView = new SpinnerView(this, view.getWidth(), mUserTxt.getLeft());

                int id = 0;
                ArrayList<SpinnerView.SpinnerItem<DeviceInfo>> spinnerItems = new ArrayList<>();
                for (DeviceInfo info : mLANDeviceList) {
                    SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_LAN, 0, info.getLanIp(), false, info);
                    spinnerItems.add(spinnerItem);
                    id++;
                }

                for (DeviceInfo info : mHistoryDeviceList) {
                    if (!EmptyUtils.isEmpty(info.getWanIp())) {
                        SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_WAN, R.drawable.btn_clear, info.getWanIp(), true, info);
                        spinnerItems.add(spinnerItem);
                        id++;
                    }
                }

                for (DeviceInfo info : mHistoryDeviceList) {
                    if (!EmptyUtils.isEmpty(info.getSsudpCid())) {
                        String name = info.getName();
                        if (EmptyUtils.isEmpty(name)) {
                            name = info.getSsudpCid();
                        }
                        SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_SSUDP, R.drawable.btn_clear, name, true, info);
                        spinnerItems.add(spinnerItem);
                        id++;
                    }
                }

                mDeviceSpinnerView.addSpinnerItems(spinnerItems);
                mDeviceSpinnerView.setOnSpinnerClickListener(new SpinnerView.OnSpinnerClickListener<DeviceInfo>() {
                    @Override
                    public void onButtonClick(View view, SpinnerView.SpinnerItem<DeviceInfo> item) {
                        DeviceInfo info = item.obj;
                        if (item.group == Constants.DOMAIN_DEVICE_WAN) {
                            info.setWanIp(null);
                            info.setWanPort(null);
                        } else if (item.group == Constants.DOMAIN_DEVICE_SSUDP) {
                            info.setSsudpCid(null);
                            info.setSsudpPwd(null);
                        }
                        DeviceInfoKeeper.update(info);
                        mDeviceSpinnerView.dismiss();
                    }

                    @Override
                    public void onItemClick(View view, SpinnerView.SpinnerItem<DeviceInfo> item) {
                        Log.e(TAG, ">>> DeviceSpinnerView Item Click: " + item.id);
                        DeviceInfo deviceInfo = item.obj;
                        if (item.group == Constants.DOMAIN_DEVICE_LAN) {
                            mIPTxt.setText(deviceInfo.getLanIp());
                            mPortTxt.setText(deviceInfo.getLanPort());
                            mPortLayout.setVisibility(View.VISIBLE);
                        } else if (item.group == Constants.DOMAIN_DEVICE_WAN) {
                            mIPTxt.setText(deviceInfo.getWanIp());
                            mPortTxt.setText(deviceInfo.getWanPort());
                            mPortLayout.setVisibility(View.VISIBLE);
                        } else {
                            mIPTxt.setText(deviceInfo.getSsudpCid());
                            mPortTxt.setText(deviceInfo.getSsudpPwd());
                            mPortLayout.setVisibility(View.GONE);
                        }

                        mDeviceSpinnerView.dismiss();
                    }
                });

                InputMethodUtils.hideKeyboard(LoginActivity.this);
                mDeviceSpinnerView.showPopupDown(view);
            }
        }
    }

    private void initLoginHistory() {
        List<UserInfo> userList = UserInfoKeeper.activeUsers();
        if (!EmptyUtils.isEmpty(userList)) {
            mMoreUserBtn.setVisibility(View.VISIBLE);
            mHistoryUserList.addAll(userList);
            mLastLoginUser = userList.get(0);
            mUserTxt.setText(mLastLoginUser.getName());
            mPwdTxt.setText(mLastLoginUser.getPwd());
        } else {
            mMoreUserBtn.setVisibility(View.GONE);
        }

        List<DeviceInfo> deviceList = DeviceInfoKeeper.all();
        if (null != deviceList) {
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
        } else if (mPortLayout.isShown() && !Utils.checkPort(port)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPortTxt);
            AnimUtils.focusToEnd(mPortTxt);
            ToastHelper.showToast(R.string.tip_invalid_port);
            return false;
        }

        String mac = getLANDeviceMacByIP(ip);
        int domain;
        if (mac != null) {
            domain = Constants.DOMAIN_DEVICE_LAN;
        } else {
            if (mPortLayout.isShown()) {
                domain = Constants.DOMAIN_DEVICE_WAN;
            } else {
                domain = Constants.DOMAIN_DEVICE_SSUDP;
            }
        }

        if (domain == Constants.DOMAIN_DEVICE_SSUDP) {
            connectSSUPDClient(ip, port, user, pwd);
        } else {
            doLogin(user, pwd, ip, port, mac, domain);
        }

        return true;
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac, int domain) {
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

                // TODO... do not open ssudp
//                if (mLoginSession.isNew()) {
//                    showSSUDPTipsDialog();
//                } else {
                gotoMainActivity();
//                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, getString(R.string.tips_title_version_mismatch), errorMsg, getString(R.string.ok), null);
                } else if (errorNo == HttpErrorNo.ERR_CONNECT_REFUSED) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.connection_refused, R.string.ok, null);
                } else {
                    showTipView(errorMsg, false);
                }
            }
        });
        loginAPI.login(domain);
    }

    private void showSSUDPTipsDialog() {
        MagicDialog dialog = new MagicDialog(this);
        dialog.confirm().title(R.string.tip_title_bond_ssudp).content(R.string.tip_bond_ssudp)
                .positive(R.string.bind_now).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            getSSUDPClientID();
                        } else {
                            gotoMainActivity();
                        }
                    }
                }).show();
    }

    private void setSSUDPName(final String cid) {
        MagicDialog.creator(LoginActivity.this).edit().title(R.string.title_ssudp_name).hint(R.string.hint_enter_ssupd_name)
                .positive(R.string.confirm).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public boolean onClick(View view, MagicDialog.MagicDialogButton button, EditText editText, boolean checked) {
                        String name = editText.getText().toString();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            if (EmptyUtils.isEmpty(name)) {
                                AnimUtils.sharkEditText(LoginActivity.this, editText);
                                return false;
                            } else {
                                mLoginSession.getDeviceInfo().setSsudpCid(cid);
                                mLoginSession.getDeviceInfo().setSsudpPwd("12345678");
                                mLoginSession.getDeviceInfo().setName(name);
                                DeviceInfoKeeper.update(mLoginSession.getDeviceInfo());

                                showTipView(R.string.bind_succeed, true, new PopupWindow.OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        gotoMainActivity();
                                    }
                                });
                                return true;
                            }
                        } else {
                            gotoMainActivity();
                        }

                        return true;
                    }
                }).show();
    }

    private void getSSUDPClientID() {
        OneOSSSUDPClientIDAPI ssudpClientIDAPI = new OneOSSSUDPClientIDAPI(mLoginSession.getIp(), mLoginSession.getPort());
        ssudpClientIDAPI.setOnClientIDListener(new OneOSSSUDPClientIDAPI.OnClientIDListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.binding);
            }

            @Override
            public void onSuccess(String url, String cid) {
                setSSUDPName(cid);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                MagicDialog dialog = new MagicDialog(LoginActivity.this);
                dialog.notice().title(R.string.tip_title_bond_ssudp_failed).content(R.string.tip_bond_ssudp_failed)
                        .positive(R.string.ok).bold(MagicDialog.MagicDialogButton.POSITIVE)
                        .listener(new OnMagicDialogClickCallback() {
                            @Override
                            public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
                                gotoMainActivity();
                            }
                        }).show();
            }
        });
        ssudpClientIDAPI.query();
    }

    private void connectSSUPDClient(String strcid, String strpwd, final String user, final String pwd) {
        final SSUDPManager ssudpManager = SSUDPManager.getInstance();
        ssudpManager.initSSUDPClient(LoginActivity.this, strcid, strpwd);
        ssudpManager.connectSSUPDClient(new SSUDPManager.OnSSUDPConnectListener() {
            @Override
            public void onResult(final int progress, final boolean connected) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                            ssudpManager.startSSUDPClient();
                            doLogin(user, pwd, null, null, null, Constants.DOMAIN_DEVICE_SSUDP);
                        } else {
                            dismissLoading();
                            MagicDialog dialog = new MagicDialog(LoginActivity.this);
                            dialog.notice().title(R.string.tips).content(R.string.tip_title_connect_ssudp_failed).positive(R.string.ok)
                                    .bold(MagicDialog.MagicDialogButton.POSITIVE).show();
                        }
                    }
                });
            }
        });
        showLoading(R.string.ssudp_connecting);
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        if (null != uploadIntent) {
            intent.putExtra(MainActivity.EXTRA_UPLOAD_INTENT, uploadIntent);
        }
        startActivity(intent);
        finish();
    }

    private String getLANDeviceMacByIP(String ip) {
        for (DeviceInfo info : mLANDeviceList) {
            if (info.getLanIp().equals(ip)) {
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
        String lastMac = mLastLoginUser.getMac();
        if (!EmptyUtils.isEmpty(lastMac)) {
            if (lastMac.equalsIgnoreCase(mac)) {
                mIPTxt.setText(ip);
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
                isLast = true;
            }
        }

        return isLast;
    }
}
