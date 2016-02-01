package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileListAdapter;
import com.eli.oneos.model.oneos.api.OneOSListDBAPI;
import com.eli.oneos.model.oneos.api.OneOSListDirAPI;
import com.eli.oneos.model.oneos.comp.OneOSFileNameComparator;
import com.eli.oneos.model.oneos.comp.OneOSFileTimeComparator;
import com.eli.oneos.model.user.LoginManage;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FilePathPanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.pullrefresh.PullToRefreshBase;
import com.eli.oneos.widget.pullrefresh.PullToRefreshGridView;
import com.eli.oneos.widget.pullrefresh.PullToRefreshListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudFileFragment extends Fragment {
    private static final String TAG = CloudFileFragment.class.getSimpleName();

    protected MainActivity mMainActivity;
    protected BaseNavFragment mParentFragment;
    private FilePathPanel mPathPanel;
    private ListView mListView;
    private GridView mGridView;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private OneOSFileListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;
    private boolean isListShown = true;
    private FileOrderType mOrderType = FileOrderType.NAME;
    public OneOSFileType mFileType = OneOSFileType.PRIVATE;
    private LinearLayout mOrderLayout;
    private Animation mSlideInAnim, mSlideOutAnim;

    private LoginSession mLoginSession = null;
    private ArrayList<OneOSFile> mFileList = new ArrayList<>();
    private ArrayList<OneOSFile> mSelectedList = new ArrayList<>();
    protected String curPath = null;
    private int mLastClickPosition = 0, mLastClickItem2Top = 0;
    private boolean isSelectionLastPosition = false;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }

            OneOSFileBaseAdapter mAdapter = getCurFileAdapter();
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
                updateSelectAndOperatePanel();
            } else {
                OneOSFile file = mFileList.get(position);
                if (file.isDirectory()) {
                    mLastClickPosition = position;
                    mLastClickItem2Top = view.getTop();
                    curPath = file.getPath();
                    autoPullToRefresh();
                } else {
                    attemptOpenOneOSFile(file);
                }
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }

            OneOSFileBaseAdapter mAdapter = getCurFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (!isMultiMode) {
                setMultiModel(true, position);
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
                updateSelectAndOperatePanel();
            }

            return true;
        }
    };
    private FileSelectPanel.OnFileSelectListener mFileSelectListener = new FileSelectPanel.OnFileSelectListener() {
        @Override
        public void onSelect(boolean isSelectAll) {
            getCurFileAdapter().selectAllItem(isSelectAll);
            getCurFileAdapter().notifyDataSetChanged();
            updateSelectAndOperatePanel();
        }

        @Override
        public void onDismiss() {
            setMultiModel(false, 0);
        }
    };
    private FileManagePanel.OnFileManageListener mFileManageListener = new FileManagePanel.OnFileManageListener() {
        @Override
        public void onClick(View view, ArrayList<OneOSFile> selectedList, FileManageAction action) {
            if (EmptyUtils.isEmpty(selectedList)) {
                ToastHelper.showToast(R.string.tip_select_file);
            } else {
                OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });
                fileManage.manage(mFileType, action, selectedList);
            }
        }

        @Override
        public void onDismiss() {
        }
    };
    private int mFirstVisibleItem = 0;
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    // ToastHelper.showToast("停止...");
                    showOrderLayout(mFirstVisibleItem == 0);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    // ToastHelper.showToast("正在滑动...");
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    // ToastHelper.showToast("开始滚动...");
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mFirstVisibleItem = firstVisibleItem;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_directory, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (BaseNavFragment) getParentFragment();
        curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        mFileType = OneOSFileType.PRIVATE;

        initLoginSession();
        initView(view);

        return view;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Log.d(TAG, "On Configuration Changed");
