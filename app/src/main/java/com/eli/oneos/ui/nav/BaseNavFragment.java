package com.eli.oneos.ui.nav;


import android.support.v4.app.Fragment;

import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.widget.FileManagePanel;
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
     * @param isShown Whether show
     */
    public abstract void showSelectBar(boolean isShown);

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    public abstract void updateSelectBar(int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener);

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    public abstract void showManageBar(boolean isShown);

    /**
     * Update Bottom Operate Bar
     *
     * @param fileType     OneOS file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    public abstract void updateManageBar(OneOSFileType fileType, ArrayList<OneOSFile> selectedList, FileManagePanel.OnFileManageListener mListener);

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    public abstract void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable);
}
