package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.api.OneOSFileType;
import com.eli.oneos.ui.nav.BaseNavFragment;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudDirFragment extends BaseFileListFragment {
    private static final String TAG = CloudDirFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_directory, container, false);

        mParentFragment = (BaseNavFragment) getParentFragment();
        curPath = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
        mFileType = OneOSFileType.PUBLIC;
        curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        mFileType = OneOSFileType.PRIVATE;

        initBaseParams(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        autoPullToRefresh();
    }
}
