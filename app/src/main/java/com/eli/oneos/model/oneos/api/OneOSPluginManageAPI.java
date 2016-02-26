package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Manage Plugins API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSPluginManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSPluginManageAPI.class.getSimpleName();

    private OnManagePluginListener listener;

    public OneOSPluginManageAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnManagePluginListener(OnManagePluginListener listener) {
        this.listener = listener;
    }

    public void on(String pack) {
        doManage(pack, "on");
    }

    public void off(String pack) {
        doManage(pack, "off");
    }

    public void delete(String pack) {
        doManage(pack, "unActive");
    }

    private void doManage(final String pack, final String cmd) {
        url = genOneOSAPIUrl(OneOSAPIs.APP_MANAGE);
        AjaxParams params = new AjaxParams();
        params.put("pack", pack);
        params.put("cmd", cmd);
        params.put("session", session);
        logHttp(TAG, url, params);
        finalHttp.post(url, params, new AjaxCallBack<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
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
                            listener.onSuccess(url, pack, cmd, true);
                        } else {
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

    public interface OnManagePluginListener {
        void onStart(String url);

        void onSuccess(String url, String pack, String cmd, boolean ret);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
