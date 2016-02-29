package com.eli.oneos.ui.nav.tansfer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.model.oneos.adapter.RecordAdapter;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.DownloadManager;
import com.eli.oneos.model.oneos.transfer.TransferElement;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.transfer.UploadManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.widget.SwipeListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/19.
 */
public class RecordsFragment extends BaseTransferFragment {
    private static final String TAG = RecordsFragment.class.getSimpleName();

    private OneSpaceService mTransferService = null;
    private SwipeListView mListView;
    private RecordAdapter mAdapter;
    private TextView mEmptyTxt;
    private ArrayList<TransferHistory> mHistoryList = new ArrayList<>();
    private LoginManage loginManage;

    public RecordsFragment(boolean isDownload) {
        super(isDownload);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);

        initView(view);
        loginManage = LoginManage.getInstance();
        initTransferService();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTransferHistory();
    }

    private void initView(View view) {
        View mEmptyView = (View) view.findViewById(R.id.layout_empty);
        mEmptyTxt = (TextView) view.findViewById(R.id.txt_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_transfer);
        mListView.setEmptyView(mEmptyView);
        mAdapter = new RecordAdapter(getContext(), mHistoryList, isDownload, mListView.getRightViewWidth());
        mAdapter.setOnDeleteListener(new RecordAdapter.onDeleteClickListener() {
            @Override
            public void onDelete(int position) {
                TransferHistory history = mHistoryList.get(position);
                TransferHistoryKeeper.delete(history);
                mHistoryList.remove(history);
                mListView.hiddenRight();
                mAdapter.notifyDataSetChanged();
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TransferHistory history = mHistoryList.get(position);
                String path;
                if (isDownload) {
                    path = history.getTargetPath() + File.separator + history.getName();
                } else {
                    path = history.getSrcPath();
                }
                File file = new File(path);
                FileUtils.openLocalFile((BaseActivity) getActivity(), file);
            }
        });
    }

    private void initTransferHistory() {
        mHistoryList.clear();
        if (loginManage.isLogin()) {
            mEmptyTxt.setText(R.string.empty_transfer_list);
            List<TransferHistory> historyList = TransferHistoryKeeper.all(loginManage.getLoginSession().getUserInfo().getId(), isDownload);
            if (!EmptyUtils.isEmpty(historyList)) {
                mHistoryList.addAll(historyList);
            }
        } else {
            mEmptyTxt.setText(R.string.not_login);
        }
        Log.e(TAG, "TransferHistory Size: " + mHistoryList.size());
        mAdapter.notifyDataSetChanged();
    }

    private void initTransferService() {
        mTransferService = MyApplication.getTransferService();
        if (mTransferService != null) {
            if (isDownload) {
                mTransferService.setOnDownloadCompleteListener(new DownloadManager.OnDownloadCompleteListener() {
                    @Override
                    public void downloadComplete(DownloadElement element) {
                        Log.d(TAG, "---Download Complete: " + element.getSrcPath());
                        if (loginManage.isLogin()) {
                            final TransferHistory history = genTransferHistory(element, true);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHistoryList.add(0, history);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            } else {
                mTransferService.setOnUploadCompleteListener(new UploadManager.OnUploadCompleteListener() {
                    @Override
                    public void uploadComplete(UploadElement element) {
                        if (loginManage.isLogin()) {
                            final TransferHistory history = genTransferHistory(element, false);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHistoryList.add(0, history);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Log.e(TAG, "Get transfer service is null");
        }
    }

    private TransferHistory genTransferHistory(TransferElement element, boolean isDownload) {
        long uid = loginManage.getLoginSession().getUserInfo().getId();

        return new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(isDownload), element.getSrcName(),
                element.getSrcPath(), element.getTargetPath(), element.getSize(), 0L, System.currentTimeMillis());
    }

    /**
     * On Title Menu Click
     *
     * @param index
     * @param view
     */
    @Override
    public void onMenuClick(int index, View view) {
        if (loginManage.isLogin() && isVisible()) {
            mHistoryList.clear();
            mAdapter.notifyDataSetChanged();
            TransferHistoryKeeper.delete(loginManage.getLoginSession().getUserInfo().getId());
        }
    }

}
