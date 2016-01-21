package com.eli.oneos.model.user;

/**
 * Singleton Class for manage list information.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/11.
 */
public class LoginManage {
    private static LoginSession loginSession = null;
    private static LoginManage INSTANCE = new LoginManage();

    private LoginManage() {
    }

    /**
     * Get Singleton Class instance.
     *
     * @return LoginManage Instance
     */
    public static LoginManage getInstance() {
        return LoginManage.INSTANCE;
    }

    /**
     * Whether is list OneSpace
     *
     * @return if list
     */
    public boolean isLogin() {
        if (loginSession == null) {
            return false;
        }

        if (loginSession.getUserInfo() == null || loginSession.getDeviceInfo() == null) {
            return false;
        }

        if (loginSession.getSession() == null) {
            return false;
        }

        return true;
    }

    /**
     * Logout OneSpace
     *
     * @return logout result
     */
    public boolean logout() {
        if (isLogin()) {
            loginSession.setSession(null);
            return true;
        }

        return false;
    }

    public boolean isLANDevice() {
        if (isLogin()) {
            return loginSession.getDeviceInfo().getIsLAN();
        }

        return false;
    }

    public void setLoginSession(LoginSession loginSession) {
        LoginManage.loginSession = loginSession;
    }

    public LoginSession getLoginSession() {
        return LoginManage.loginSession;
    }
}
