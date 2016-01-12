package com.eli.oneos.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eli.oneos.R;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.SystemBarTintManager;
import com.eli.oneos.widget.LoadingView;

/**
 * Base Activity for OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BaseActivity extends FragmentActivity {

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadingView = LoadingView.getInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogUtils.dismiss();
        dismissLoading();
    }

    /**
     * Modify System Status Bar Style
     */
    protected void initStatusBarStyle() {
        initStatusBarStyle(R.color.status_bar);
    }

    /**
     * Modify System Status Bar Style
     *
     * @param colorId Status Bar background color resource id
     */
    protected void initStatusBarStyle(int colorId) {
        SystemBarTintManager mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setNavigationBarTintEnabled(true);
        mTintManager.setStatusBarTintResource(colorId);
    }

    protected void showLoading() {
        mLoadingView.show(this);
    }

    protected void showLoading(int msgId) {
        mLoadingView.show(this, msgId);
    }

    protected void showLoading(int msgId, boolean isCancellable) {
        mLoadingView.show(this, msgId, isCancellable);
    }

    protected void showLoading(int msgId, boolean isCancellable, DialogInterface.OnDismissListener listener) {
        mLoadingView.show(this, msgId, isCancellable, listener);
    }

    protected void dismissLoading() {
        mLoadingView.dismiss();
    }
}
