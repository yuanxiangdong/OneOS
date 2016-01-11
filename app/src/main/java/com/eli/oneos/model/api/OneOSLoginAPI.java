package com.eli.oneos.model.api;

import android.util.Log;

import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceHistoryKeeper;
import com.eli.oneos.db.UserHistoryKeeper;
import com.eli.oneos.db.greendao.DeviceHistory;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserHistory;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.user.LoginSession;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Login API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSLoginAPI extends OneOSAPI {
    private static final String TAG = OneOSLoginAPI.class.getSimpleName();

    private OnLoginListener listener;
    private String user = null;
    private String pwd = null;
    private String mac = null;

    public OneOSLoginAPI(String ip, String port, String user, String pwd, String mac) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
        this.mac = mac;
        initHttp();
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
                            boolean isLAN = (mac != null) ? true : false;

                            UserHistory userHistory = new UserHistory(user, pwd, mac, time);
                            UserHistoryKeeper.insertOrReplace(userHistory);
                            if (!isLAN) {
                                DeviceHistory deviceHistory = new DeviceHistory(ip, mac, port, time, false);
                                DeviceHistoryKeeper.insertOrReplace(deviceHistory);
                            }

                            UserInfo userInfo = new UserInfo(user, pwd, time, uid, gid, admin);
                            DeviceInfo deviceInfo = new DeviceInfo(mac, ip, time, port, "", "", isLAN);
                            LoginSession loginInfo = new LoginSession(userInfo, deviceInfo, session, time);

                            listener.onSuccess(url, loginInfo);
                        } else {
                            // {"errno":-1,"msg":"login error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.getString("msg");
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, "JSONException");
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
