package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.api.OneOSFileOptGenerate;
import com.eli.oneos.model.api.OneOSFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.widget.FileOperatePanel;
import com.eli.oneos.widget.FileSelectPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudNavFragment extends BaseNavFragment {
    private static final String TAG = CloudNavFragment.class.getSimpleName();

    private CloudDirFragment mDirFragment;
    private BaseFileListFragment mCurFragment;
    private FileSelectPanel mSelectPanel;
    private FileOperatePanel mOperatePanel;

    private RelativeLayout mTitleLayout;

    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        mMainActivity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.fragment_nav_cloud, container, false);
//        mHandler = getCurApplication().getHandler();
//
//        getCurApplication().setCloudFileType(OneOSFileType.PRIVATE);
//
//        registerBroadcastReceiver();
//
//        initAnimActions();

        initView(view);

        initFragment();

        return view;
    }

    private void initView(View view) {
        mTitleLayout = (RelativeLayout) view.findViewById(R.id.include_title);
        mSelectPanel = (FileSelectPanel) view.findViewById(R.id.layout_select_top_panel);
        mOperatePanel = (FileOperatePanel) view.findViewById(R.id.layout_operate_bottom_panel);
    }

    private void initFragment() {
        mDirFragment = new CloudDirFragment();

        changeFragmentByType(OneOSFileType.PRIVATE);
    }

    private void changeFragmentByType(OneOSFileType type) {
        BaseFileListFragment fragment;

        if (type == OneOSFileType.PRIVATE) {
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
        if (null != mCurFragment) {
            return mCurFragment.onBackPressed();
        }

        return false;
    }

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown       Whether show
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    @Override
    public void showSelectBar(boolean isShown, int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener) {
        if (isShown) {
            mSelectPanel.setOnSelectListener(mListener);
            mSelectPanel.showPanel(true, totalCount, selectedCount);
        } else {
            mSelectPanel.hidePanel(true);
        }
    }

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown      Whether show
     * @param fileType     OneOS file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    @Override
    public void showOperateBar(boolean isShown, OneOSFileType fileType, ArrayList<OneOSFile> selectedList, FileOperatePanel.OnFileOperateListener mListener) {
        if (isShown) {
            mOperatePanel.setOnOperateListener(mListener);
            mOperatePanel.showPanel(OneOSFileOptGenerate.generate(fileType, selectedList), true);
            mMainActivity.showNavBar(false);
        } else {
            mMainActivity.showNavBar(true);
            mOperatePanel.hidePanel(true);
        }
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
