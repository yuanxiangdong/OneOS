package com.eli.oneos.utils;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Utils {
    public static final int DB_VERSION = 1;

    /**
     * check if port avaliable
     *
     * @param port
     * @return result
     */
    public static boolean checkPort(String port) {
        if (EmptyUtils.isEmpty(port)) {
            return false;
        }

        int i = -1;
        try {
            i = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = -1;
        }

        return i >= 0 && i <= 65535;
    }
}
