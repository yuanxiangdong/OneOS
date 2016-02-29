package com.eli.oneos.model.oneos.backup.file;

import android.content.Context;
import android.os.Environment;

import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.backup.BackupPriority;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.backup.RecursiveFileObserver;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.transfer.UploadFileThread;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.SDCardUtils;
import com.eli.oneos.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupFileManager {
    private static final String TAG = BackupFileManager.class.getSimpleName();

    public static final boolean USE_FILE_OBSERVER = true;

    private List<BackupFileElement> completeList = Collections.synchronizedList(new ArrayList<BackupFileElement>());
    private BackupScanFileThread mBackupThread = null;
    private List<RecursiveFileObserver> mFileObserverList = new ArrayList<>();
    private HandlerQueueThread handlerQueueThread = null;

    private LoginSession mLoginSession = null;
    private List<BackupFile> mBackupList = null;
    private Context context;
    private long mLastBackupTime = 0;

    private BackupScanFileThread.OnScanFileListener mScanListener = new BackupScanFileThread.OnScanFileListener() {
        @Override
        public void onComplete(ArrayList<BackupFileElement> mBackupList) {
            addBackupElements(mBackupList);
            if (USE_FILE_OBSERVER && !EmptyUtils.isEmpty(mFileObserverList)) {
                for (RecursiveFileObserver observer : mFileObserverList) {
                    observer.startWatching();
                }
            }
        }
    };

    private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {

        @Override
        public void onAdd(BackupFile backupInfo, File file) {
            BackupFileElement mElement = new BackupFileElement(backupInfo, file, true);
            if (mElement != null) {
                addBackupElement(mElement);
            }
        }
    };

    public BackupFileManager(LoginSession mLoginSession, Context context) {
        this.mLoginSession = mLoginSession;
        this.context = context;

        mBackupList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId());
        initBackupPhotoIfNeeds();

        handlerQueueThread = new HandlerQueueThread();
        if (USE_FILE_OBSERVER) {
            for (BackupFile info : mBackupList) {
                RecursiveFileObserver mFileObserver = new RecursiveFileObserver(info, info.getPath(),
                        RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
                mFileObserverList.add(mFileObserver);
            }
        }
        mBackupThread = new BackupScanFileThread(mBackupList, mScanListener);
    }

    private boolean initBackupPhotoIfNeeds() {
        boolean isNewBackupPath = false;
        File mExternalDCIMDir = SDCardUtils.getExternalSDCard();
        if (null != mExternalDCIMDir) {
            File mExternalDCIM = new File(mExternalDCIMDir, "DCIM");
            if (null != mExternalDCIM && mExternalDCIM.exists()) {
                BackupFile info = BackupFileKeeper.getBackupInfo(mLoginSession.getUserInfo().getId(), mExternalDCIM.getAbsolutePath());
                if (null == info) {
                    info = new BackupFile(null, mLoginSession.getUserInfo().getId(), mExternalDCIM.getAbsolutePath(),
                            true, BackupType.ALBUM, BackupPriority.MAX, System.currentTimeMillis(), 0L);
                    BackupFileKeeper.insertOrReplace(info);
                    Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Add New Backup Album Dir: " + info.getPath());
                    isNewBackupPath = true;
                    mBackupList.add(info);
                }
            }
        }
        File mInternalDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (null != mInternalDCIMDir && mInternalDCIMDir.exists()) {
            BackupFile info = BackupFileKeeper.getBackupInfo(mLoginSession.getUserInfo().getId(), mInternalDCIMDir.getAbsolutePath());
            if (null == info) {
                info = new BackupFile(null, mLoginSession.getUserInfo().getId(), mInternalDCIMDir.getAbsolutePath(),
                        true, BackupType.ALBUM, BackupPriority.MAX, System.currentTimeMillis(), 0L);
                BackupFileKeeper.insertOrReplace(info);
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Add New Backup Album Dir: " + info.getPath());
                isNewBackupPath = true;
                mBackupList.add(info);
            }
        }

        return isNewBackupPath;
    }

    public void startBackup() {
        if (handlerQueueThread != null) {
            handlerQueueThread.start();
        }

        if (mBackupThread != null) {
            mBackupThread.start();
        }
    }

    public void stopBackup() {
        if (handlerQueueThread != null) {
            handlerQueueThread.stopBackupThread();
            handlerQueueThread = null;
        }

        if (mBackupThread != null && mBackupThread.isAlive()) {
            mBackupThread.stopBackupThread();
            mBackupThread = null;
        }

        if (mFileObserverList != null) {
            for (RecursiveFileObserver observer : mFileObserverList) {
                observer.stopWatching();
            }
        }
    }

    public int getBackupListSize() {
        if (handlerQueueThread == null) {
            return 0;
        } else {
            return handlerQueueThread.getBackupListSize();
        }
    }

    private boolean addBackupElements(List<BackupFileElement> mList) {
        if (mList == null) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "Backup List is empty, nothing need to add");
            return false;
        }

        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "==>>>Add backup list, size: " + mList.size());
        if (handlerQueueThread != null) {
            if (!handlerQueueThread.isRunning) {
                handlerQueueThread.start();
            }

            return handlerQueueThread.notifyAddNewBackupItems(mList);
        }

        return false;
    }

    private boolean addBackupElement(BackupFileElement mElement) {
        if (mElement == null) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "Backup element is empty, nothing need to add");
            return false;
        }

        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "==>>>Add backup item: " + mElement.toString());
        if (handlerQueueThread != null) {
            if (!handlerQueueThread.isRunning) {
                handlerQueueThread.start();
            }

            return handlerQueueThread.notifyAddNewBackupItem(mElement);
        }

        return false;
    }

    private class HandlerQueueThread extends Thread {
        private final String TAG = HandlerQueueThread.class.getSimpleName();
        private List<BackupFileElement> mBackupList = Collections.synchronizedList(new ArrayList<BackupFileElement>());
        private UploadFileThread backupPhotoThread = null;
        private boolean isRunning = false;
        private boolean hasBackupTask = false;
        private UploadFileThread.OnUploadResultListener listener = new UploadFileThread.OnUploadResultListener() {
            @Override
            public void onResult(UploadElement element) {
                BackupFileElement mElement = (BackupFileElement) element;
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Backup Result: " + mElement.getFile().getName() + ", State: " + mElement.getState() + ", Time: " + System.currentTimeMillis());

                stopCurrentBackupTask();

                if (mBackupList.contains(mElement)) {
                    if (mElement.getState() == TransferState.COMPLETE) {
                        mElement.setTime(System.currentTimeMillis());

                        mLastBackupTime = mElement.getFile().lastModified();
                        if (mLastBackupTime > mElement.getBackupInfo().getTime()) {
                            mElement.getBackupInfo().setTime(mLastBackupTime);
                            if (BackupFileKeeper.update(mElement.getBackupInfo())) {
                                Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Update Database Last Backup Time Success: " + FileUtils.formatTime(mLastBackupTime));
                            } else {
                                Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "Update Database Last Backup Time Failed");
                                return;
                            }
                        }

                        completeList.add(mElement);
                        mBackupList.remove(mElement);
                        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Backup Complete");
                    } else {
                        if (mElement.getState() == TransferState.FAILED && mElement.getException() == TransferException.FILE_NOT_FOUND) {
                            mBackupList.remove(mElement);
                        } else {
                            Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "Backup Failed");
                            mElement.setState(TransferState.WAIT);
                        }
                    }
                }

                try {
                    sleep(10); // sleep 10ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                notifyNewBackupTask();
            }
        };

        @Override
        public synchronized void start() {
            if (!isRunning) {
                isRunning = true;
                super.start();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                if (hasBackupTask) {
                    synchronized (this) {
                        try {
                            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Waiting for Backup task stop: " + System.currentTimeMillis());
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Waiting for Backup List Change: " + System.currentTimeMillis());
                    synchronized (mBackupList) {
                        mBackupList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // for control backup only in wifi
                boolean isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupFileOnlyWifi();
                while (isOnlyWifiBackup && !Utils.isWifiAvailable(context)) {
                    try {
                        sleep(60000); // sleep 60 * 1000 = 60s
                        isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupFileOnlyWifi();
                        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "----Is Backup Only Wifi: " + isOnlyWifiBackup);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (BackupFileElement element : mBackupList) {
                    if (element.getState() == TransferState.WAIT) {
                        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Start a New Backup Task");
                        hasBackupTask = true;
                        backupPhotoThread = new UploadFileThread(element, mLoginSession, listener);
                        backupPhotoThread.start();
                        break;
                    }
                }
            }
        }

        /**
         * stop current Backup task, called when current Backup thread over,
         * before remove upload list
         */
        private synchronized void stopCurrentBackupTask() {
            hasBackupTask = false;
            synchronized (this) {
                this.notify();
            }
        }

        /**
         * notified to start a new Backup task, called after Backup thread over
         * and Backup list removed
         */
        private synchronized void notifyNewBackupTask() {
            synchronized (mBackupList) {
                mBackupList.notify();
            }
        }

        public synchronized boolean notifyAddNewBackupItems(List<BackupFileElement> mAddList) {
            if (mAddList.size() <= 0) {
                return false;
            }

            synchronized (mBackupList) {
                int curSize = mBackupList.size();
                if (mBackupList.addAll(mAddList)) {
                    if (curSize <= 0) {
                        if (!hasBackupTask) {
                            mBackupList.notify();
                        }
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        public synchronized boolean notifyAddNewBackupItem(BackupFileElement mElement) {
            if (mElement == null) {
                return false;
            }

            synchronized (mBackupList) {
                int curSize = mBackupList.size();
                if (mBackupList.add(mElement)) {
                    if (curSize <= 0) {
                        if (!hasBackupTask) {
                            mBackupList.notify();
                        }
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        /**
         * stop backup
         */
        public void stopBackupThread() {
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "====Stop Backup====");
            isRunning = false;
            if (backupPhotoThread != null) {
                backupPhotoThread.stopBackupPhoto();
                backupPhotoThread = null;
            }

            interrupt();
        }

        public int getBackupListSize() {
            if (mBackupList == null) {
                return 0;
            }
            return this.mBackupList.size();
        }
    }

}
