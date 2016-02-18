package com.eli.oneos.model.oneos.trans;

import com.eli.oneos.model.oneos.OneOSFile;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class DownloadElement extends TransferElement {

    private OneOSFile file;

    public DownloadElement(OneOSFile file, String downloadPath) {
        this(file, downloadPath, 0);
    }

    public DownloadElement(OneOSFile file, String downloadPath, long offset) {
        this.file = file;
        this.targetPath = downloadPath;
        this.offset = offset;
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
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return file.getSize();
    }

    // ===============getter and setter method======================
    public OneOSFile getFile() {
        return file;
    }

    public void setFile(OneOSFile file) {
        this.file = file;
    }
    // ===============getter and setter method======================
}
