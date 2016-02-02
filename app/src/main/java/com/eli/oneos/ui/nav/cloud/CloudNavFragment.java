package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileTypeItem;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.SearchPanel;
import com.eli.oneos.widget.TypePopupView;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudNavFragment extends BaseNavFragment {
    private static final String TAG = CloudNavFragment.class.getSimpleName();

    private CloudFileFragment mCurFragment;
    private FileSelectPanel mSelectPanel;
    private SearchPanel mSearchPanel;
    private FileManagePanel mManagePanel;
    private ImageButton mSearchBtn;

    private RelativeLayout mTitleLayout;
    private Button mTypeBtn;
    private TypePopupView mTypePopView;

    private ArrayList<FileTypeItem> mFileTypeList = new ArrayList<>();

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
        initTypeView();
        initFragment();

        return view;
    }

    private void initView(View view) {
        mTitleLayout = (RelativeLayout) view.findViewById(R.id.include_title);
        mSelectPanel = (FileSelectPanel) view.findViewById(R.id.layout_select_top_panel);
        mSearchPanel = (SearchPanel) view.findViewById(R.id.layout_search_panel);
        mManagePanel = (FileManagePanel) view.findViewById(R.id.layout_operate_bottom_panel);
        mTypeBtn = (Button) view.findViewById(R.id.btn_sort);
        mTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTypePopView.showPopupTop(mTitleLayout);
            }
        });
        mSearchBtn = (ImageButton) view.findViewById(R.id.ibtn_nav_title_right);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchPanel.showPanel(true);
            }
        });
    }

    private void initTypeView() {
        FileTypeItem privateItem = new FileTypeItem(R.string.file_type_private, R.drawable.btn_file_type_private, R.drawable.btn_file_type_private_pressed, OneOSFileType.PRIVATE);
        mFileTypeList.add(privateItem);
        FileTypeItem publicItem = new FileTypeItem(R.string.file_type_public, R.drawable.btn_file_type_public, R.drawable.btn_file_type_public_pressed, OneOSFileType.PUBLIC);
        mFileTypeList.add(publicItem);
        FileTypeItem docItem = new FileTypeItem(R.string.file_type_doc, R.drawable.btn_file_type_doc, R.drawable.btn_file_type_doc_pressed, OneOSFileType.DOC);
        mFileTypeList.add(docItem);
        FileTypeItem picItem = new FileTypeItem(R.string.file_type_pic, R.drawable.btn_file_type_pic, R.drawable.btn_file_type_pic_pressed, OneOSFileType.PICTURE);
        mFileTypeList.add(picItem);
        FileTypeItem audioItem = new FileTypeItem(R.string.file_type_audio, R.drawable.btn_file_type_audio, R.drawable.btn_file_type_audio_pressed, OneOSFileType.AUDIO);
        mFileTypeList.add(audioItem);
        FileTypeItem videoItem = new FileTypeItem(R.string.file_type_video, R.drawable.btn_file_type_video, R.drawable.btn_file_type_video_pressed, OneOSFileType.VIDEO);
        mFileTypeList.add(videoItem);
        FileTypeItem recycleItem = new FileTypeItem(R.string.file_type_cycle, R.drawable.btn_file_type_recycle, R.drawable.btn_file_type_recycle_pressed, OneOSFileType.RECYCLE);
        mFileTypeList.add(recycleItem);
        mTypePopView = new TypePopupView(mMainActivity, mFileTypeList);
        mTypePopView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileTypeItem item = mFileTypeList.get(position);
                OneOSFileType type = (OneOSFileType) item.getFlag();
                mTypeBtn.setText(OneOSFileType.getTypeName(type));
                changeFragmentByType(type);
                mTypePopView.dismiss();
            }
        });
    }

    private void initFragment() {
        mCurFragment = new CloudFileFragment();
        changeFragmentByType(OneOSFileType.PRIVATE);
    }

    private void changeFragmentByType(OneOSFileType type) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        String path = null;
        if (type == OneOSFileType.PRIVATE) {
            path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        } else if (type == OneOSFileType.PUBLIC) {
            path = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
        } else if (type == OneOSFileType.RECYCLE) {
            path = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
        }
        if (!mCurFragment.isAdded()) {
            transaction.add(R.id.fragment_content, mCurFragment);
        }
        transaction.show(mCurFragment);
        mCurFragment.setFileType(type, path);
        mCurFragment.onResume();
        transaction.commitAllowingStateLoss();
    }

    public void addSearchListener(SearchPanel.OnSearchActionListener listener) {
        mSearchPanel.setOnSearchListener(listener);
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
     * @param isShown Whether show
     */
    @Override
    public void showSelectBar(boolean isShown) {
        if (isShown) {
            mSelectPanel.showPanel(true);
        } else {
            mSelectPanel.hidePanel(true);
        }
    }

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    @Override
    public void updateSelectBar(int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener) {
        mSelectPanel.setOnSelectListener(mListener);
        mSelectPanel.updateCount(totalCount, selectedCount);
    }

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    @Override
    public void showManageBar(boolean isShown) {
        if (isShown) {
            mManagePanel.showPanel(true);
        } else {
            mManagePanel.hidePanel(false, true);
        }
    }

    /**
     * Update Bottom Operate Bar
     *
     * @param fileType     OneOS file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    @Override
    public void updateManageBar(OneOSFileType fileType, ArrayList<OneOSFile> selectedList, FileManagePanel.OnFileManageListener mListener) {
        mManagePanel.setOnOperateListener(mListener);
        mManagePanel.updatePanelItems(fileType, selectedList);
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
