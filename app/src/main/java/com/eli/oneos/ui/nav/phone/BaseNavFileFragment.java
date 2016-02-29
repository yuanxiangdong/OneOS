package com.eli.oneos.ui.nav.phone;


import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.SearchPanel;

import java.io.File;
import java.util.ArrayList;

/**
 * Navigation Base Abstract Class
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseNavFileFragment extends BaseNavFragment {

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
    public abstract void updateManageBar(LocalFileType fileType, ArrayList<File> selectedList, FileManagePanel.OnFileManageListener mListener);

    /**
     * Add search file listener
     *
     * @param listener
     */
    public abstract void addSearchListener(SearchPanel.OnSearchActionListener listener);
}
