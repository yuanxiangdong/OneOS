package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.PluginInfo;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * OneSpace OS List Plugins API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSListPluginAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSListPluginAPI.class.getSimpleName();

    private OnListPluginListener listener;

    public OneOSListPluginAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnListPluginListener(OnListPluginListener listener) {
        this.listener = listener;
    }

    public void list() {
        url = genOneOSAPIUrl(OneOSAPIs.APP_LIST);
        AjaxParams params = new AjaxParams();
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
                            ArrayList<PluginInfo> mPlugList = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray("apps");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                PluginInfo info = new PluginInfo(jsonArray.getJSONObject(i));
                                mPlugList.add(info);
                            }
                            for (int i = 0; i < mPlugList.size(); i++) {
                                if (mPlugList.get(i).getPack().equalsIgnoreCase("todo")) {
                                    mPlugList.remove(i);
                                    break;
                                }
                            }
                            Log.e(TAG, "Count: " + mPlugList.size());
                            listener.onSuccess(url, mPlugList);
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

    public interface OnListPluginListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<PluginInfo> plugins);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
