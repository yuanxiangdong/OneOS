package com.eli.oneos.model.oneos;

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

    public static String getTypeName(OneOSFileType type) {
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
}
