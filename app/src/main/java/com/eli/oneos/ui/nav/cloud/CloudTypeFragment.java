package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/25.
 */
public class CloudTypeFragment extends BaseFileListFragment {
    private static final String TAG = CloudTypeFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_directory, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (BaseNavFragment) getParentFragment();
        mFileType = OneOSFileType.PICTURE;

        initBaseParams(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        autoPullToRefresh();
    }
}
