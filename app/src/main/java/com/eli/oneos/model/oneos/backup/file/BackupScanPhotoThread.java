package com.eli.oneos.model.oneos.backup.file;

import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
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
public class BackupScanPhotoThread extends Thread {
    private static final String TAG = BackupScanPhotoThread.class.getSimpleName();

    private List<BackupFile> mBackupList;
    private OnScanFileListener mListener;
    private boolean isInterrupt = false;

    public BackupScanPhotoThread(List<BackupFile> mBackupList, OnScanFileListener mScanListener) {
        this.mBackupList = mBackupList;
        this.mListener = mScanListener;
        if (EmptyUtils.isEmpty(mBackupList)) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP_ALBUM, TAG, "BackupFile List is Empty");
            isInterrupt = true;
        }
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "Backup List Size: " + mBackupList.size());
    }

    private ArrayList<BackupFileElement> scanningBackupFiles(BackupFile info) {
        final long lastBackupTime = info.getTime();
        boolean isFirstBackup = (lastBackupTime <= 0) ? true : false;
        boolean isBackupAlbum = (info.getType() == BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        ArrayList<File> fileList = new ArrayList<>();
        // 遍历备份目录文件
        listFiles(fileList, backupDir, isBackupAlbum, lastBackupTime);
        ArrayList<BackupFileElement> backupElements = new ArrayList<>();
        if (null != fileList) {
            for (File file : fileList) {
                BackupFileElement element = new BackupFileElement(info, file, isFirstBackup);
                backupElements.add(element);
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "Add Backup Element: " + element.toString());
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
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "======Start Sort Backup Task=====");
            Collections.sort(mBackupList, new Comparator<BackupFile>() {
                @Override
                public int compare(BackupFile info1, BackupFile info2) {
                    // priority 1 is max
                    if (info1.getPriority() < info2.getPriority()) {
                        return 1;
                    } else if (info1.getPriority() > info2.getPriority()) {
                        return -1;
                    }
                    return 0;
                }
            });
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "======Complete Sort Backup Task=====");

            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, ">>>>>>Start Scanning Directory=====");
            ArrayList<BackupFileElement> backupElements = new ArrayList<>();
            for (BackupFile info : mBackupList) {
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "------Scanning: " + info.getPath());
                ArrayList<BackupFileElement> files = scanningBackupFiles(info);
                backupElements.addAll(files);
                info.setCount(info.getCount() + 1);
            }
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, ">>>>>>Complete Scanning Directory: " + backupElements.size());

            if (mListener != null) {
                mListener.onComplete(backupElements);
            }

            break;
        }
    }

    private void listFiles(ArrayList<File> list, File dir, boolean isBackupAlbum, long lastBackupTime) {
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "######List Dir: " + dir.getAbsolutePath() + ", LastTime: " + lastBackupTime);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(new BackupFileFilter(isBackupAlbum, lastBackupTime));
            if (null != files) {
                for (File file : files) {
                    listFiles(list, file, isBackupAlbum, lastBackupTime);
                }
            }
        } else {
            list.add(dir);
        }
    }

    public void stopScanThread() {
        this.isInterrupt = true;
        interrupt();
    }

    public interface OnScanFileListener {
        void onComplete(ArrayList<BackupFileElement> backupList);
    }
}
