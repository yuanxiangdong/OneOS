package com.eli.oneos.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.eli.oneos.model.oneos.api.OneOSFormatAPI;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.api.OneOSMemenetUserInfoAPI;
import com.eli.oneos.model.oneos.api.OneOSSsudpClientIDAPI;
import com.eli.oneos.model.oneos.scan.OnScanDeviceListener;
import com.eli.oneos.model.oneos.scan.ScanDeviceManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.nav.HdManageActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.SpinnerView;
import com.eli.oneos.widget.TitleBackLayout;

import net.cifernet.mobile.cmapi.CMInterface;
import net.cifernet.mobile.cmapi.listener.CSListener;
import net.cifernet.mobile.cmapi.listener.RTListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private List<DeviceInfo> mMementDeviceList = new ArrayList<DeviceInfo>();
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


            Intent intent = getIntent();
            if (null != intent) {
                String value = intent.getStringExtra("domain");
                Log.d(TAG, "get domain: " + value);
                if (!EmptyUtils.isEmpty(value)) {
                    mIPTxt.setText(value);
                }
            }

            if (EmptyUtils.isEmpty(mIPTxt.getText().toString()) && !EmptyUtils.isEmpty(mLANDeviceList)) {
                mIPTxt.setText(mLANDeviceList.get(0).getLanIp());
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
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
            attemptLogin(false);
        }
    };
    private View.OnClickListener onMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.btn_more_user:
//                    showUserSpinnerView(mUserLayout);
//                    break;
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_new);
        //initSystemBarStyle();

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

        memenetList();
        CMInterface.getInstance().start_service(LoginActivity.this);
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
//        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
//        mTitleLayout.setBackVisible(false);
//        mTitleLayout.setTitle(R.string.title_login);
//        mRootView = mTitleLayout;


        mUserLayout = (RelativeLayout) findViewById(R.id.layout_user);
        mUserTxt = (EditText) findViewById(R.id.editext_user);
//        mMoreUserBtn = (ImageButton) findViewById(R.id.btn_more_user);
//        mMoreUserBtn.setOnClickListener(onMoreClickListener);
        mPwdTxt = (EditText) findViewById(R.id.editext_pwd);
        mPortTxt = (EditText) findViewById(R.id.editext_port);
        mPortTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (attemptLogin(false)) {
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
            if (!EmptyUtils.isEmpty(mLANDeviceList) || !EmptyUtils.isEmpty(mHistoryDeviceList) || !EmptyUtils.isEmpty(mMementDeviceList)) {
                TextView mPreTxt = (TextView) findViewById(R.id.txt_name);
                mDeviceSpinnerView = new SpinnerView(this, view.getWidth(), mPreTxt.getWidth());

                int id = 0;
                ArrayList<SpinnerView.SpinnerItem<DeviceInfo>> spinnerItems = new ArrayList<>();
                for (DeviceInfo info : mLANDeviceList) {
                    SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_LAN, 0, info.getLanIp(), false, info);
                    spinnerItems.add(spinnerItem);
                    id++;
                }

                for (DeviceInfo memenet : mMementDeviceList) {
                    SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_LAN, 0, memenet.getLanIp(), false, memenet);
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
                        } else  {
                            mIPTxt.setText(deviceInfo.getWanIp());
                            mPortTxt.setText(deviceInfo.getWanPort());
                            mPortLayout.setVisibility(View.VISIBLE);
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
//            mMoreUserBtn.setVisibility(View.VISIBLE);
            mHistoryUserList.addAll(userList);
            mLastLoginUser = userList.get(0);
            mUserTxt.setText(mLastLoginUser.getName());
            mPwdTxt.setText(mLastLoginUser.getPwd());
        } else {
//            mMoreUserBtn.setVisibility(View.GONE);
        }

        List<DeviceInfo> deviceList = DeviceInfoKeeper.all();
        if (null != deviceList) {
            mHistoryDeviceList.addAll(deviceList);
        }
    }

    private boolean attemptLogin( boolean isMemenet) {
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
            domain = Constants.DOMAIN_DEVICE_WAN;
        }


        if (ip.endsWith("cifernet.net") || ip.endsWith("memenet.net")) {
            if (isMemenet) {
                doLogin(user, pwd, ip, port, mac, domain);
            } else {
                memenetInit(ip);
                showLoading(R.string.memeneting);
            }
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
                gotoFormat();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, getString(R.string.tips_title_version_mismatch), errorMsg, getString(R.string.ok), null);
                } else if (errorNo == HttpErrorNo.ERR_CONNECT_REFUSED) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.connection_refused, R.string.ok, null);
                } else {
                    ToastHelper.showToast(errorMsg);
                   // showTipView(errorMsg, false);
                }
            }
        });
        loginAPI.login(domain);
    }




    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        if (null != uploadIntent) {
            intent.putExtra(MainActivity.EXTRA_UPLOAD_INTENT, uploadIntent);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void gotoFormat() {
        OneOSFormatAPI formatAPI = new OneOSFormatAPI(mLoginSession);
        formatAPI.setListener(new OneOSFormatAPI.OnHDInfoListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.logining, false);
            }

            @Override
            public void onSuccess(String url, String errHdNum, String count) {
                if (errHdNum.equals("0")) {
                    getMemenetUserInfo();
                    gotoMainActivity();
                } else {
                    Intent intent = new Intent(LoginActivity.this, HdManageActivity.class);
                    intent.putExtra("count", count);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
            }
        });
        mLoginSession.getOneOSInfo().getVersion();
        formatAPI.getHdInfo(mLoginSession.getOneOSInfo().getVersion());
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

    // ---------------------------- Memenet --------------------------------
