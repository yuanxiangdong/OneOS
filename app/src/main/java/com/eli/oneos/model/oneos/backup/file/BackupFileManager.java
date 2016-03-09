package com.eli.oneos.model.oneos.backup.file;

import android.content.Context;

import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSUploadFileAPI;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.backup.RecursiveFileObserver;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BackupFileManager {
    private static final String TAG = BackupFileManager.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_FILE;

    private Context context;
    private LoginSession mLoginSession = null;
    private List<BackupFileThread> mBackupThreadList = new ArrayList<>();

    public BackupFileManager(LoginSession mLoginSession, Context context) {
        this.mLoginSession = mLoginSession;
        this.context = context;

        List<BackupFile> backupDirList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.FILE);

//        File mInternalDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        BackupFile info = new BackupFile(null, mLoginSession.getUserInfo().getId(), mInternalDCIMDir.getAbsolutePath(),
//                true, BackupType.FILE, BackupPriority.MAX, 0L, 0L);
//        backupDirList.add(info);

        if (null != backupDirList) {
            for (BackupFile file : backupDirList) {
                BackupFileThread thread = new BackupFileThread(file);
                mBackupThreadList.add(thread);
            }
        }
    }

    public void startBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            thread.start();
        }
    }

    public void stopBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.isAlive()) {
                thread.stopBackup();
            }
        }
    }

    public boolean addBackupFile(BackupFile file) {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.getBackupFile().getPath().equals(file.getPath())) {
                return false;
            }
        }

        BackupFileThread thread = new BackupFileThread(file);
        mBackupThreadList.add(thread);
        thread.start();
        return true;
    }

    public boolean removeBackupFile(BackupFile file) {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.getBackupFile().getPath().equals(file.getPath())) {
                if (thread.isAlive()) {
                    thread.stopBackup();
                }

                return true;
            }
        }

        return false;
    }

    private class BackupFileThread extends Thread {
        private final String TAG = BackupFileThread.class.getSimpleName();
        private BackupFile backupFile;
        // 扫描备份失败的文件和新增加的文件列表
        private List<BackupFileElement> mAdditionalList = Collections.synchronizedList(new ArrayList<BackupFileElement>());
        private OneOSUploadFileAPI uploadFileAPI;
        private boolean hasBackupTask = false;
        private RecursiveFileObserver mFileObserver;
        private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {
            @Override
            public void onAdd(BackupFile backupInfo, File file) {
                BackupFileElement element = new BackupFileElement(backupInfo, file, true);
                notifyAddNewBackupItem(element);
            }
        };

        public BackupFileThread(BackupFile file) {
            if (null == file) {
                new Throwable(new NullPointerException("BackupFile can not be null"));
            }
            this.backupFile = file;
        }

        private boolean doUploadFile(BackupFileElement element) {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload File: " + element.getSrcPath());
            uploadFileAPI = new OneOSUploadFileAPI(mLoginSession, element);
            boolean result = uploadFileAPI.upload();
            uploadFileAPI = null;
            if (result) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Backup File Success: " + element.getSrcPath());
                return true;
            } else {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Backup File Failed: " + element.getSrcPath());
                return false;
            }
        }

        private void scanningAndBackupFiles(File dir) {
            if (isInterrupted()) {
                return;
            }

            if (dir.exists()) {
                if (dir.isDirectory()) {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Scanning Dir: " + dir.getPath());
                    File[] files = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return !pathname.isHidden();
                        }
                    });
                    for (File file : files) {
                        scanningAndBackupFiles(file);
                    }
                } else {
                    BackupFileElement element = new BackupFileElement(backupFile, dir, true);
                    if (!doUploadFile(element)) {
                        mAdditionalList.add(element);
                        Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Add to Additional List");
                    }
                }
            }
        }

        @Override
        public void run() {
            backupFile.setCount(backupFile.getCount() + 1);
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start scanning and upload file: " + backupFile.getPath());
            scanningAndBackupFiles(new File(backupFile.getPath()));
            mFileObserver = new RecursiveFileObserver(backupFile, backupFile.getPath(),
                    RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
            mFileObserver.startWatching();
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Scanning and upload file complete");

            while (!isInterrupted()) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start upload AdditionalList files: " + mAdditionalList.size());
                if (!EmptyUtils.isEmpty(mAdditionalList)) {
                    hasBackupTask = true;
                    Iterator<BackupFileElement> iterator = mAdditionalList.iterator();
                    while (!isInterrupted() && iterator.hasNext()) {
                        if (!doUploadFile(iterator.next())) {
                            iterator.remove();
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Remove Additional Element");
                        }
                    }
                }
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload AdditionalList files complete");
                hasBackupTask = false;

                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Waiting for AdditionalList Changed...");
                    synchronized (mAdditionalList) {
                        mAdditionalList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized boolean notifyAddNewBackupItem(BackupFileElement mElement) {
            if (mElement == null) {
                return false;
            }

            synchronized (mAdditionalList) {
                int curSize = mAdditionalList.size();
                if (mAdditionalList.add(mElement)) {
                    if (curSize <= 0) {
                        if (!hasBackupTask) {
                            mAdditionalList.notify();
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
        public void stopBackup() {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "====Stop Backup====");
            interrupt();
            mFileObserver.stopWatching();
            if (uploadFileAPI != null) {
                uploadFileAPI.stopUpload();
                uploadFileAPI = null;
            }
            backupFile.setTime(System.currentTimeMillis());
            BackupFileKeeper.update(backupFile);
        }

        public BackupFile getBackupFile() {
            if (isInterrupted()) {
                return backupFile;
            }

            return null;
        }
    }
}
