package com.eli.oneos.constant;

/**
 * OneSpace OS 3.x API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class OneOSAPIs {
    public static final String ONE_API_DEFAULT_PORT = "80";
    private static final String PREFIX_HTTP = "http://";
    private static final String ONE_API = "/oneapi";

    public static final String LOGIN = ONE_API + "/user/login";





    public static String genOneOSAPIUrl(String ip, String port, String action) {
        return PREFIX_HTTP + ip + ":" + port + action;
    }
}
