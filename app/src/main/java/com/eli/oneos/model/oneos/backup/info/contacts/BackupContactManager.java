package com.eli.oneos.model.oneos.backup.info.contacts;

import com.eli.oneos.model.logger.Logged;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;

public class BackupContactManager {
    private static final String TAG = BackupContactManager.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_CONTACTS;

    private static BackupContactManager instance = new BackupContactManager();
    private OnBackupInfoListener mListener;
    private BackupContactsThread backupContactsThread = null;
    private RecoveryContactsThread recoveryContactsThread = null;

    /**
     * Singleton instance method
     *
     * @return singleton instance of class
     */
    public static BackupContactManager getInstance() {
        return instance;
    }

    /**
     * Start Backup Contacts to server
     */
    public void startBackup() {
        if (null == backupContactsThread || !backupContactsThread.isAlive()) {
            backupContactsThread = new BackupContactsThread(mListener);
            backupContactsThread.start();
        }
    }

    /**
     * Recover Contacts from server
     */
    public void startRecover() {
        if (null == recoveryContactsThread || !recoveryContactsThread.isAlive()) {
            recoveryContactsThread = new RecoveryContactsThread(mListener);
            recoveryContactsThread.start();
        }
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
        if (null != backupContactsThread) {
            backupContactsThread.setOnBackupInfoListener(mListener);
        }
        if (null != recoveryContactsThread) {
            recoveryContactsThread.setOnBackupInfoListener(mListener);
        }
    }
}
