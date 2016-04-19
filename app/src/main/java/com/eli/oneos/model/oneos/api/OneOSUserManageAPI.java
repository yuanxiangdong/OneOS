package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSUserManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSUserManageAPI.class.getSimpleName();

    private OnUserManageListener listener;
    private String username = null;
    private String cmd = null;

    public OneOSUserManageAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnUserManageListener(OnUserManageListener listener) {
        this.listener = listener;
    }

    private void manage(Map<String, String> params) {
        if (null == params) {
            params = new HashMap<>();
        }
        url = genOneOSAPIUrl(OneOSAPIs.USER_MANAGE);
        params.put("session", session);
        params.put("cmd", cmd);
        params.put("username", username);

        httpUtils.post(url, params, new OnHttpListener<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
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
                            listener.onSuccess(url, cmd);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
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

    public void add(String username, String password) {
        this.cmd = "add";
        this.username = username;

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        manage(params);
    }

    public void delete(String username) {
        this.cmd = "delete";
        this.username = username;

        manage(null);
    }

    public void chpwd(String username, String password) {
        this.cmd = "chpwd";
        this.username = username;

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        manage(params);
    }

    public void chspace(String username, long space) {
        this.cmd = "space";
        this.username = username;

        Map<String, String> params = new HashMap<>();
        params.put("space", String.valueOf(space));
        manage(params);
    }

    public interface OnUserManageListener {
        void onStart(String url);

        void onSuccess(String url, String cmd);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
