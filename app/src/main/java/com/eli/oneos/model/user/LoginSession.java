package com.eli.oneos.model.user;

import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;

/**
 * User Login information
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class LoginSession {

    private long time = 0;
    private UserInfo userInfo = null;
    private DeviceInfo deviceInfo = null;
    private String session = null;

    public LoginSession() {
    }

    public LoginSession(UserInfo userInfo, DeviceInfo deviceInfo, String session, long time) {
        this.userInfo = userInfo;
        this.deviceInfo = deviceInfo;
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
}
