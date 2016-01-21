package com.eli.oneos.model.oneos;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.api.OneOSFileManageAPI;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;

import java.util.ArrayList;

/**
 * OneSpace OS File Manage
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManage {

    private MainActivity mActivity;
    private LoginSession loginSession;
    private ArrayList<OneOSFile> fileList;
    private FileManageAction action;
    private OnManageCallback callback;
    private OneOSFileManageAPI fileManageAPI;
    private OneOSFileManageAPI.OnFileManageListener mListener = new OneOSFileManageAPI.OnFileManageListener() {
        @Override
        public void onStart(String url, ArrayList<OneOSFile> fileList, FileManageAction action) {
            if (action == FileManageAction.DELETE) {
                mActivity.showLoading(R.string.deleting_file);
            }

        }

        @Override
        public void onSuccess(String url, ArrayList<OneOSFile> fileList, FileManageAction action) {
            if (action == FileManageAction.DELETE) {
                mActivity.showTipView(R.string.delete_file_success, true);
            }

            if (null != callback) {
                callback.onComplete(true);
            }
        }

        @Override
        public void onFailure(String url, ArrayList<OneOSFile> fileList, FileManageAction action, int errorNo, String errorMsg) {
            if (action == FileManageAction.DELETE) {
                mActivity.showTipView(errorMsg, false);
            }

            if (null != callback) {
                callback.onComplete(false);
            }
        }
    };

    public OneOSFileManage(MainActivity activity, LoginSession loginSession, OnManageCallback callback) {
        this.mActivity = activity;
        this.loginSession = loginSession;
        this.callback = callback;
        fileManageAPI = new OneOSFileManageAPI(this.loginSession.getDeviceInfo().getIp(), this.loginSession.getDeviceInfo().getPort(), this.loginSession.getSession());
        fileManageAPI.setOnFileManageListener(mListener);
    }

    public void manage(FileManageAction action, ArrayList<OneOSFile> selectedList) {
        this.fileList = selectedList;
        this.action = action;

        if (EmptyUtils.isEmpty(fileList) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.DELETE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.tip, R.string.tip_delete_file, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageAPI.delete(fileList, false);
                    }
                }
            });
        }


    }

    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