//        int orientation = this.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//
//        }
//
//        mListView.setAdapter(mListAdapter);
//        mGridView.setAdapter(mGridAdapter);
//        mListAdapter.notifyDataSetChanged();
//        mGridAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onResume() {
        super.onResume();
        autoPullToRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        setMultiModel(false, 0);
    }

    private void initLoginSession() {
        mLoginSession = LoginManage.getInstance().getLoginSession();
    }

    private void initView(View view) {
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
                        mPullRefreshGridView.setVisibility(View.GONE);
                        mPullRefreshListView.setVisibility(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        mPullRefreshListView.setVisibility(View.GONE);
                        mPullRefreshGridView.setVisibility(View.VISIBLE);
                        mGridAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        mPathPanel = (FilePathPanel) view.findViewById(R.id.layout_path_panel);
        mPathPanel.setOnPathPanelClickListener(new FilePathPanel.OnPathPanelClickListener() {
            @Override
            public void onClick(View view, String path) {
                if (null == path) { // New Folder Button Clicked
                    OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            autoPullToRefresh();
                        }
                    });
                    fileManage.manage(FileManageAction.MKDIR, curPath);
                } else {
                    curPath = path;
                    autoPullToRefresh();
                }
            }
        });
        mPathPanel.showNewFolderButton(mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview_filelist);
        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mPullRefreshListView.setEmptyView(mEmptyView);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                setMultiModel(false, 0);
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        mListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(mListView);
        mListAdapter = new OneOSFileListAdapter(getContext(), mFileList, mSelectedList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
            }
        }, mLoginSession);
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
        mListView.setOnScrollListener(mScrollListener);
        mListView.setAdapter(mListAdapter);

        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview_filelist);
        mPullRefreshGridView.setEmptyView(mEmptyView);
        mPullRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {
                                                      @Override
                                                      public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                                                          setMultiModel(false, 0);
                                                          getOneOSFileList(curPath);
                                                      }

                                                      @Override
                                                      public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                                                      }
                                                  }

        );
        mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mGridView);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mSelectedList, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setOnScrollListener(mScrollListener);
        mGridView.setAdapter(mGridAdapter);
    }

    public void setFileType(OneOSFileType type, String path) {
        if (this.mFileType != type) {
            this.mFileType = type;
            this.curPath = path;
        }
    }

    private OneOSFileBaseAdapter getCurFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    private String getParentPath(String path) {
        int startIndex = path.lastIndexOf(File.separator) + 1;
        return path.substring(0, startIndex);
    }

    private void backToParentDir(String path) {
        String parentPath = getParentPath(path);
        Log.d(TAG, "----Parent Path: " + parentPath + "------");
        isSelectionLastPosition = true;
        curPath = parentPath;
        autoPullToRefresh();
    }

    private boolean tryBackToParentDir() {
        isSelectionLastPosition = true;
        Log.d(TAG, "=====Current Path: " + curPath + "========");
        if (mFileType == OneOSFileType.PRIVATE) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }
        if (mFileType == OneOSFileType.PUBLIC) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }
        if (mFileType == OneOSFileType.RECYCLE) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }

        return false;
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    public boolean onBackPressed() {
        if (getCurFileAdapter().isMultiChooseModel()) {
            showSelectAndOperatePanel(false);
            return true;
        }

        return tryBackToParentDir();
    }

    protected void autoPullToRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isListShown) {
                    mPullRefreshListView.setRefreshing();
                } else {
                    mPullRefreshGridView.setRefreshing();
                }
            }
        }, Constants.DELAY_TIME_AUTO_REFRESH);
    }

    private void notifyRefreshComplete(boolean isItemChanged) {
        if (mOrderType == FileOrderType.NAME) {
            Collections.sort(mFileList, new OneOSFileNameComparator());
        } else {
            Collections.sort(mFileList, new OneOSFileTimeComparator());
        }

        mPathPanel.updatePath(mFileType, curPath);
        if (isListShown) {
            mListAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshListView.onRefreshComplete();
            if (isSelectionLastPosition) {
                mListView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        } else {
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshGridView.onRefreshComplete();
            if (isSelectionLastPosition) {
                mGridView.setSelection(mLastClickPosition);
                // mGridView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
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

    private void updateSelectAndOperatePanel() {
        mParentFragment.updateSelectBar(mFileList.size(), mSelectedList.size(), mFileSelectListener);
        mParentFragment.updateOperateBar(mFileType, mSelectedList, mFileManageListener);
    }

    private boolean setMultiModel(boolean isSetMultiModel, int position) {
        boolean curIsMultiModel = getCurFileAdapter().isMultiChooseModel();
        if (curIsMultiModel == isSetMultiModel) {
            return false;
        }

        if (isSetMultiModel) {
            updateSelectAndOperatePanel();
            showSelectAndOperatePanel(true);
            mListAdapter.setIsMultiModel(true);
            mGridAdapter.setIsMultiModel(true);
            mSelectedList.add(mFileList.get(position));
            getCurFileAdapter().notifyDataSetChanged();
            return true;
        } else {
            showSelectAndOperatePanel(false);
            mListAdapter.setIsMultiModel(false);
            mGridAdapter.setIsMultiModel(false);
            getCurFileAdapter().notifyDataSetChanged();
            return true;
        }
    }

    private void attemptOpenOneOSFile(OneOSFile file) {
        // TODO... Open OneOS File
        ToastHelper.showToast("Open OneOS File is coming soon!");
    }

    private void getOneOSFileList(String path) {
        if (mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC || mFileType == OneOSFileType.RECYCLE) {
            if (EmptyUtils.isEmpty(path)) {
                Log.e(TAG, "Get list path is NULL");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyRefreshComplete(true);
                    }
                }, Constants.DELAY_TIME_AUTO_REFRESH);
                return;
            }

            OneOSListDirAPI listDirAPI = new OneOSListDirAPI(mLoginSession.getDeviceInfo().getIp(), mLoginSession.getDeviceInfo().getPort(), mLoginSession.getSession(), path);
            listDirAPI.setOnFileListListener(new OneOSListDirAPI.OnFileListListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, String path, ArrayList<OneOSFile> files) {
                    curPath = path;
                    mFileList.clear();
                    if (!EmptyUtils.isEmpty(files)) {
                        mFileList.addAll(files);
                    }

                    notifyRefreshComplete(true);
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete(true);
                }
            });
            listDirAPI.list();
        } else {
            OneOSListDBAPI listDbAPI = new OneOSListDBAPI(mLoginSession.getDeviceInfo().getIp(), mLoginSession.getDeviceInfo().getPort(), mLoginSession.getSession(), mFileType);
            listDbAPI.setOnFileListListener(new OneOSListDBAPI.OnFileDBListListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, OneOSFileType type, ArrayList<OneOSFile> files) {
                    mFileList.clear();
                    if (!EmptyUtils.isEmpty(files)) {
                        mFileList.addAll(files);
                    }

                    notifyRefreshComplete(true);
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete(true);
                }
            });
            listDbAPI.list();
        }
    }
}
