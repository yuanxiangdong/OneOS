package com.eli.oneos.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.utils.ActivityCollector;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.SystemBarManager;
import com.eli.oneos.widget.LoadingView;
import com.eli.oneos.widget.TipView;

import net.cifernet.mobile.cmapi.CMInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Base Activity for OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BaseActivity extends FragmentActivity {

    protected View mRootView;
    private SystemBarManager mTintManager;
    private LoadingView mLoadingView;
    private TipView mTipView;
    private final static String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
        mLoadingView = LoadingView.getInstance();
        mTipView = TipView.getInstance();

        Log.d(TAG, getRunningActivityName() + "==============baseActivity onCreate");
        mHandler.removeCallbacks(mTimerTask);
        mHandler.post(mTimerTask);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogUtils.dismiss();
        dismissLoading();
        Log.d(TAG, getRunningActivityName() + "==============baseActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        Log.d(TAG, getRunningActivityName() + "==============baseActivity onDestroy");
        if (getRunningActivityName().indexOf("com.eli.oneos.ui") != -1) {
            mHandler.removeCallbacks(mTimerTask);
        }
    }

    /**
     * Modify System Status Bar Style
     */
    protected void initSystemBarStyle() {
        initSystemBarStyle(R.color.status_bar);
    }

    /**
     * Modify System Status Bar Style
     *
     * @param colorId Status Bar background color resource id
     */
    protected void initSystemBarStyle(int colorId) {
        if (null == mTintManager) {
            mTintManager = new SystemBarManager(this);
        }
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setStatusBarTintResource(colorId);
//        mTintManager.setNavigationBarTintEnabled(true);
//        mTintManager.setNavigationBarTintResource(colorId);
    }

    public void showLoading() {
        mLoadingView.show(this);
    }

    public void showLoading(int msgId) {
        mLoadingView.show(this, msgId);
    }

    public void showLoading(int msgId, boolean isCancellable) {
        mLoadingView.show(this, msgId, isCancellable);
    }

    public void showLoading(int msgId, int timeout, DialogInterface.OnDismissListener listener) {
        mLoadingView.show(this, msgId, timeout, listener);
    }

    public void showLoading(int msgId, boolean isCancellable, DialogInterface.OnDismissListener listener) {
        mLoadingView.show(this, msgId, isCancellable, -1, listener);
    }

    public void dismissLoading() {
        mLoadingView.dismiss();
    }

    public void showTipView(int msgId, boolean isPositive) {
        dismissLoading();
        mTipView.show(this, mRootView, msgId, isPositive);
    }

    public void showTipView(int msgId, boolean isPositive, PopupWindow.OnDismissListener listener) {
        dismissLoading();
        mTipView.show(this, mRootView, msgId, isPositive, listener);
    }

    public void showTipView(String msg, boolean isPositive) {
        dismissLoading();
        mTipView.show(this, mRootView, msg, isPositive);
    }

    public boolean controlActivity(String action) {
        return false;
    }


    Handler mHandler = new Handler();
    private Runnable mTimerTask = new Runnable() {
        @Override
        public void run() {
            int statusCode = 10000;

            try {
                String status = CMInterface.getInstance().get_status();
                Log.d(TAG, "status ======" + status);
                JSONTokener parser;
                JSONObject root;
                parser = new JSONTokener(status);
                root = (JSONObject) parser.nextValue();
                statusCode = root.getInt("status");
                if (statusCode != net.cifernet.mobile.cmapi.Constants.CS_CONNECTED) {
                    backToLogin();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (statusCode == net.cifernet.mobile.cmapi.Constants.CS_CONNECTED) {
                mHandler.postDelayed(this, 3000);

            }
        }

    };

    private void backToLogin() {
        Log.d(TAG, "123456=======" + getRunningActivityName());

        if (!getRunningActivityName().endsWith("LoginActivity") && !getRunningActivityName().endsWith("LauncherActivity") ) {
            String ip = null ;
            if (LoginManage.getInstance().isLogin()) {
                ip = LoginManage.getInstance().getLoginSession().getIp();
            }

            if ( ip.endsWith("cifernet.net") || ip.endsWith("memenet.net")) {
                DialogUtils.dismiss();
                dismissLoading();

                OneSpaceService mTransferService = MyApplication.getService();
                mTransferService.notifyUserLogout();
                LoginManage.getInstance().logout();
                CMInterface.getInstance().disconnect();

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                ActivityCollector.finishAll();
            }
        }
    }

    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }
}
