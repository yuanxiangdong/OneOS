package com.eli.oneos.model.oneos;

import android.content.res.Resources;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.api.OneOSFileManageAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.ServerFileTreeView;
import com.eli.oneos.widget.undobar.UndoBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * OneSpace OS File Manage
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManage {
    private static final String TAG = OneOSFileManage.class.getSimpleName();

    private BaseActivity mActivity;
    private LoginSession loginSession;
    private FileManageAction action;
    private View mRootView;
    private OnManageCallback callback;
    private List<OneOSFile> fileList;
    private OneOSFileManageAPI fileManageAPI;
    private OneOSFileManageAPI.OnFileManageListener mListener = new OneOSFileManageAPI.OnFileManageListener() {
        @Override
        public void onStart(String url, FileManageAction action) {
            if (action == FileManageAction.ATTR) {
                mActivity.showLoading(R.string.getting_file_attr);
            } else if (action == FileManageAction.DELETE) {
                mActivity.showLoading(R.string.deleting_file);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showLoading(R.string.renaming_file);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showLoading(R.string.making_folder);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showLoading(R.string.encrypting_file);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showLoading(R.string.decrypting_file);
            } else if (action == FileManageAction.COPY) {
                mActivity.showLoading(R.string.copying_file);
            } else if (action == FileManageAction.MOVE) {
                mActivity.showLoading(R.string.moving_file);
            } else if (action == FileManageAction.CLEAN_RECYCLE) {
                mActivity.showLoading(R.string.cleaning_recycle);
            }
        }

        @Override
        public void onSuccess(String url, FileManageAction action, String response) {
            if (action == FileManageAction.ATTR) {
                mActivity.dismissLoading();
                // {"result":true, "srcPath":"/PS-AI-CDR","dirs":1,"files":10,"size":3476576309,"uid":1001,"gid":0}
                try {
                    OneOSFile file = fileList.get(0);
                    Resources resources = mActivity.getResources();
                    List<String> titleList = new ArrayList<>();
                    List<String> contentList = new ArrayList<>();
                    JSONObject json = new JSONObject(response);
                    titleList.add(resources.getString(R.string.file_attr_path));
                    contentList.add(json.getString("srcPath"));
                    titleList.add(resources.getString(R.string.file_attr_size));
                    long size = json.getLong("size");
                    contentList.add(FileUtils.fmtFileSize(size) + " (" + size + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                    if (file.isDirectory()) {
                        titleList.add(resources.getString(R.string.file_attr_folders));
                        contentList.add(json.getString("dirs") + resources.getString(R.string.tail_file_attr_folders));
                        titleList.add(resources.getString(R.string.file_attr_files));
                        contentList.add(json.getString("files") + resources.getString(R.string.tail_file_attr_files));
                    }
                    titleList.add(resources.getString(R.string.file_attr_permission));
                    contentList.add(file.getPerm());
                    titleList.add(resources.getString(R.string.file_attr_uid));
                    contentList.add(json.getString("uid"));
                    titleList.add(resources.getString(R.string.file_attr_gid));
                    contentList.add(json.getString("gid"));
                    DialogUtils.showListDialog(mActivity, titleList, contentList, R.string.tip_attr_file, R.string.ok, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(R.string.error_json_exception);
                }
            } else if (action == FileManageAction.DELETE) {
                mActivity.showTipView(R.string.delete_file_success, true);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showTipView(R.string.rename_file_success, true);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showTipView(R.string.new_folder_success, true);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showTipView(R.string.encrypt_file_success, true);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showTipView(R.string.decrypt_file_success, true);
            } else if (action == FileManageAction.COPY) {
                mActivity.showTipView(R.string.copy_file_success, true);
            } else if (action == FileManageAction.MOVE) {
                mActivity.showTipView(R.string.move_file_success, true);
            } else if (action == FileManageAction.CLEAN_RECYCLE) {
                mActivity.showTipView(R.string.clean_recycle_success, true);
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

    public OneOSFileManage(BaseActivity activity, LoginSession loginSession, View rootView, OnManageCallback callback) {
        this.mActivity = activity;
        this.loginSession = loginSession;
        this.mRootView = rootView;
        this.callback = callback;
        fileManageAPI = new OneOSFileManageAPI(this.loginSession.getDeviceInfo().getIp(), this.loginSession.getDeviceInfo().getPort(), this.loginSession.getSession());
        fileManageAPI.setOnFileManageListener(mListener);
    }

    public void manage(final OneOSFileType type, FileManageAction action, final ArrayList<OneOSFile> selectedList) {
        this.action = action;
        this.fileList = selectedList;

        if (EmptyUtils.isEmpty(selectedList) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.ATTR) {
            fileManageAPI.attr(selectedList.get(0));
        } else if (action == FileManageAction.DELETE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.tip, R.string.tip_delete_file, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageAPI.delete(selectedList, type == OneOSFileType.RECYCLE);
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
            DialogUtils.showEditPwdDialog(mActivity, R.string.tip_encrypt_file, R.string.warning_encrypt_file, R.string.hint_encrypt_pwd, R.string.hint_confirm_encrypt_pwd,
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
        } else if (action == FileManageAction.COPY) {
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginSession, R.string.tip_copy_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageAPI.copy(selectedList, tarPath);
                }
            });
        } else if (action == FileManageAction.MOVE) {
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginSession, R.string.tip_move_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageAPI.move(selectedList, tarPath);
                }
            });
        } else if (action == FileManageAction.CLEAN_RECYCLE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.title_clean_recycle_file, R.string.tip_clean_recycle_file, R.string.clean_now, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageAPI.cleanRecycle();
                    }
                }
            });
        } else if (action == FileManageAction.DOWNLOAD) {
            downloadFiles();
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
                                    Log.d(TAG, "MkDir: " + path + ", Name: " + newName);
                                    fileManageAPI.mkdir(path, newName);
                                    InputMethodUtils.hideKeyboard(mActivity, mContentEditText);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        }

    }

    private void downloadFiles() {
        String names = "";
        int count = fileList.size() >= 4 ? 4 : fileList.size();
        for (int i = 0; i < count; i++) {
            names += fileList.get(i).getName() + " ";
        }
        new UndoBar.Builder(mActivity).setMessage(mActivity.getResources().getString(R.string.tip_start_download) + names)
                .setListener(new UndoBar.StatusBarListener() {

                    @Override
                    public void onUndo(Parcelable token) {
                    }

                    @Override
                    public void onClick() {
                        mActivity.controlActivity(MainActivity.ACTION_SHOW_TRANSFER_DOWNLOAD);
                    }

                    @Override
                    public void onHide() {
                    }
                }).show();
        String savePath = LoginManage.getInstance().getLoginSession().getDownloadPath();
        OneSpaceService service = MyApplication.getTransferService();
        for (OneOSFile file : fileList) {
            service.addDownloadTask(file, savePath);
        }

        if (null != callback) {
            callback.onComplete(true);
        }
    }

    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
