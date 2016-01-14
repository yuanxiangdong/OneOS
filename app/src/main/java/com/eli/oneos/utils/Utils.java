package com.eli.oneos.utils;

import com.eli.oneos.MyApplication;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Utils {


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipToPx(float dpValue) {
        final float scale = MyApplication.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * check if ip is valid
     */
    public static boolean isAvaliableIp(String IP) {
        boolean b = false;
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }

        return b;
    }

    /**
     * check if port is valid
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
