package com.eli.oneos.model.oneos.user;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;

/**
 * User Login information
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class LoginSession {

    private UserInfo userInfo = null;
    private UserSettings userSettings = null;
    private DeviceInfo deviceInfo = null;
    private String session = null;
    private long time = 0;

    public LoginSession(UserInfo userInfo, DeviceInfo deviceInfo, UserSettings userSettings, String session, long time) {
        this.userInfo = userInfo;
        this.deviceInfo = deviceInfo;
        this.userSettings = userSettings;
        this.session = session;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * base url
     *
     * @return such as http://192.168.1.17:80
     */
    public String getBaseUrl() {
        if (null != deviceInfo) {
            return OneOSAPIs.PREFIX_HTTP + deviceInfo.getIp() + ":" + deviceInfo.getPort();
        }

        return null;
    }

    public boolean isAdmin() {
        if (userInfo.getAdmin() == 1) {
            return true;
        }

        return false;
    }

    public String getDownloadPath() {
        return userSettings.getDownloadPath();
    }

    public boolean isLANDevice() {
        return deviceInfo.getIsLAN();
    }
}
