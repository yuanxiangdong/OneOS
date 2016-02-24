package com.eli.oneos.model.oneos.backup;

import android.os.Build;

import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;

import java.io.File;

public class BackupElement extends UploadElement {
    private static final String ROOT_DIR_NAME_ALBUM = "/来自：" + Build.BRAND + "-" + Build.MODEL;
    private BackupInfo backupInfo;

//    public BackupElement(BackupInfo backupInfo, File file, String uploadPath, boolean overwrite) {
//        super(file, uploadPath, overwrite);
//        this.backupInfo = backupInfo;
//    }

    public BackupElement(BackupInfo info, File file, boolean check) {
        this.backupInfo = info;
        setFile(file);
        setCheck(check);

        boolean isBackupAlbum = info.getType().equalsIgnoreCase(BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        // 设备备份保存根目录
        if (isBackupAlbum) {
            String cameraDate = FileUtils.getPhotoDate(file);
            // 相对路径
            String relativeDir = file.getParent().replaceFirst(backupDir.getAbsolutePath(), "");
            // 相册路径： /来自：MI4/Album/RelativeDir/2015-09/xxx.png
            String serverPath = ROOT_DIR_NAME_ALBUM + File.separator + "Album" + File.separator + relativeDir + File.separator + cameraDate + File.separator;
            setTargetPath(serverPath);
        } else {
            ToastHelper.showToast("TODO.. Backup All Files");
            // TODO.. Backup All Files
        }
    }

    public BackupInfo getBackupInfo() {
        return backupInfo;
    }

    public void setBackupInfo(BackupInfo backupInfo) {
        this.backupInfo = backupInfo;
    }
}