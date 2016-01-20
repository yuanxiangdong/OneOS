package com.eli.oneos.ui.nav;


import android.support.v4.app.Fragment;

import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.api.OneOSFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.widget.FileOperatePanel;
import com.eli.oneos.widget.FileSelectPanel;

import java.util.ArrayList;

/**
 * Navigation Base Abstract Class
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseNavFragment extends Fragment {

    protected MainActivity mMainActivity;

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    public abstract boolean onBackPressed();

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown       Whether show
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    public abstract void showSelectBar(boolean isShown, int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener);

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown      Whether show
     * @param fileType     OneOS file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    public abstract void showOperateBar(boolean isShown, OneOSFileType fileType, ArrayList<OneOSFile> selectedList, FileOperatePanel.OnFileOperateListener mListener);

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    public abstract void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable);
}
