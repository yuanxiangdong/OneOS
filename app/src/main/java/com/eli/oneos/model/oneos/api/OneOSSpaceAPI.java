package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSSpaceAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSSpaceAPI.class.getSimpleName();

    private OnSpaceListener listener;
    private String username = null;

    public OneOSSpaceAPI(LoginSession loginSession) {
        super(loginSession);
        username = loginSession.getUserInfo().getName();
    }

    public void setOnSpaceListener(OnSpaceListener listener) {
        this.listener = listener;
    }

    public void query(String username) {
        this.username = username;
        query(false);
    }

    public void query(final boolean isOneOSSpace) {
        AjaxParams params = new AjaxParams();
        if (isOneOSSpace) {
            url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_HD_SMART);
        } else {
            url = genOneOSAPIUrl(OneOSAPIs.USER_MANAGE);
            params.put("session", session);
            params.put("username", username);
            params.put("cmd", "space");
        }
        Log.d(TAG, "Get Space: " + url + ", Params: " + params.toString());

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
                            if (isOneOSSpace) {
                                String spaceStr = json.getString("vfs");
                                String spaceStr2 = null;
                                if (json.has("hds")) {
                                    String hds = json.getString("hds");
                                    if (!EmptyUtils.isEmpty(hds)) {
                                        JSONArray jsonArray = new JSONArray(hds);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject hdsJSON = jsonArray.getJSONObject(i);
                                            if (hdsJSON.has("vfs")) {
                                                String vfs = hdsJSON.getString("vfs");
                                                if (i == 0) {
                                                    spaceStr = vfs;
                                                } else {
                                                    spaceStr2 = vfs;
                                                }
                                            }
                                        }
                                    }
                                }

                                long totalSize = 0, freeSize = 0;
                                if (!EmptyUtils.isEmpty(spaceStr) && !spaceStr.equals("{}")) {
                                    json = new JSONObject(spaceStr);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    totalSize = blocks * frsize;
                                    freeSize = bavail * frsize;
                                }

                                long totalSize2 = -1, freeSize2 = -1;
                                if (!EmptyUtils.isEmpty(spaceStr2) && !spaceStr2.equals("{}")) {
                                    json = new JSONObject(spaceStr2);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    totalSize2 = blocks * frsize;
                                    freeSize2 = bavail * frsize;
                                }
                                listener.onSuccess(url, isOneOSSpace, totalSize, freeSize, totalSize2, freeSize2);
                            } else {
                                long space = json.getLong("space") * 1024 * 1024 * 1024;
                                long used = json.getLong("used");
                                listener.onSuccess(url, isOneOSSpace, space, space - used, -1, -1);
                            }
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

    public interface OnSpaceListener {
        void onStart(String url);

        void onSuccess(String url, boolean isOneOSSpace, long total, long free, long total2, long free2);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
