package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.FileViewerType;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSStickyGridAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSStickyListAdapter;
import com.eli.oneos.model.oneos.api.OneOSListDBAPI;
import com.eli.oneos.model.oneos.api.OneOSSearchAPI;
import com.eli.oneos.model.oneos.comp.OneOSFileCTTimeComparator;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFileFragment;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.PullToRefreshView;
import com.eli.oneos.widget.SearchPanel;
import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersView;
import com.eli.oneos.widget.sticky.listview.StickyListHeadersView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/03.
 */
public class CloudDbFragment extends BaseCloudFragment {
    private static final String TAG = CloudDbFragment.class.getSimpleName();

    private RelativeLayout mListLayout, mGridLayout;
    private StickyListHeadersView mListView;
    private StickyGridHeadersView mGridView;
    private PullToRefreshView mListPullToRefreshView;
    private PullToRefreshView mGridPullToRefreshView;
    private boolean isPullDownRefresh = true;
    private OneOSStickyListAdapter mListAdapter;
    private OneOSStickyGridAdapter mGridAdapter;

    private int mPage = 0, mPages = 0;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mLastClickPosition = position;
            mLastClickItem2Top = view.getTop();

            OneOSFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (isMultiMode) {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel(false);
            } else {
                isSelectionLastPosition = true;
                FileUtils.openOneOSFile(mLoginSession, mMainActivity, position, mFileList);
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            OneOSFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (!isMultiMode) {
                setMultiModel(true, position);
                updateSelectAndManagePanel(false);
            } else {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel(false);
            }

            return true;
        }
    };
    private FileSelectPanel.OnFileSelectListener mFileSelectListener = new FileSelectPanel.OnFileSelectListener() {
        @Override
        public void onSelect(boolean isSelectAll) {
            getFileAdapter().selectAllItem(isSelectAll);
            getFileAdapter().notifyDataSetChanged();
            updateSelectAndManagePanel(false);
        }

        @Override
        public void onDismiss() {
            setMultiModel(false, 0);
        }
    };

    private FileManagePanel.OnFileManageListener mFileManageListener = new FileManagePanel.OnFileManageListener() {
        @Override
        public void onClick(View view, ArrayList<?> selectedList, FileManageAction action) {
            if (EmptyUtils.isEmpty(selectedList)) {
                ToastHelper.showToast(R.string.tip_select_file);
            } else {
                isSelectionLastPosition = true;
                OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mOrderLayout, new OneOSFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });
                if (action.equals(FileManageAction.MORE)) {
                    Log.d(TAG,"Manage More======");
                    updateSelectAndManagePanel(true);
                }else if (action.equals(FileManageAction.BACK)){
                    updateSelectAndManagePanel(false);
                } else {
                    fileManage.manage(mFileType, action, (ArrayList<OneOSFile>) selectedList);
                }
            }
        }


        @Override
        public void onDismiss() {
        }
    };
    private PullToRefreshView.OnHeaderRefreshListener mHeaderRefreshListener = new PullToRefreshView.OnHeaderRefreshListener() {
        @Override
        public void onHeaderRefresh(PullToRefreshView view) {
            isPullDownRefresh = true;
            setMultiModel(false, 0);
            getOneOSFileList(0);
        }
    };
    private PullToRefreshView.OnFooterRefreshListener mFooterRefreshListener = new PullToRefreshView.OnFooterRefreshListener() {
        @Override
        public void onFooterRefresh(PullToRefreshView view) {
            isPullDownRefresh = false;
            setMultiModel(false, 0);
            if (mPage < mPages - 1) {
                getOneOSFileList(++mPage);
            } else {
                mMainActivity.showTipView(R.string.all_loaded, true);
                mListPullToRefreshView.onFooterRefreshComplete();
                mGridPullToRefreshView.onFooterRefreshComplete();
            }
        }
    };
    private String mSearchFilter = null;
    private SearchPanel.OnSearchActionListener mSearchListener = new SearchPanel.OnSearchActionListener() {
        @Override
        public void onVisible(boolean visible) {
        }

        @Override
        public void onSearch(String filter) {
            mSearchFilter = filter;
            autoPullToRefresh();
        }

        @Override
        public void onCancel() {
            mSearchFilter = null;
            autoPullToRefresh();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, ">>>>>>>>On Create>>>>>>>");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_db, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (BaseNavFileFragment) getParentFragment();
        isListShown = false;

        initLoginSession();
        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, ">>>>>>>>On Resume>>>>>>>");
        if (null != mParentFragment) {
            mParentFragment.addSearchListener(mSearchListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        setMultiModel(false, 0);
    }

    private void initView(View view) {
        mListLayout = (RelativeLayout) view.findViewById(R.id.include_file_list);
        mGridLayout = (RelativeLayout) view.findViewById(R.id.include_file_grid);

        mSlideInAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_in_from_top);
        mSlideOutAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_out_to_top);
        mOrderLayout = (LinearLayout) view.findViewById(R.id.layout_order_view);

        final RadioGroup mOrderGroup = (RadioGroup) view.findViewById(R.id.rg_order);
        mOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FileOrderType orderType = (checkedId == R.id.rbtn_order_name) ? FileOrderType.NAME : FileOrderType.TIME;
                if (mOrderType != orderType) {
                    mOrderType = orderType;
                    notifyRefreshComplete(false);
                }
            }
        });
        CheckBox mSwitcherBox = (CheckBox) view.findViewById(R.id.cb_switch_view);
        mSwitcherBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isListShown != isChecked) {
                    isListShown = isChecked;
                    if (isListShown) {
                        mGridLayout.setVisibility(View.GONE);
                        mListLayout.setVisibility(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        mListLayout.setVisibility(View.GONE);
                        mGridLayout.setVisibility(View.VISIBLE);
                        mGridAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mListPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.layout_pull_refresh_list);
        mListPullToRefreshView.setOnHeaderRefreshListener(mHeaderRefreshListener);
        mListPullToRefreshView.setOnFooterRefreshListener(mFooterRefreshListener);
        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mListView = (StickyListHeadersView) view.findViewById(R.id.listview_timeline);
        mListAdapter = new OneOSStickyListAdapter(getContext(), mFileList, mSelectedList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel(false);
            }
        }, mLoginSession);
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
        mListView.setFastScrollEnabled(false);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mListAdapter);

        mGridPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.layout_pull_refresh_grid);
        mGridPullToRefreshView.setOnHeaderRefreshListener(mHeaderRefreshListener);
        mGridPullToRefreshView.setOnFooterRefreshListener(mFooterRefreshListener);
        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mGridView = (StickyGridHeadersView) view.findViewById(R.id.gridview_timeline);
        mGridAdapter = new OneOSStickyGridAdapter(getContext(), mFileList, mSelectedList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel(false);
            }
        }, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setOnHeaderClickListener(new StickyGridHeadersView.OnHeaderClickListener() {

            @Override
            public void onHeaderClick(AdapterView<?> parent, View view, long id) {
                mGridView.toggleHeaderState(id);
                mGridAdapter.notifyDataSetChanged();

                // ImageView mStateView = (ImageView) view.findViewById(R.id.iv_state);
                // int headCount = mGridView.getCountFromHeader((int) id);
                // if (headCount == 0) {
                // mStateView.setImageResource(R.drawable.icon_timeline_open);
                // } else {
                // mStateView.setImageResource(R.drawable.icon_timeline_close);
                // }
            }
        });
        mGridView.setEmptyView(mEmptyView);
        mGridView.setAdapter(mGridAdapter);
    }

    public void setFileType(OneOSFileType type, String path) {
        this.mFileType = type;
        Log.d(TAG, "========Set FileType: " + type);
        autoPullToRefresh();
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        if (getFileAdapter().isMultiChooseModel()) {
            showSelectAndOperatePanel(false);
            return true;
        }

        return false;
    }

    /**
     * Get current file adapter
     *
     * @return
     */
    @Override
    public OneOSFileBaseAdapter getFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    protected void autoPullToRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isListShown) {
                    mListPullToRefreshView.headerRefreshing();
                } else {
                    mGridPullToRefreshView.headerRefreshing();
                }
            }
        }, Constants.DELAY_TIME_AUTO_REFRESH);
    }

    private void notifyRefreshComplete(boolean isItemChanged) {

        if (mFileType == OneOSFileType.PICTURE){
            Collections.sort(mFileList, new OneOSFileCTTimeComparator());
        }


        isListShown = FileViewerType.isList(mUserSettings.getFileViewerType());
//        if (mOrderType == FileOrderType.NAME) {
//            Collections.sort(mFileList, new OneOSFileNameComparator());
//        } else {
//            Collections.sort(mFileList, new OneOSFileTimeComparator());
//        }



        ArrayList<String> mSectionLetters = new ArrayList<>();
        int index = 0;
        String fmt = mMainActivity.getResources().getString(R.string.fmt_time_line);
        for (OneOSFile file : mFileList) {

            String letter = FileUtils.formatTime(file.getTime()*1000, fmt);
            if (mFileType == OneOSFileType.PICTURE){
                letter = FileUtils.formatTime(file.getCttime()*1000, fmt);
            }

            if (OneOSAPIs.isOneSpaceX1()){
                if (mFileType == OneOSFileType.PICTURE){
                    letter = FileUtils.formatTime(file.getPhototime()*1000, fmt);
                }else{
                    letter = FileUtils.formatTime(file.getTime()*1000, fmt);
                }
            }

            if (!mSectionLetters.contains(letter)) {
                mSectionLetters.add(letter);
                file.setSection(index);
                index++;
            } else {
                file.setSection(mSectionLetters.indexOf(letter));
            }

        }

        int sec = mSectionLetters.size();
        String[] sections = new String[sec];
        for (int i = 0; i < sec; i++) {
            sections[i] = mSectionLetters.get(i);
        }

        mListAdapter.updateSections(sections);
        mGridAdapter.updateSections(sections);


        if (isListShown) {
            mGridLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            mListAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mListView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        } else {
            mListLayout.setVisibility(View.GONE);
            mGridLayout.setVisibility(View.VISIBLE);
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mGridView.setSelection(mLastClickPosition);
                // mGridView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        }
        if (isPullDownRefresh) {
            mListPullToRefreshView.onHeaderRefreshComplete();
            mGridPullToRefreshView.onHeaderRefreshComplete();
        } else {
            mListPullToRefreshView.onFooterRefreshComplete();
            mGridPullToRefreshView.onFooterRefreshComplete();
        }
    }

    private void showOrderLayout(boolean isShown) {
        if (isShown == mOrderLayout.isShown()) {
            return;
        }

        if (isShown) {
            mOrderLayout.startAnimation(mSlideInAnim);
            mOrderLayout.setVisibility(View.VISIBLE);
        } else {
            mOrderLayout.startAnimation(mSlideOutAnim);
            mSlideOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mOrderLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showSelectAndOperatePanel(boolean isShown) {
        mParentFragment.showSelectBar(isShown);
        mParentFragment.showManageBar(isShown);
    }

    private void updateSelectAndManagePanel(boolean isMore) {

        mParentFragment.updateSelectBar(mFileList.size(), mSelectedList.size(), mFileSelectListener);
        mParentFragment.updateManageBar(mFileType, mSelectedList, isMore, mFileManageListener);
    }

    private boolean setMultiModel(boolean isSetMultiModel, int position) {
        boolean curIsMultiModel = getFileAdapter().isMultiChooseModel();
        if (curIsMultiModel == isSetMultiModel) {
            return false;
        }

        if (isSetMultiModel) {
            updateSelectAndManagePanel(false);
            showSelectAndOperatePanel(true);
            mListAdapter.setIsMultiModel(true);
            mGridAdapter.setIsMultiModel(true);
            mSelectedList.add(mFileList.get(position));
            getFileAdapter().notifyDataSetChanged();
            return true;
        } else {
            showSelectAndOperatePanel(false);
            mListAdapter.setIsMultiModel(false);
            mGridAdapter.setIsMultiModel(false);
            getFileAdapter().notifyDataSetChanged();
            return true;
        }
    }

    private void addSearchFilesByType(ArrayList<OneOSFile> files) {
        String type = OneOSFileType.getServerTypeName(mFileType);
        for (OneOSFile file : files) {
            if (file.getType().equalsIgnoreCase(type)) {
                mFileList.add(file);
            }
        }
    }

    private void getOneOSFileList(int page) {
        Log.d(TAG, "---------File type: " + mFileType);
        if (!EmptyUtils.isEmpty(mSearchFilter)) {
            OneOSSearchAPI searchAPI = new OneOSSearchAPI(mLoginSession);
            searchAPI.setOnFileListListener(new OneOSSearchAPI.OnSearchFileListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, ArrayList<OneOSFile> files) {
                    mFileList.clear();
                    if (!EmptyUtils.isEmpty(files)) {
                        addSearchFilesByType(files);
                    }

                    notifyRefreshComplete(true);
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete(true);
                }
            });
            searchAPI.search(mFileType, mSearchFilter);
        } else {
            OneOSListDBAPI listDbAPI = new OneOSListDBAPI(mLoginSession, mFileType);
            listDbAPI.setOnFileListListener(new OneOSListDBAPI.OnFileDBListListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, OneOSFileType type, int total, int pages, int page, ArrayList<OneOSFile> files) {
                    if (page == 0) {
                        mFileList.clear();
                    }
                    if (!EmptyUtils.isEmpty(files)) {
                        mFileList.addAll(files);
                        mPage = page;
                        mPages = pages;
                    }

                    notifyRefreshComplete(true);
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete(true);
                }
            });
            listDbAPI.list(page);
        }
    }
}
