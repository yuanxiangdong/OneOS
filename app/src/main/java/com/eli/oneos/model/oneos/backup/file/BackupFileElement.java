package com.eli.oneos.model.oneos.backup.file;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;

import java.io.File;

public class BackupFileElement extends UploadElement {
    private BackupFile backupInfo;

//    public BackupFileElement(BackupFile backupInfo, File file, String uploadPath, boolean overwrite) {
//        super(file, uploadPath, overwrite);
//        this.backupInfo = backupInfo;
//    }

    public BackupFileElement(BackupFile info, File file, boolean check) {
        this.backupInfo = info;
        setFile(file);
        setCheck(check);

        boolean isBackupAlbum = info.getType() == BackupType.ALBUM;  // 相册备份
        File backupDir = new File(info.getPath());
        // 设备备份保存根目录
        if (isBackupAlbum) {
            String cameraDate = FileUtils.getPhotoDate(file);
            // 相对路径
            String relativeDir = file.getParent().replaceFirst(backupDir.getAbsolutePath(), "");
            // 相册路径： /来自：MI4/Album/RelativeDir/2015-09/xxx.png
            String serverPath = Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM + relativeDir + File.separator + cameraDate + File.separator;
            setTargetPath(serverPath);
        } else {
            ToastHelper.showToast("TODO.. Backup All Files");
            // TODO.. Backup All Files
        }
    }

    public BackupFile getBackupInfo() {
        return backupInfo;
    }

    public void setBackupInfo(BackupFile backupInfo) {
        this.backupInfo = backupInfo;
    }
}