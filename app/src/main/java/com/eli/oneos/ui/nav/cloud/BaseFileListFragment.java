package com.eli.oneos.ui.nav.cloud;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.adapter.OneOSFileListAdapter;
import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.api.OneOSFileType;
import com.eli.oneos.model.api.OneOSListDirAPI;
import com.eli.oneos.model.comp.OneOSFileNameComparator;
import com.eli.oneos.model.comp.OneOSFileTimeComparator;
import com.eli.oneos.model.user.LoginManager;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.PathPanel;
import com.eli.oneos.widget.pullrefresh.PullToRefreshBase;
import com.eli.oneos.widget.pullrefresh.PullToRefreshGridView;
import com.eli.oneos.widget.pullrefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseFileListFragment extends Fragment {
    private static final String TAG = BaseFileListFragment.class.getSimpleName();

    private PathPanel mPathPanel;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private OneOSFileListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;
    private boolean isListShown = true;
    private FileOrderType mOrderType = FileOrderType.NAME;
    public OneOSFileType mFileType = OneOSFileType.PRIVATE;

    private LoginSession mLoginSession = null;
    private ArrayList<OneOSFile> mFileList = new ArrayList<>();
    private HashMap<Integer, Boolean> mSelectedMap = new HashMap<>();
    protected String curPath = null;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isListShown) {
                position -= 1; // for PullToRefreshView header
            }

            OneOSFileBaseAdapter mAdapter = getCurFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (isMultiMode) {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                mClickedCheckBox.toggle();
                mSelectedMap.put(position, mClickedCheckBox.isChecked());
                mAdapter.notifyDataSetChanged();

                // TODO.. update select title
                ToastHelper.showToast("You needs to update select title!");
            } else {
                attemptOpenOneOSFile(mFileList.get(position));
            }
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
        mLoginSession = LoginManager.getInstance().getLoginSession();
    }

    private void initView(View view) {
        RadioGroup mOrderGroup = (RadioGroup) view.findViewById(R.id.rg_order);
        mOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FileOrderType orderType = (checkedId == R.id.rbtn_order_name) ? FileOrderType.NAME : FileOrderType.TIME;
                if (mOrderType != orderType) {
                    mOrderType = orderType;
                    notifyRefreshComplete();
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
        mPathPanel = (PathPanel) view.findViewById(R.id.layout_path_panel);
        mPathPanel.setOnPathPanelClickListener(new PathPanel.OnPathPanelClickListener() {
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
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mPullRefreshListView.setEmptyView(mEmptyView);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                cancelMultiModel();
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        ListView mListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(mListView);
        mListAdapter = new OneOSFileListAdapter(getContext(), mFileList, mSelectedMap, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                mListAdapter.setIsMultiModel(true);
                mGridAdapter.setIsMultiModel(true);
                mSelectedMap.put((Integer) view.getTag(), true);
                mListAdapter.notifyDataSetChanged();
            }
        }, mLoginSession);
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setAdapter(mListAdapter);

        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview);
        mPullRefreshGridView.setEmptyView(mEmptyView);
        mPullRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                cancelMultiModel();
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        GridView mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mListView);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mSelectedMap, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setAdapter(mGridAdapter);
    }


    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    public boolean onBackPressed() {
        return cancelMultiModel();
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

    private void notifyRefreshComplete() {
        if (mOrderType == FileOrderType.NAME) {
            Collections.sort(mFileList, new OneOSFileNameComparator());
        } else {
            Collections.sort(mFileList, new OneOSFileTimeComparator());
        }

        mPathPanel.updatePath(curPath);
        if (isListShown) {
            mListAdapter.notifyDataSetChanged(true);
            mPullRefreshListView.onRefreshComplete();
        } else {
            mGridAdapter.notifyDataSetChanged(true);
            mPullRefreshGridView.onRefreshComplete();
        }
    }

    private boolean cancelMultiModel() {
        if (getCurFileAdapter().isMultiChooseModel()) {
            mListAdapter.setIsMultiModel(false);
            mGridAdapter.setIsMultiModel(false);
            mListAdapter.notifyDataSetChanged();
            mGridAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
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

                    notifyRefreshComplete();
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete();
                }
            });
            listDirAPI.list();
        } else {
            // TODO... list db file
            ToastHelper.showToast("List DB files is coming soon!");
            notifyRefreshComplete();
        }
    }
}