/* getMemenetUserInfo 首次局域网登录成功保存米米网账号密码和nas设备域名 */

    private void getMemenetUserInfo() {
        OneOSMemenetUserInfoAPI MemenetUserInfoAPI = new OneOSMemenetUserInfoAPI(mLoginSession);
        MemenetUserInfoAPI.setListener(new OneOSMemenetUserInfoAPI.MemenetUserInfoListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String memenetAccount, String memenetPassWord, String domain) {
                Context memenetInfo = LoginActivity.this;
                SharedPreferences memenetSP = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
                String memenet = memenetSP.getString(memenetAccount, "none");
                Log.d(TAG, "memenet ===== " + memenet);
                if (memenet.equals("none")) {
                    Log.d(TAG, "---------------存入memenet数据----------------");
                    SharedPreferences.Editor editor1 = memenetSP.edit();
                    editor1.putString(memenetAccount, memenetPassWord + "======" + domain);
                    editor1.commit();

                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                Log.d(TAG, "errorMsg=" + errorMsg);
            }
        });
        MemenetUserInfoAPI.getMemenetUserInfo();
    }


    /* memenetList 获取 Memenet 域名列表  */

    private void memenetList() {
        mMementDeviceList.clear();
        //CMInterface.getInstance().restart_service();
        Context memenetInfo = LoginActivity.this;
        SharedPreferences sp = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        Map<String, ?> allContent = sp.getAll();

        for (Map.Entry<String, ?> entry : allContent.entrySet()) {
            String ip = entry.getValue().toString().split("======")[1];
            DeviceInfo DeviceInfo = new DeviceInfo(null, null, ip, OneOSAPIs.ONE_API_DEFAULT_PORT, null, null, null, null,
                    Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis());
            mMementDeviceList.add(DeviceInfo);
        }
    }


    /*  memenetInit  登陆米米网账号密码 */

    private void memenetInit(final String ip) {

        Log.d(TAG, "-----------memementInt Start-------------");
        Context memenetInfo = LoginActivity.this;
        SharedPreferences sp = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        Map<String, ?> allContent = sp.getAll();
        String domain, password;
        boolean isLogin = false;

        for (final Map.Entry<String, ?> entry : allContent.entrySet()) {
            domain = entry.getValue().toString().split("======")[1];
            password = entry.getValue().toString().split("======")[0];

            if (domain.equals(ip)) {
                final String finalPassword = password;
                isLogin = true;
                new Thread() {
                    @Override
                    public void run() {
                        final int i = CMInterface.getInstance().connect(entry.getKey(), finalPassword);
                        if (i == net.cifernet.mobile.cmapi.Constants.CE_SUCC) {
                            Log.d(TAG, "-------------- memenet login success -------------");
                            checkAuthority();

                        }
                    }

                }.start();
            }
        }
        Log.d(TAG, "isLogin=" + isLogin);
        if (!isLogin) {
            new Thread() {
                @Override
                public void run() {
                    final int i = CMInterface.getInstance().connect("2374494259@qq.com", "xDhp3NY5ktW");
                    if (i == net.cifernet.mobile.cmapi.Constants.CE_SUCC) {
                        Log.d(TAG, "-------------- memenet built-in account login success -------------");
                        checkAuthority();
                    }
                }

            }.start();
        }
    }


    /*   checkAythority 开启米米网服务  */
    private void checkAuthority() {
        boolean auth = false;
        while (!auth) {
            auth = CMInterface.getInstance().service_is_prepared(LoginActivity.this);
            if (!auth) {
                CMInterface.getInstance().service_to_prepare(LoginActivity.this, 0);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "Memenet auth: " + auth);
        CMInterface.getInstance().start_service(LoginActivity.this);
        try {
            Thread.sleep(2000);
            mHandler.post(mRunnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    Handler mHandler = new Handler();
    private CSListener mCSListener = new CSListener() {
        @Override
        public void onConnected(String baseinfo) {
            System.out.println("==============1==============");
            attemptLogin(true);
            mHandler.removeCallbacks(mRunnable);
        }

        @Override
        public void onConnecting(boolean isAuth) {
            System.out.println("==============2==============");
            mHandler.postDelayed(mRunnable, 2000);
        }


        @Override
        public void onDisconnected(Integer reason) {
            System.out.println("==============3==============");
            dismissLoading();
            if (reason != net.cifernet.mobile.cmapi.Constants.DR_BY_USER)
                Toast.makeText(LoginActivity.this, "reason:" + reason, Toast.LENGTH_SHORT).show();
        }
    };

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            CMInterface.getInstance().get_status(mCSListener);
        }
    };


}
