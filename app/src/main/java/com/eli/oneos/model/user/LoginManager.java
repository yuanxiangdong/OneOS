package com.eli.oneos.model.user;

/**
 * Singleton Class for manage list information.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/11.
 */
public class LoginManager {
    private static LoginSession loginSession = null;
    private static LoginManager INSTANCE = new LoginManager();

    private LoginManager() {
    }

    /**
     * Get Singleton Class instance.
     *
     * @return LoginManager Instance
     */
    public static LoginManager getInstance() {
        return LoginManager.INSTANCE;
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
        LoginManager.loginSession = loginSession;
    }

    public LoginSession getLoginSession() {
        return LoginManager.loginSession;
    }
}
