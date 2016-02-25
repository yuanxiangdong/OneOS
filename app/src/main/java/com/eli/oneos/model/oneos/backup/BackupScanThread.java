package com.eli.oneos.model.oneos.backup;

import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.model.logger.LogLevel;
import com.eli.oneos.model.logger.Logged;
import com.eli.oneos.model.logger.Logger;
import com.eli.oneos.utils.EmptyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class BackupScanThread extends Thread {
    private static final String TAG = BackupScanThread.class.getSimpleName();
    private static final int SCAN_FREQUENCY = 1000 * 60 * 60;

    private List<BackupInfo> mBackupList;
    private OnScanFileListener mListener;
    private boolean isInterrupt = false;

    public BackupScanThread(List<BackupInfo> mBackupList, OnScanFileListener mScanListener) {
        this.mBackupList = mBackupList;
        this.mListener = mScanListener;
        if (EmptyUtils.isEmpty(mBackupList)) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP, TAG, "BackupInfo List is Empty");
            isInterrupt = true;
        }
        Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "Backup List Size: " + mBackupList.size());
    }

    private ArrayList<BackupElement> scanningBackupFiles(BackupInfo info) {
        final long lastBackupTime = info.getTime();
        boolean isFirstBackup = (lastBackupTime <= 0) ? true : false;
        boolean isBackupAlbum = info.getType().equalsIgnoreCase(BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        ArrayList<File> files = new ArrayList<>();
        // 遍历备份目录文件
        listFiles(files, backupDir, isBackupAlbum, lastBackupTime);
        ArrayList<BackupElement> backupElements = new ArrayList<>();
        if (null != files) {
            for (File file : files) {
                BackupElement element = new BackupElement(info, file, !isFirstBackup);
                backupElements.add(element);
                Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "Add Backup Element: " + element.toString());
            }
        }

        Collections.sort(backupElements, new Comparator<BackupElement>() {
            @Override
            public int compare(BackupElement elem1, BackupElement elem2) {
                if (elem1.getFile().lastModified() > elem2.getFile().lastModified()) {
                    return 1;
                } else if (elem1.getFile().lastModified() < elem2.getFile().lastModified()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        return backupElements;
    }

    @Override
    public void run() {
        while (!isInterrupt) {
            Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "======Start Sort Backup Task=====");
            Collections.sort(mBackupList, new Comparator<BackupInfo>() {
                @Override
                public int compare(BackupInfo info1, BackupInfo info2) {
                    // priority 1 is max
                    if (info1.getPriority() < info2.getPriority()) {
                        return 1;
                    } else if (info1.getPriority() > info2.getPriority()) {
                        return -1;
                    }
                    return 0;
                }
            });
            Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "======Complete Sort Backup Task=====");

            Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, ">>>>>>Start Scanning Directory=====");
            ArrayList<BackupElement> backupElements = new ArrayList<>();
            for (BackupInfo info : mBackupList) {
                Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "------Scanning: " + info.getPath());
                ArrayList<BackupElement> files = scanningBackupFiles(info);
                backupElements.addAll(files);
            }
            Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, ">>>>>>Complete Scanning Directory: " + backupElements.size());

            if (mListener != null) {
                mListener.onComplete(backupElements);
            }

            if (BackupManager.USE_FILE_OBSERVER) {
                stopBackupThread();
                break;
            }

            try {
                Logger.p(LogLevel.INFO, Logged.BACKUP, TAG, "======Sleep 10min====");
                sleep(SCAN_FREQUENCY);
            } catch (InterruptedException e) {
                Logger.p(LogLevel.ERROR, Logged.BACKUP, TAG, "BackupScanThread Exception", e);
            }
        }
    }

    private void listFiles(ArrayList<File> list, File dir, boolean isBackupAlbum, long lastBackupTime) {
        Logger.p(LogLevel.DEBUG, Logged.BACKUP, TAG, "######List: " + dir.getAbsolutePath());
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(new BackupFileFilter(isBackupAlbum, lastBackupTime));
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        listFiles(list, file, isBackupAlbum, lastBackupTime);
                    } else {
                        list.add(file);
                    }
                }
            }
        } else {
            list.add(dir);
        }
    }

    public void stopBackupThread() {
        this.isInterrupt = true;
        interrupt();
    }

    public interface OnScanFileListener {
        void onComplete(ArrayList<BackupElement> backupList);
    }
}
