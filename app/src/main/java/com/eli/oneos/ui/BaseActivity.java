package com.eli.oneos.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eli.oneos.R;
import com.eli.oneos.utils.SystemBarTintManager;

/**
 * Base Activity for OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
