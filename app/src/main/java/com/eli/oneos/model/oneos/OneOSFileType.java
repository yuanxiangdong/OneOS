package com.eli.oneos.model.oneos;

import com.eli.oneos.R;

public enum OneOSFileType {
    /**
     * 文件目录
     */
    PRIVATE,
    /**
     * 公共目录
     */
    PUBLIC,
    /**
     * 回收站
     */
    RECYCLE,
    /**
     * 文档
     */
    DOC,
    /**
     * 图片
     */
    PICTURE,
    /**
     * 视频
     */
    VIDEO,
    /**
     * 音频
     */
    AUDIO;

    public static String getServerTypeName(OneOSFileType type) {
        if (type == DOC) {
            return "doc";
        } else if (type == VIDEO) {
            return "video";
        } else if (type == AUDIO) {
            return "audio";
        } else {
            return "pic";
        }
    }

    public static int getTypeName(OneOSFileType type) {
        int name = R.string.file_type_private;
        if (type == PUBLIC) {
            name = R.string.file_type_public;
        } else if (type == RECYCLE) {
            name = R.string.file_type_cycle;
        } else if (type == DOC) {
            name = R.string.file_type_doc;
        } else if (type == VIDEO) {
            name = R.string.file_type_video;
        } else if (type == AUDIO) {
            name = R.string.file_type_audio;
        } else if (type == PICTURE) {
            name = R.string.file_type_pic;
        }

        return name;
    }
}
