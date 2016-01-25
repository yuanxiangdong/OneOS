package com.eli.oneos.model.oneos;

import android.widget.EditText;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.api.OneOSFileManageAPI;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
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
    private FileManageAction action;
    private OnManageCallback callback;
    private OneOSFileManageAPI fileManageAPI;
    private OneOSFileManageAPI.OnFileManageListener mListener = new OneOSFileManageAPI.OnFileManageListener() {
        @Override
        public void onStart(String url, FileManageAction action) {
            if (action == FileManageAction.DELETE) {
                mActivity.showLoading(R.string.deleting_file);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showLoading(R.string.renaming_file);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showLoading(R.string.making_folder);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showLoading(R.string.encrypting_file);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showLoading(R.string.decrypting_file);
            }
        }

        @Override
        public void onSuccess(String url, FileManageAction action) {
            if (action == FileManageAction.DELETE) {
                mActivity.showTipView(R.string.delete_file_success, true);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showTipView(R.string.rename_file_success, true);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showTipView(R.string.new_folder_success, true);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showTipView(R.string.encrypt_file_success, true);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showTipView(R.string.decrypt_file_success, true);
            }

            if (null != callback) {
                callback.onComplete(true);
            }
        }

        @Override
        public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
//            if (action == FileManageAction.DELETE) {
            mActivity.showTipView(errorMsg, false);
//            }

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

    public void manage(FileManageAction action, final ArrayList<OneOSFile> selectedList) {
        this.action = action;

        if (EmptyUtils.isEmpty(selectedList) || action == null) {
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
                        fileManageAPI.delete(selectedList, false);
                    }
                }
            });
        } else if (action == FileManageAction.RENAME) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_rename_file, R.string.hint_rename_file, file.getName(),
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.rename(file, newName);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        } else if (action == FileManageAction.ENCRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditPwdDialog(mActivity, R.string.tip_encrypt_file, R.string.hint_encrypt_pwd, R.string.hint_confirm_encrypt_pwd,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString();
                                fileManageAPI.crypt(file, pwd, true);
                                DialogUtils.dismiss();
                            }
                        }
                    });
        } else if (action == FileManageAction.DECRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_decrypt_file, R.string.hint_decrypt_pwd, null,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(pwd)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.crypt(file, pwd, false);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        }

    }

    public void manage(FileManageAction action, final String path) {
        this.action = action;

        if (EmptyUtils.isEmpty(path) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.MKDIR) {
            DialogUtils.showEditDialog(mActivity, R.string.tip_new_folder, R.string.hint_new_folder, R.string.default_new_folder,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                } else {
                                    fileManageAPI.mkdir(path, newName);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        }

    }

    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
