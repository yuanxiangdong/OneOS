package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.utils.EmptyUtils;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSGetMacAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSGetMacAPI.class.getSimpleName();

    private OnGetMacListener listener;

    public OneOSGetMacAPI(String ip, String port) {
        super(ip, port);
    }

    public void setOnGetMacListener(OnGetMacListener listener) {
        this.listener = listener;
    }

    public void getMac() {
        url = genOneOSAPIUrl(OneOSAPIs.GET_MAC);
        Log.d(TAG, "Get OneSpace Mac: " + url);
        AjaxParams params = new AjaxParams();
        params.put("iface", "eth1");

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
                            // {"result":true, "data":{"NetWork":"192.168.1.0","MacAddr":"78:C2:C0:00:00:98","BMode":"BOND1","NetMode":"DHCP","IPAddress":"0.0.0.0","SndDNS":"192.168.1.1","NetMask":"0.0.0.0","FstDNS":"8.8.8.8"}}
                            String mac = json.getJSONObject("data").getString("MacAddr");
                            if (EmptyUtils.isEmpty(mac)) {
                                listener.onFailure(url, -1, "Response Mac Address is NULL");
                            } else {
                                UserInfo userHistory = UserInfoKeeper.top();
                                if (null != userHistory) {
                                    userHistory.setMac(mac);
                                    UserInfoKeeper.update(userHistory);
                                } else {
                                    Log.e(TAG, "Top User History is NULL");
                                }
                                listener.onSuccess(url, mac);
                            }
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.getString("msg");
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

    public interface OnGetMacListener {
        void onStart(String url);

        void onSuccess(String url, String mac);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
