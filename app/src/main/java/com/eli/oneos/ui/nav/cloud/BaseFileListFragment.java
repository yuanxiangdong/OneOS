package com.eli.oneos.ui.nav.cloud;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileListAdapter;
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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseFileListFragment extends Fragment {
    private static final String TAG = BaseFileListFragment.class.getSimpleName();

    protected MainActivity mMainActivity;
    protected BaseNavFragment mParentFragment;
    private FilePathPanel mPathPanel;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private OneOSFileListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;
    private boolean isListShown = true;
    private FileOrderType mOrderType = FileOrderType.NAME;
    public OneOSFileType mFileType = OneOSFileType.PRIVATE;
    private LinearLayout mOrderLayout;

    private LoginSession mLoginSession = null;
    private ArrayList<OneOSFile> mFileList = new ArrayList<>();
    private ArrayList<OneOSFile> mSelectedList = new ArrayList<>();
    protected String curPath = null;

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
                attemptOpenOneOSFile(mFileList.get(position));
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }
            setMultiModel(true, position);

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
                OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, new OneOSFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });
                fileManage.manage(action, selectedList);
            }
        }

        @Override
        public void onDismiss() {
        }
    };

    public OneOSFileBaseAdapter getCurFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    public void initBaseParams(View view) {
        initLoginSession();
        initView(view);
    }

    private void initLoginSession() {
        mLoginSession = LoginManage.getInstance().getLoginSession();
    }

    private void initView(View view) {
        mOrderLayout = (LinearLayout) view.findViewById(R.id.layout_order_view);
        

        RadioGroup mOrderGroup = (RadioGroup) view.findViewById(R.id.rg_order);
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
                    // TODO... Do new folder
                    ToastHelper.showToast("New Folder is coming soon!");
                } else {
                    curPath = path;
                    autoPullToRefresh();
                }
            }
        });
        mPathPanel.showNewFolderButton(mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview);
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
        ListView mListView = mPullRefreshListView.getRefreshableView();
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
        mListView.setAdapter(mListAdapter);

        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview);
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
        });
        GridView mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mListView);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mSelectedList, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setAdapter(mGridAdapter);
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

        return false;
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

        mPathPanel.updatePath(curPath);
        if (isListShown) {
            mListAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshListView.onRefreshComplete();
        } else {
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshGridView.onRefreshComplete();
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
        if (file.isDirectory()) {
            curPath = file.getPath();
            autoPullToRefresh();
        } else {
            // TODO... Open OneOS File
            ToastHelper.showToast("Open OneOS File is coming soon!");
        }
    }

    private void getOneOSFileList(String path) {
        if (EmptyUtils.isEmpty(path)) {
            Log.e(TAG, "Get list path is NULL");
            return;
        }

        if (mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC || mFileType == OneOSFileType.RECYCLE) {
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
            // TODO... list db file
            ToastHelper.showToast("List DB files is coming soon!");
            notifyRefreshComplete(true);
        }
    }
}
