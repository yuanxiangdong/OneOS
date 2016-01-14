package com.eli.oneos.ui.nav;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eli.oneos.R;
import com.eli.oneos.model.api.ServerFileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudFragment extends BaseNavFragment {
    private static final String TAG = CloudFragment.class.getSimpleName();

    private CloudDirectoryFragment mDirFragment;
    private Fragment mCurFragment;
    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud, container, false);

//        mHandler = getCurApplication().getHandler();
//
//        getCurApplication().setCloudFileType(ServerFileType.ALL);
//
//        registerBroadcastReceiver();
//
//        initAnimActions();
//
//        initView(view);
//
        initFragment();

        return view;
    }

    private void initFragment() {
        mDirFragment = new CloudDirectoryFragment();

        changeFragmentByType(ServerFileType.ALL);
    }

    private void changeFragmentByType(ServerFileType type) {
        Fragment fragment;

        if (type == ServerFileType.ALL) {
            fragment = mDirFragment;
        } else {
            fragment = mDirFragment;
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mCurFragment != null) {
            mCurFragment.onPause();
            transaction.hide(mCurFragment);
        }

        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_content, fragment);
        } else {
            fragment.onResume();
        }
        mCurFragment = fragment;
        transaction.show(mCurFragment);
        transaction.commitAllowingStateLoss();
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * Show/Hide Top Title Bar
     *
     * @param isShown whether show
     */
    @Override
    public void showTitleBar(boolean isShown) {

    }

    /**
     * Show/Hide Bottom Navigation Bar
     *
     * @param isShown whether show
     */
    @Override
    public void showNavBar(boolean isShown) {

    }

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    @Override
    public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {

    }
}
