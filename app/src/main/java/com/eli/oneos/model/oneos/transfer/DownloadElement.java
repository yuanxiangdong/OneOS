package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.oneos.OneOSFile;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class DownloadElement extends TransferElement {

    private OneOSFile file;
    // needs check phone space
    private boolean check = false;
    private String tmpName = null;

    public DownloadElement(OneOSFile file, String downloadPath) {
        this(file, downloadPath, 0);
    }

    public DownloadElement(OneOSFile file, String downloadPath, long offset) {
        this.file = file;
        this.targetPath = downloadPath;
        this.offset = offset;
        this.tmpName = file.getName() + "." + System.currentTimeMillis() + ".tmp";
    }

    /**
     * Whether is download file
     *
     * @return true or false
     */
    @Override
    protected boolean isDownload() {
        return true;
    }

    /**
     * Get transmission source file path
     */
    @Override
    public String getSrcPath() {
        return file.getPath();
    }

    /**
     * Get transmission source file name
     */
    @Override
    public String getSrcName() {
        return file.getName();
    }

    /**
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return file.getSize();
    }

    public File getDownloadFile() {
        return new File(targetPath + File.separator + getSrcName());
    }

    /**
     * downloading file temporary name
     *
     * @return
     */
    public String getTmpName() {
        return tmpName;
    }

    // ===============getter and setter method======================
    public OneOSFile getFile() {
        return file;
    }

    public void setFile(OneOSFile file) {
        this.file = file;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    // ===============getter and setter method======================
}
