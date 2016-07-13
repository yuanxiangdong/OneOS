package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.model.upgrade.OneOSVersionManager;
import com.eli.oneos.utils.EmptyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Login API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSLoginAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSLoginAPI.class.getSimpleName();

    private OnLoginListener listener;
    private String user = null;
    private String pwd = null;
    private String mac = null;
    private int domain = Constants.DOMAIN_DEVICE_LAN;
    private OnHttpListener httpListener = new OnHttpListener<String>() {

        @Override
        public void onFailure(Throwable th, int errorNo, String strMsg) {
            // super.onFailure(th, errorNo, strMsg);
            errorNo = parseFailure(th, errorNo);
            if (listener != null) {
                if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                    strMsg = context.getResources().getString(R.string.oneos_version_mismatch);
                }
                listener.onFailure(url, errorNo, strMsg);
            }
        }

        @Override
        public void onSuccess(String result) {
            // super.onSuccess(result);
            Log.d(TAG, "Response Data:" + result);
            if (listener != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        // {"username":"admin","uid":1001,"admin":1,"gid":0,"result":true,"session":"c5i6qqbe78oj0c1h78o0===="}
                        final int uid = json.getInt("uid");
                        final int gid = json.getInt("gid");
                        final int admin = json.getInt("admin");
                        final String session = json.getString("session");
                        final long time = System.currentTimeMillis();

                        if (!EmptyUtils.isEmpty(mac)) {
                            genLoginSession(mac, uid, gid, admin, session, time, domain);
                        } else {
                            // get device mac address
                            OneOSGetMacAPI getMacAPI = new OneOSGetMacAPI(ip, port, domain != Constants.DOMAIN_DEVICE_SSUDP);
                            getMacAPI.setOnGetMacListener(new OneOSGetMacAPI.OnGetMacListener() {
                                @Override
                                public void onStart(String url) {
                                }

                                @Override
                                public void onSuccess(String url, String mac) {
                                    genLoginSession(mac, uid, gid, admin, session, time, domain);
                                }

                                @Override
                                public void onFailure(String url, int errorNo, String errorMsg) {
                                    String msg = context.getResources().getString(R.string.error_get_device_mac);
                                    listener.onFailure(url, errorNo, msg);
                                }
                            });
                            getMacAPI.getMac();
                        }
                    } else {
                        // {"errno":-1,"msg":"list error","result":false}
                        int errorNo = json.getInt("errno");
                        String msg = context.getResources().getString(R.string.error_login_user_or_pwd);
                        listener.onFailure(url, errorNo, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                }
            }
        }
    };

    public OneOSLoginAPI(String ip, String port, String user, String pwd, String mac) {
        super(ip, port);
        this.user = user;
        this.pwd = pwd;
        this.mac = mac;
    }

    public void setOnLoginListener(OnLoginListener listener) {
        this.listener = listener;
    }

    public void login(int domain) {
        this.domain = domain;
        url = genOneOSAPIUrl(OneOSAPIs.LOGIN);
        Log.d(TAG, "Login: " + url);
        Map<String, String> params = new HashMap<>();
        params.put("username", user);
        params.put("password", pwd);

        if (domain == Constants.DOMAIN_DEVICE_SSUDP) {
            httpUtils.sendSSUDP(url, params, httpListener);
        } else {
            httpUtils.post(url, params, httpListener);
        }

        if (listener != null) {
            listener.onStart(url);
        }
    }

    private void checkOneOSVersion(final LoginSession loginSession) {
        OneOSVersionAPI versionAPI = new OneOSVersionAPI(loginSession.getIp(), loginSession.getPort(), domain != Constants.DOMAIN_DEVICE_SSUDP);
        versionAPI.setOnSystemVersionListener(new OneOSVersionAPI.OnSystemVersionListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, OneOSInfo info) {
                loginSession.setOneOSInfo(info);
                if (OneOSVersionManager.check(info.getVersion())) {
                    listener.onSuccess(url, loginSession);
                } else {
                    String msg = String.format(context.getResources().getString(R.string.fmt_oneos_version_upgrade), OneOSVersionManager.MIN_ONEOS_VERSION);
                    listener.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                String msg = context.getResources().getString(R.string.oneos_version_check_failed);
                listener.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg);
            }
        });
        versionAPI.query();
    }

    private void genLoginSession(String mac, int uid, int gid, int admin, String session, long time, int domain) {
        long id; // user id
        UserSettings userSettings;
        UserInfo userInfo = UserInfoKeeper.getUserInfo(user, mac);
        if (null == userInfo) {
            userInfo = new UserInfo(null, user, mac, pwd, admin, uid, gid, domain, time, false, true);
            id = UserInfoKeeper.insert(userInfo);
//            if (id == -1) {
//                Logger.p(LogLevel.ERROR, true, TAG, "Insert UserInfo Error: " + id);
//                new Throwable(new Exception("Insert UserInfo Error"));
//                return;
//            } else {
            userSettings = UserSettingsKeeper.insertDefault(id, user);
//            }
        } else {
            userInfo.setPwd(pwd);
            userInfo.setAdmin(admin);
            userInfo.setUid(uid);
            userInfo.setGid(gid);
            userInfo.setTime(time);
            userInfo.setDomain(domain);
            userInfo.setIsLogout(false);
            userInfo.setIsActive(true);
            UserInfoKeeper.update(userInfo);

            id = userInfo.getId();
            userSettings = UserSettingsKeeper.getSettings(id);
        }
        Logger.p(LogLevel.ERROR, true, TAG, "Login User ID: " + id);

        boolean isNewDevice = false;
        DeviceInfo deviceInfo = DeviceInfoKeeper.query(mac);
        if (null == deviceInfo) {
            deviceInfo = new DeviceInfo(mac, null, null, null, null, null, null, null, domain, time);
            isNewDevice = true;
        }
        deviceInfo.setMac(mac);
        deviceInfo.setTime(time);
        deviceInfo.setDomain(domain);
        if (domain == Constants.DOMAIN_DEVICE_LAN) {
            deviceInfo.setLanIp(ip);
            deviceInfo.setLanPort(port);
        } else if (domain == Constants.DOMAIN_DEVICE_WAN) {
            deviceInfo.setWanIp(ip);
            deviceInfo.setWanPort(port);
        }
        if (isNewDevice) {
            DeviceInfoKeeper.insert(deviceInfo);
        } else {
            DeviceInfoKeeper.update(deviceInfo);
        }

        LoginSession loginSession = new LoginSession(userInfo, deviceInfo, userSettings, session, isNewDevice, time);

        checkOneOSVersion(loginSession);
    }

    public interface OnLoginListener {
        void onStart(String url);

        void onSuccess(String url, LoginSession loginSession);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
