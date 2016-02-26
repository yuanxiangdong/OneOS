package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.logger.LogLevel;
import com.eli.oneos.model.logger.Logger;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

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

    public OneOSLoginAPI(String ip, String port, String user, String pwd, String mac) {
        super(ip, port);
        this.user = user;
        this.pwd = pwd;
        this.mac = mac;
    }

    public void setOnLoginListener(OnLoginListener listener) {
        this.listener = listener;
    }

    public void login() {
        url = genOneOSAPIUrl(OneOSAPIs.LOGIN);
        Log.d(TAG, "Login: " + url);
        AjaxParams params = new AjaxParams();
        params.put("username", user);
        params.put("password", pwd);

        finalHttp.post(url, params, new AjaxCallBack<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            // {"username":"admin","uid":1001,"admin":1,"gid":0,"result":true,"session":"c5i6qqbe78oj0c1h78o0===="}
                            int uid = json.getInt("uid");
                            int gid = json.getInt("gid");
                            int admin = json.getInt("admin");
                            String session = json.getString("session");
                            long time = System.currentTimeMillis();
                            boolean isLAN = (!EmptyUtils.isEmpty(mac)) ? true : false;

                            if (!EmptyUtils.isEmpty(mac)) {
                                mac = "";
                            }

                            long id; // user id
                            UserSettings userSettings;
                            UserInfo userInfo = UserInfoKeeper.getUserInfo(user, mac);
                            if (null == userInfo) {
                                userInfo = new UserInfo(null, user, mac, pwd, admin, uid, gid, time, true);
                                id = UserInfoKeeper.insert(userInfo);
                                if (id == -1) {
                                    Logger.p(LogLevel.ERROR, true, TAG, "Insert UserInfo Error: " + id);
                                    new Throwable(new Exception("Insert UserInfo Error"));
                                    return;
                                } else {
                                    userSettings = UserSettingsKeeper.insertDefault(id);
                                }
                            } else {
                                userInfo.setPwd(pwd);
                                userInfo.setAdmin(admin);
                                userInfo.setUid(uid);
                                userInfo.setGid(gid);
                                userInfo.setTime(time);
                                userInfo.setIsActive(true);
                                UserInfoKeeper.update(userInfo);

                                id = userInfo.getId();
                                userSettings = UserSettingsKeeper.getSettings(id);
                            }

                            DeviceInfo deviceInfo = new DeviceInfo(mac, ip, port, false, time);
                            if (!isLAN) {
                                DeviceInfoKeeper.insertOrReplace(deviceInfo);
                            }

                            LoginSession loginSession = new LoginSession(userInfo, deviceInfo, userSettings, session, time);

                            listener.onSuccess(url, loginSession);
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
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    public interface OnLoginListener {
        void onStart(String url);

        void onSuccess(String url, LoginSession loginSession);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
