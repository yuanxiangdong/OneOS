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
    private OnBackupFileListener listener;
    private OnBackupFileListener callback = new OnBackupFileListener() {
        @Override
        public void onBackup(BackupFile backupFile, File file) {
            if (null != listener) {
                listener.onBackup(backupFile, file);
            }
        }

        @Override
        public void onStop(BackupFile backupFile) {
            if (null != listener) {
                listener.onStop(backupFile);
            }
        }
    };

    public BackupFileManager(LoginSession mLoginSession, Context context) {
        this.mLoginSession = mLoginSession;
        this.context = context;

        List<BackupFile> backupDirList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.FILE);

        if (null != backupDirList) {
            for (BackupFile file : backupDirList) {
                BackupFileThread thread = new BackupFileThread(file, callback);
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
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            if (thread.isAlive()) {
                thread.stopBackup();
            }
            iterator.remove();
        }
    }

    public boolean addBackupFile(BackupFile file) {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.getBackupFile() == file || thread.getBackupFile().getId() == file.getId()) {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Add Item is exist: " + file.getPath());
                return false;
            }
        }

        BackupFileThread thread = new BackupFileThread(file, callback);
        mBackupThreadList.add(thread);
        thread.start();
        return true;
    }

    public boolean deleteBackupFile(BackupFile file) {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            if (thread.getBackupFile() == file || thread.getBackupFile().getId() == file.getId()) {
                if (thread.isAlive()) {
                    thread.stopBackup();
                }
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public boolean isBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.isBackup()) {
                return true;
            }
        }

        return false;
    }

    public void setOnBackupFileListener(OnBackupFileListener listener) {
        this.listener = listener;
    }

    private class BackupFileThread extends Thread {
        private final String TAG = BackupFileThread.class.getSimpleName();
        private BackupFile backupFile;
        // 扫描备份失败的文件和新增加的文件列表
        private List<BackupElement> mAdditionalList = Collections.synchronizedList(new ArrayList<BackupElement>());
        private OneOSUploadFileAPI uploadFileAPI;
        private boolean hasBackupTask = false;
        private OnBackupFileListener listener;
        private RecursiveFileObserver mFileObserver;
        private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {
            @Override
            public void onAdd(BackupFile backupInfo, File file) {
                BackupElement element = new BackupElement(backupInfo, file, true);
                notifyAddNewBackupItem(element);
            }
        };

        public BackupFileThread(BackupFile file, OnBackupFileListener listener) {
            if (null == file) {
                new Throwable(new NullPointerException("BackupFile can not be null"));
            }
            this.backupFile = file;
            this.listener = listener;
        }

        private boolean doUploadFile(BackupElement element) {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload File: " + element.getSrcPath());
            if (null != this.listener) {
                this.listener.onBackup(element.getBackupInfo(), element.getFile());
            }
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
                    BackupElement element = new BackupElement(backupFile, dir, true);
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
                    Iterator<BackupElement> iterator = mAdditionalList.iterator();
                    while (!isInterrupted() && iterator.hasNext()) {
                        if (!doUploadFile(iterator.next())) {
                            iterator.remove();
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Remove Additional Element");
                        }
                    }
                }
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload AdditionalList files complete");
                hasBackupTask = false;
                backupFile.setTime(System.currentTimeMillis());
                BackupFileKeeper.update(backupFile);
                if (null != listener) {
                    listener.onStop(backupFile);
                }

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

        public synchronized boolean notifyAddNewBackupItem(BackupElement mElement) {
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
            if (null != mFileObserver) {
                mFileObserver.stopWatching();
            }
            if (uploadFileAPI != null) {
                uploadFileAPI.stopUpload();
                uploadFileAPI = null;
            }
            backupFile.setTime(System.currentTimeMillis());
            BackupFileKeeper.update(backupFile);
        }

        public BackupFile getBackupFile() {
            if (!isInterrupted()) {
                return backupFile;
            }

            return null;
        }

        public boolean isBackup() {
            return hasBackupTask;
        }
    }

    public interface OnBackupFileListener {
        void onBackup(BackupFile backupFile, File file);

        void onStop(BackupFile backupFile);
    }
}
