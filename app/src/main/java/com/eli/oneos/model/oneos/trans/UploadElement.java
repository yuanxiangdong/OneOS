package com.eli.oneos.model.oneos.trans;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class UploadElement extends TransferElement {
    private File file;

    public UploadElement(File file, String uploadPath) {
        this.file = file;
        this.targetPath = uploadPath;
    }

    public boolean isUploadToPrivateDir() {
        return targetPath.startsWith("/");
    }

    /**
     * Whether is download file
     *
     * @return true or false
     */
    @Override
    protected boolean isDownload() {
        return false;
    }

    /**
     * Get transmission source file path
     */
    @Override
    public String getSrcPath() {
        return file.getPath();
    }

    /**
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return file.length();
    }

    // ===============getter and setter method======================
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    // ===============getter and setter method======================
}
