package com.eli.oneos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.scan.OnScanDeviceListener;
import com.eli.oneos.model.oneos.scan.ScanDeviceManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.utils.AppVersionUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.Map;

public class LauncherActivity extends BaseActivity {

    private CircleProgressBar mProgressBar;
    private UserInfo lastUserHistory;
    private Intent mSendIntent;
    private ScanDeviceManager mScanManager;
    private boolean isLastDeviceExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            mSendIntent = intent;
        }
        if (mSendIntent != null) {
            LoginManage loginManager = LoginManage.getInstance();
            if (loginManager.isLogin()) {
                gotoMainActivity();
                return;
            }
        }

        initLastLoginInfo();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mScanManager) {
            mScanManager.stop();
        }
    }

    private void initView() {
        mProgressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        showAlphaAnim();
    }

    private void initLastLoginInfo() {
        lastUserHistory = UserInfoKeeper.top();
//        lastUserHistory = null; // TODO.. test code
    }

    private void showAlphaAnim() {
        ImageView mLogoView = (ImageView) findViewById(R.id.iv_welcome_logo);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(800);
        mLogoView.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            /**
             * <p>Notifies the start of the animation.</p>
             *
             * @param animation The started animation.
             */
            @Override
            public void onAnimationStart(Animation animation) {
            }

            /**
             * <p>Notifies the end of the animation. This callback is not invoked
             * for animations with repeat count set to INFINITE.</p>
             *
             * @param animation The animation which reached its end.
             */
            @Override
            public void onAnimationEnd(Animation animation) {
                showAppVersion();
                if (lastUserHistory == null) {
                    gotoLoginActivity();
                } else {
                    scanningLANDevice();
                }
            }

            /**
             * <p>Notifies the repetition of the animation.</p>
             *
             * @param animation The animation which was repeated.
             */
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void showAppVersion() {
        String appVersion = AppVersionUtils.getAppVersion();
        if (!EmptyUtils.isEmpty(appVersion)) {
            TextView mVersionTxt = (TextView) findViewById(R.id.txt_version);
            mVersionTxt.setText(AppVersionUtils.formatAppVersion(appVersion));
        }
    }

    private void gotoMainActivity() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                OneSpaceService service = MyApplication.getTransferService();
                service.startBackupFile();
                Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                if (null != mSendIntent) {
                    intent.putExtra("IntentFilter", mSendIntent);
                }
                startActivity(intent);
                LauncherActivity.this.finish();
            }
        }, 500);
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        if (null != mSendIntent) {
            intent.putExtra("IntentFilter", mSendIntent);
        }
        startActivity(intent);
        finish();
    }

    private void scanningLANDevice() {
        mScanManager = new ScanDeviceManager(this, new OnScanDeviceListener() {

            @Override
            public void onScanStart() {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScanning(String mac, String ip) {
                if (checkIfLastLoginDevice(mac)) {
                    isLastDeviceExist = true;
                    doLogin(lastUserHistory.getName(), lastUserHistory.getPwd(), ip, OneOSAPIs.ONE_API_DEFAULT_PORT, mac);
                    mScanManager.stop();
                    mScanManager = null;
                }
            }

            @Override
            public void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp) {
                if (!isLastDeviceExist) {
                    gotoLoginActivity();
                }
            }
        });
        mScanManager.start();
    }

    private boolean checkIfLastLoginDevice(String mac) {
        if (lastUserHistory == null) {
            return false;
        }

        boolean isLast = false;
        String perferMac = lastUserHistory.getMac();
        if (!EmptyUtils.isEmpty(perferMac)) {
            if (perferMac.equalsIgnoreCase(mac)) {
                isLast = true;
            }
        }

        return isLast;
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac) {
        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                LoginManage.getInstance().setLoginSession(loginSession);
                gotoMainActivity();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                gotoLoginActivity();
            }
        });
        loginAPI.login();
    }

}
