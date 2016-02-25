package com.eli.oneos.ui.nav.tools;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.db.BackupInfoHistoryKeeper;
import com.eli.oneos.db.greendao.BackupInfoHistory;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.backup.info.contacts.BackupContactManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.eli.oneos.widget.TitleBackLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Backup Contacts Activity
 */
public class BackupContactActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "BackupContactActivity";
    private Button mSyncContactsBtn, mRecoverContactsBtn;
    private ImageButton backBtn;
    private TextView syncContactsTime;// , syncContactsState, recoverContactsState;
    private TextView mStateTxt, mProgressTxt;
    private AnimCircleProgressBar mAnimCircleProgressBar;

    private BackupContactManager mBackupContactManager = null;
    private MyApplication mApplication = null;
    private OnBackupInfoListener mListener = new OnBackupInfoListener() {
        @Override
        public void onStart(BackupInfoType type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSyncContactsBtn.setEnabled(false);
                    mRecoverContactsBtn.setEnabled(false);
                }
            });
        }

        @Override
        public void onBackup(final BackupInfoType type, final BackupInfoStep step, final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecoverContactsBtn.setEnabled(false);
                    mSyncContactsBtn.setEnabled(false);

                    if (step == BackupInfoStep.EXPORT) {
                        mAnimCircleProgressBar.setMainProgress(progress);
                        mProgressTxt.setText(String.valueOf(progress));
                        mStateTxt.setText(R.string.exporting);
                    } else if (step == BackupInfoStep.UPLOAD) {
                        mStateTxt.setText(R.string.syncing);
                    } else if (step == BackupInfoStep.DOWNLOAD) {
                        mAnimCircleProgressBar.setMainProgress(progress);
                        mProgressTxt.setText(String.valueOf(progress));
                        mStateTxt.setText(R.string.recover_prepare);
                    } else if (step == BackupInfoStep.IMPORT) {
                        mAnimCircleProgressBar.setMainProgress(progress);
                        mProgressTxt.setText(String.valueOf(progress));
                        mStateTxt.setText(R.string.recovering);
                    }
                }
            });
        }

        @Override
        public void onComplete(final BackupInfoType type, final BackupInfoException exception) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecoverContactsBtn.setEnabled(true);
                    mSyncContactsBtn.setEnabled(true);

                    boolean success = (exception == null);
                    if (success) {
                        mAnimCircleProgressBar.setMainProgress(100);
                        mProgressTxt.setText(String.valueOf(100));
                    }

                    if (type == BackupInfoType.BACKUP_CONTACTS || type == BackupInfoType.BACKUP_SMS) {
                        if (success) {
                            mStateTxt.setText(R.string.sync_success);
                            setCompleteTime();
                        } else {
                            notifyFailedInfo(type, exception);
                        }
                    } else if (type == BackupInfoType.RECOVERY_CONTACTS || type == BackupInfoType.RECOVERY_SMS) {
                        if (success) {
                            mStateTxt.setText(R.string.recover_success);
                        } else {
                            notifyFailedInfo(type, exception);
                        }
                    }
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_sync_contacts);
        initSystemBarStyle();

        mBackupContactManager = BackupContactManager.getInstance();
        mBackupContactManager.setOnBackupInfoListener(mListener);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        BackupInfoHistory mBackupHistory = BackupInfoHistoryKeeper.getBackupHistory(loginSession.getUserInfo().getId(), BackupInfoType.BACKUP_CONTACTS);
        long time = 0;
        if (mBackupHistory != null) {
            time = mBackupHistory.getTime();
        }
        if (time <= 0) {
            syncContactsTime.setHint(R.string.not_sync);
        } else {
            syncContactsTime.setText(FileUtils.formatTime(time, "yyyy/MM/dd HH:mm"));
        }

        updateSyncButton();
    }

    /**
     * Find views by id
     */
    private void initViews() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_sync_contacts);

        mStateTxt = (TextView) findViewById(R.id.txt_state);
        mProgressTxt = (TextView) findViewById(R.id.txt_progress);
        mAnimCircleProgressBar = (AnimCircleProgressBar) findViewById(R.id.progressbar);

        mSyncContactsBtn = (Button) findViewById(R.id.btn_sync_contacts);
        mSyncContactsBtn.setOnClickListener(this);
        syncContactsTime = (TextView) findViewById(R.id.sync_time);

        mRecoverContactsBtn = (Button) findViewById(R.id.btn_recover_contacts);
        mRecoverContactsBtn.setOnClickListener(this);
    }

    private void updateSyncButton() {
        mSyncContactsBtn.setEnabled(true);
        mRecoverContactsBtn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync_contacts:
                Log.i(TAG, "Click Sync Contacts Button");
                mSyncContactsBtn.setEnabled(false);
                mRecoverContactsBtn.setEnabled(false);
                mBackupContactManager.startBackup();
                break;
            case R.id.btn_recover_contacts:
                mSyncContactsBtn.setEnabled(false);
                mRecoverContactsBtn.setEnabled(false);
                mBackupContactManager.startRecover();
                break;
            default:
                break;
        }
    }


    /**
     * set complete time
     */
    private void setCompleteTime() {
        syncContactsTime.setText(getCurTime());
    }

    /**
     * get current time of system
     */
    public String getCurTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String time = dateFormat.format(date);
        return time;
    }

    private void notifyFailedInfo(BackupInfoType type, BackupInfoException ex) {
        if (null != ex && type != null) {
            int title;
            int content;
            if (type == BackupInfoType.BACKUP_CONTACTS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.EXPORT_ERROR) {
                    content = R.string.error_export_contacts;
                } else if (ex == BackupInfoException.LOCAL_EMPTY) {
                    content = R.string.no_contact_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.SERVER_EMPTY) {
                    content = R.string.no_contact_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_contacts;
                }
            } else if (type == BackupInfoType.BACKUP_SMS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.EXPORT_ERROR) {
                    content = R.string.error_export_sms;
                } else if (ex == BackupInfoException.LOCAL_EMPTY) {
                    content = R.string.no_sms_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.SERVER_EMPTY) {
                    content = R.string.no_sms_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_sms;
                }
            }

            mStateTxt.setText(title);
            DialogUtils.showNotifyDialog(this, title, content, R.string.ok, null);
        }
    }
}
