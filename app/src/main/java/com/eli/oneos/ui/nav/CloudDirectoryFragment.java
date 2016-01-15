package com.eli.oneos.ui.nav;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.adapter.OneOSFileListAdapter;
import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.api.OneOSGetFilesAPI;
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

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudDirectoryFragment extends Fragment {
    private static final String TAG = CloudDirectoryFragment.class.getSimpleName();

    private PathPanel mPathPanel;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private OneOSFileListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;
    private boolean isListShown = true;
    private FileOrderType mOrderType = FileOrderType.NAME;

    private CheckBox mSwitcherBox;

    private LoginSession mLoginSession = null;
    private ArrayList<OneOSFile> mFileList = new ArrayList<>();
    private String curPath = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_directory, container, false);

        mLoginSession = LoginManager.getInstance().getLoginSession();
        curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;

        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        autoPullToRefresh();
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
        mSwitcherBox = (CheckBox) view.findViewById(R.id.cb_switch_view);
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
                } else {
                    curPath = path;
                    autoPullToRefresh();
                }
            }
        });

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview);
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mPullRefreshListView.setEmptyView(mEmptyView);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        final ListView mListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(mListView);
        mListAdapter = new OneOSFileListAdapter(getContext(), mFileList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                mListAdapter.setIsMultiModel(true);
                mGridAdapter.setIsMultiModel(true);
            }
        }, mLoginSession);
        mListView.setAdapter(mListAdapter);

        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview);
        mPullRefreshGridView.setEmptyView(mEmptyView);
        mPullRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        GridView mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mListView);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mLoginSession);
        mGridView.setAdapter(mGridAdapter);
    }

    private void autoPullToRefresh() {
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

    private void getOneOSFileList(String path) {
        if (EmptyUtils.isEmpty(path)) {
            Log.e(TAG, "Get list path is NULL");
            return;
        }

        OneOSGetFilesAPI getFilesAPI = new OneOSGetFilesAPI(mLoginSession.getDeviceInfo().getIp(), mLoginSession.getDeviceInfo().getPort(), mLoginSession.getSession(), path);
        getFilesAPI.setOnFileListListener(new OneOSGetFilesAPI.OnFileListListener() {
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
        getFilesAPI.list();
    }
}
