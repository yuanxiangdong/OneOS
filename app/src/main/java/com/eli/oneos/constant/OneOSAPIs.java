package com.eli.oneos.constant;

import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginSession;

/**
 * OneSpace OS 3.x API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class OneOSAPIs {
    public static final int OneOS_UPLOAD_SOCKET_PORT = 7777;

    public static final String ONE_OS_PRIVATE_ROOT_DIR = "/";
    public static final String ONE_OS_PUBLIC_ROOT_DIR = "public/";
    public static final String ONE_OS_RECYCLE_ROOT_DIR = "/.recycle/";

    public static final String ONE_API_DEFAULT_PORT = "80";
    public static final String PREFIX_HTTP = "http://";
    private static final String ONE_API = "/oneapi";

    public static final String LOGIN = ONE_API + "/user/login";
    public static final String USER_MANAGE = ONE_API + "/user/manage";

    public static final String NET_GET_MAC = ONE_API + "/net/infowire";

    public static final String FILE_LIST = ONE_API + "/file/list";
    public static final String FILE_SEARCH = ONE_API + "/file/search";
    public static final String FILE_LIST_DB = ONE_API + "/file/listdb";
    public static final String FILE_MANAGE = ONE_API + "/file/manage";
    public static final String FILE_DOWNLOAD = ONE_API + "/file/download";
    public static final String FILE_THUMBNAIL = ONE_API + "/file/thumbnail";

    public static final String SYSTEM_REBOOT = ONE_API + "/sys/reboot";
    public static final String SYSTEM_HALT = ONE_API + "/sys/halt";
    public static final String SYSTEM_HD_SMART = ONE_API + "/sys/hdsmart";

//    public static String genOpenUrl(LoginSession loginSession, OneOSFile file) {
//        String srcPath = android.net.Uri.encode(file.getRealPath(loginSession.getUserInfo().getTargetPath()));
//        return loginSession.getBaseUrl() + "/" + path + "&session=" + loginSession.getSession();
//    }

    public static String genDownloadUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/download?path=home%2Fadmin%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        String path = android.net.Uri.encode(file.getRealPath(loginSession.getUserInfo().getName()));
        return loginSession.getBaseUrl() + OneOSAPIs.FILE_DOWNLOAD + "?path=" + path + "&session=" + loginSession.getSession();
    }

    public static String genThumbnailUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        String path = android.net.Uri.encode(file.getPath());
        return loginSession.getBaseUrl() + OneOSAPIs.FILE_THUMBNAIL + "?path=" + path + "&session=" + loginSession.getSession();
    }
}
