package com.eli.oneos.model.phone;

import android.content.res.Resources;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.phone.api.LocalFileManageTask;
import com.eli.oneos.model.phone.api.MakeDirAPI;
import com.eli.oneos.model.phone.api.ShareFileAPI;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.LocalFileTreeView;
import com.eli.oneos.widget.ServerFileTreeView;
import com.eli.oneos.widget.undobar.UndoBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * OneSpace OS File Manage
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class LocalFileManage {
    private static final String TAG = LocalFileManage.class.getSimpleName();

    private BaseActivity mActivity;
    private FileManageAction mAction;
    private View mRootView;
    private OnManageCallback callback;
    private List<LocalFile> fileList;
    private LocalFileManageTask fileManageTask;
    private LocalFileManageTask.OnLocalFileManageListener listener = new LocalFileManageTask.OnLocalFileManageListener() {
        @Override
        public void onStart(FileManageAction action) {
            if (action == FileManageAction.DELETE) {
                mActivity.showLoading(R.string.deleting_file);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showLoading(R.string.renaming_file);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showLoading(R.string.making_folder);
            } else if (action == FileManageAction.COPY) {
                mActivity.showLoading(R.string.copying_file);
            } else if (action == FileManageAction.MOVE) {
                mActivity.showLoading(R.string.moving_file);
            }
        }

        @Override
        public void onComplete(boolean result, FileManageAction action, String errorMsg) {
            if (result) {
                if (action == FileManageAction.ATTR) {
                    mActivity.dismissLoading();
                    // {"result":true, "path":"/PS-AI-CDR","dirs":1,"files":10,"size":3476576309,"uid":1001,"gid":0}
                    try {
                        File file = fileList.get(0).getFile();
                        Resources resources = mActivity.getResources();
                        List<String> titleList = new ArrayList<>();
                        List<String> contentList = new ArrayList<>();
                        titleList.add(resources.getString(R.string.file_attr_path));
                        contentList.add(file.getAbsolutePath());
                        titleList.add(resources.getString(R.string.file_attr_size));
                        contentList.add(FileUtils.fmtFileSize(file.length()) + " (" + file.length() + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                        titleList.add(resources.getString(R.string.file_attr_time));
                        contentList.add(FileUtils.formatTime(file.lastModified()));
                        titleList.add(resources.getString(R.string.file_attr_read));
                        contentList.add(file.canRead() ? "True" : "False");
                        titleList.add(resources.getString(R.string.file_attr_write));
                        contentList.add(file.canWrite() ? "True" : "False");
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
                } else if (action == FileManageAction.COPY) {
                    mActivity.showTipView(R.string.copy_file_success, true);
                } else if (action == FileManageAction.MOVE) {
                    mActivity.showTipView(R.string.move_file_success, true);
                } else if (action == FileManageAction.SHARE) {
                    mActivity.showTipView(R.string.share_file_success, true);
                }
            } else {
                mActivity.showTipView(R.string.operate_failed, false);
            }

            if (null != callback) {
                callback.onComplete(result);
            }
        }
    };

    public LocalFileManage(BaseActivity activity, View rootView, OnManageCallback callback) {
        this.mActivity = activity;
        this.mRootView = rootView;
        this.callback = callback;
    }

    public void manage(final LocalFileType type, FileManageAction action, final ArrayList<LocalFile> selectedList) {
        this.mAction = action;
        this.fileList = selectedList;

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
                        fileManageTask = new LocalFileManageTask(mActivity, selectedList, mAction, null, listener);
                        fileManageTask.execute(0);
                    }
                }
            });
        } else if (action == FileManageAction.RENAME) {
            final LocalFile file = selectedList.get(0);
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
                                    DialogUtils.dismiss();
                                    fileManageTask = new LocalFileManageTask(mActivity, selectedList, mAction, newName, listener);
                                    fileManageTask.execute(0);
                                }
                            }
                        }
                    });
        } else if (action == FileManageAction.COPY) {
            LocalFileTreeView fileTreeView = new LocalFileTreeView(mActivity, R.string.tip_copy_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new LocalFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageTask = new LocalFileManageTask(mActivity, selectedList, mAction, tarPath, listener);
                    fileManageTask.execute(0);
                }
            });
        } else if (action == FileManageAction.MOVE) {
            LocalFileTreeView fileTreeView = new LocalFileTreeView(mActivity, R.string.tip_move_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new LocalFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageTask = new LocalFileManageTask(mActivity, selectedList, mAction, tarPath, listener);
                    fileManageTask.execute(0);
                }
            });
        } else if (action == FileManageAction.UPLOAD) {
            LoginManage loginManage = LoginManage.getInstance();
            if (!loginManage.isLogin()) {
                mActivity.showTipView(R.string.please_login_onespace, false);
            } else {
                ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginManage.getLoginSession(), R.string.tip_upload_file, R.string.upload_file);
                fileTreeView.showPopupCenter(mRootView);
                fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                    @Override
                    public void onPaste(String tarPath) {
                        uploadFiles(tarPath);
                    }
                });
            }
        } else if (action == FileManageAction.ATTR) {
            listener.onComplete(true, action, null);
        } else if (action == FileManageAction.SHARE) {
            ShareFileAPI shareFileAPI = new ShareFileAPI();
            shareFileAPI.share(selectedList, mActivity);
        }
    }

    public void manage(final FileManageAction action, final String path) {
        this.mAction = action;

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
                                String name = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(name)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                } else {
                                    Log.d(TAG, "MkDir: " + path + ", Name: " + name);
                                    MakeDirAPI mkDirAPI = new MakeDirAPI();
                                    boolean ret = mkDirAPI.mkdir(path + File.separator + name);
                                    listener.onComplete(ret, action, null);

                                    InputMethodUtils.hideKeyboard(mActivity, mContentEditText);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        }

    }


    private void uploadFiles(String toPath) {
        String names = "";
        int count = fileList.size() >= 4 ? 4 : fileList.size();
        for (int i = 0; i < count; i++) {
            names += fileList.get(i).getName() + " ";
        }
        new UndoBar.Builder(mActivity).setMessage(mActivity.getResources().getString(R.string.tip_start_upload) + names)
                .setListener(new UndoBar.StatusBarListener() {

                    @Override
                    public void onUndo(Parcelable token) {
                    }

                    @Override
                    public void onClick() {
                        mActivity.controlActivity(MainActivity.ACTION_SHOW_TRANSFER_UPLOAD);
                    }

                    @Override
                    public void onHide() {
                    }
                }).show();
        OneSpaceService service = MyApplication.getTransferService();
        for (LocalFile file : fileList) {
            service.addUploadTask(file.getFile(), toPath);
        }

        if (null != callback) {
            callback.onComplete(true);
        }
    }

    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
