package com.eli.oneos.model.oneos.backup.file;

import com.eli.oneos.db.greendao.BackupFileInfo;
import com.eli.oneos.model.logger.LogLevel;
import com.eli.oneos.model.logger.Logged;
import com.eli.oneos.model.logger.Logger;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.utils.EmptyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class BackupScanFileThread extends Thread {
    private static final String TAG = BackupScanFileThread.class.getSimpleName();
    private static final int SCAN_FREQUENCY = 1000 * 60 * 60;

    private List<BackupFileInfo> mBackupList;
    private OnScanFileListener mListener;
    private boolean isInterrupt = false;

    public BackupScanFileThread(List<BackupFileInfo> mBackupList, OnScanFileListener mScanListener) {
        this.mBackupList = mBackupList;
        this.mListener = mScanListener;
        if (EmptyUtils.isEmpty(mBackupList)) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "BackupFileInfo List is Empty");
            isInterrupt = true;
        }
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Backup List Size: " + mBackupList.size());
    }

    private ArrayList<BackupFileElement> scanningBackupFiles(BackupFileInfo info) {
        final long lastBackupTime = info.getTime();
        boolean isFirstBackup = (lastBackupTime <= 0) ? true : false;
        boolean isBackupAlbum = info.getType().equalsIgnoreCase(BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        ArrayList<File> files = new ArrayList<>();
        // 遍历备份目录文件
        listFiles(files, backupDir, isBackupAlbum, lastBackupTime);
        ArrayList<BackupFileElement> backupElements = new ArrayList<>();
        if (null != files) {
            for (File file : files) {
                BackupFileElement element = new BackupFileElement(info, file, !isFirstBackup);
                backupElements.add(element);
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "Add Backup Element: " + element.toString());
            }
        }

        Collections.sort(backupElements, new Comparator<BackupFileElement>() {
            @Override
            public int compare(BackupFileElement elem1, BackupFileElement elem2) {
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
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "======Start Sort Backup Task=====");
            Collections.sort(mBackupList, new Comparator<BackupFileInfo>() {
                @Override
                public int compare(BackupFileInfo info1, BackupFileInfo info2) {
                    // priority 1 is max
                    if (info1.getPriority() < info2.getPriority()) {
                        return 1;
                    } else if (info1.getPriority() > info2.getPriority()) {
                        return -1;
                    }
                    return 0;
                }
            });
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "======Complete Sort Backup Task=====");

            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, ">>>>>>Start Scanning Directory=====");
            ArrayList<BackupFileElement> backupElements = new ArrayList<>();
            for (BackupFileInfo info : mBackupList) {
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "------Scanning: " + info.getPath());
                ArrayList<BackupFileElement> files = scanningBackupFiles(info);
                backupElements.addAll(files);
                info.setCount(info.getCount() + 1);
            }
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, ">>>>>>Complete Scanning Directory: " + backupElements.size());

            if (mListener != null) {
                mListener.onComplete(backupElements);
            }

            if (BackupFileManager.USE_FILE_OBSERVER) {
                stopBackupThread();
                break;
            }

            try {
                Logger.p(LogLevel.INFO, Logged.BACKUP_FILE, TAG, "======Sleep 10min====");
                sleep(SCAN_FREQUENCY);
            } catch (InterruptedException e) {
                Logger.p(LogLevel.ERROR, Logged.BACKUP_FILE, TAG, "BackupScanFileThread Exception", e);
            }
        }
    }

    private void listFiles(ArrayList<File> list, File dir, boolean isBackupAlbum, long lastBackupTime) {
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_FILE, TAG, "######List: " + dir.getAbsolutePath());
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
        void onComplete(ArrayList<BackupFileElement> backupList);
    }
}
