package com.eli.oneos.constant;

import android.os.Build;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Constants {
    public static final int DELAY_TIME_AUTO_REFRESH = 300;

    public static final String DEFAULT_DOWNLOAD_PATH = "/OneOS";

    public static final String BACKUP_ONEOS_ROOT_DIR_NAME = "/From：" + Build.BRAND + "-" + Build.MODEL + "/";
    public static final String BACKUP_ONEOS_ROOT_DIR_NAME_ALBUM = BACKUP_ONEOS_ROOT_DIR_NAME + "Album/";

    public static final String BACKUP_ONEOS_ROOT_DIR_CONTACTS = BACKUP_ONEOS_ROOT_DIR_NAME;
    public static final String BACKUP_FILE_NAME_CONTACTS = ".contactsfromandroid.vcf";

}
