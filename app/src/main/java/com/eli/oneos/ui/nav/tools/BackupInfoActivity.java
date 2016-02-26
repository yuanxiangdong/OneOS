package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.db.BackupInfoKeeper;
import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.model.logger.Logged;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoManager;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.eli.oneos.widget.TitleBackLayout;

/**
 * Backup Contacts or SMS Activity
 */
public class BackupInfoActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = BackupInfoActivity.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_SMS;
    public static final String EXTRA_BACKUP_INFO_TYPE = "is_backup_contacts";

    private Button mBackupBtn, mRecoverBtn;
    private TextView mBackupTimeTxt;// , syncContactsState, recoverContactsState;
    private TextView mStateTxt, mProgressTxt;
    private AnimCircleProgressBar mAnimCircleProgressBar;

    private BackupInfoType mBackupType = BackupInfoType.BACKUP_CONTACTS;
    private BackupInfoType mRecoveryType = BackupInfoType.RECOVERY_CONTACTS;
    private BackupInfoManager mBackupInfoManager = null;
    private OnBackupInfoListener mListener = new OnBackupInfoListener() {
        @Override
        public void onStart(final BackupInfoType type) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBackupBtn.setEnabled(false);
                        mRecoverBtn.setEnabled(false);
                    }
                });
            }
        }

        @Override
        public void onBackup(final BackupInfoType type, final BackupInfoStep step, final int progress) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(false);
                        mBackupBtn.setEnabled(false);

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
        }

        @Override
        public void onComplete(final BackupInfoType type, final BackupInfoException exception) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(true);
                        mBackupBtn.setEnabled(true);

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
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_backup_info);
        initSystemBarStyle();

        Intent intent = getIntent();
        boolean isBackupContacts = intent.getBooleanExtra(EXTRA_BACKUP_INFO_TYPE, true);
        mBackupType = isBackupContacts ? BackupInfoType.BACKUP_CONTACTS : BackupInfoType.BACKUP_SMS;
        mRecoveryType = isBackupContacts ? BackupInfoType.RECOVERY_CONTACTS : BackupInfoType.RECOVERY_SMS;

        mBackupInfoManager = BackupInfoManager.getInstance();
        mBackupInfoManager.setOnBackupInfoListener(mListener);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        BackupInfo mBackupHistory = BackupInfoKeeper.getBackupHistory(loginSession.getUserInfo().getId(), mBackupType);
        long time = 0;
        if (mBackupHistory != null) {
            time = mBackupHistory.getTime();
        }
        if (time <= 0) {
            mBackupTimeTxt.setHint(R.string.not_sync);
        } else {
            mBackupTimeTxt.setText(FileUtils.formatTime(time, "yyyy/MM/dd HH:mm"));
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

        mStateTxt = (TextView) findViewById(R.id.txt_state);
        mProgressTxt = (TextView) findViewById(R.id.txt_progress);
        mAnimCircleProgressBar = (AnimCircleProgressBar) findViewById(R.id.progressbar);

        mBackupBtn = (Button) findViewById(R.id.btn_sync_contacts);
        mBackupBtn.setOnClickListener(this);
        mBackupTimeTxt = (TextView) findViewById(R.id.sync_time);

        mRecoverBtn = (Button) findViewById(R.id.btn_recover_contacts);
        mRecoverBtn.setOnClickListener(this);

        if (mBackupType == BackupInfoType.BACKUP_SMS) {
            mTitleLayout.setTitle(R.string.title_sync_sms);
            mBackupBtn.setText(R.string.sync_sms_to_server);
            mRecoverBtn.setText(R.string.recover_sms_to_phone);
        } else {
            mTitleLayout.setTitle(R.string.title_sync_contacts);
            mBackupBtn.setText(R.string.sync_contacts_to_server);
            mRecoverBtn.setText(R.string.recover_contacts_to_phone);
        }
    }

    private void updateSyncButton() {
        mBackupBtn.setEnabled(true);
        mRecoverBtn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync_contacts:
                mBackupBtn.setEnabled(false);
                mRecoverBtn.setEnabled(false);
                if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
                    mBackupInfoManager.startBackupContacts();
                } else {
                    mBackupInfoManager.startBackupSMS();
                }
                break;
            case R.id.btn_recover_contacts:
                mBackupBtn.setEnabled(false);
                mRecoverBtn.setEnabled(false);
                if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
                    mBackupInfoManager.startRecoverContacts();
                } else {
                    mBackupInfoManager.startRecoverSMS();
                }
                break;
            default:
                break;
        }
    }


    /**
     * set complete time
     */
    private void setCompleteTime() {
        mBackupTimeTxt.setText(FileUtils.getCurFormatTime("yyyy/MM/dd HH:mm"));
    }

    private void notifyFailedInfo(BackupInfoType type, BackupInfoException ex) {
        if (null != ex && type != null) {
            int title;
            int content;
            if (type == BackupInfoType.BACKUP_CONTACTS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_contacts;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_contact_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_contact_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_contacts;
                }
            } else if (type == BackupInfoType.BACKUP_SMS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_sms;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_sms_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_sms_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_sms;
                }
            }

            mStateTxt.setText(content);
            DialogUtils.showNotifyDialog(this, title, content, R.string.ok, null);
        }
    }
}
