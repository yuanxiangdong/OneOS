package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSUser;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSListUserAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSListUserAPI.class.getSimpleName();

    private OnListUserListener listener;

    public OneOSListUserAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnListUserListener(OnListUserListener listener) {
        this.listener = listener;
    }

    public void list() {
        AjaxParams params = new AjaxParams();
        url = genOneOSAPIUrl(OneOSAPIs.USER_LIST);
        params.put("session", session);
        logHttp(TAG, url, params);

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
                            List<OneOSUser> userList = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray("users");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                json = jsonArray.getJSONObject(i);
                                String name = json.getString("username");
                                int uid = json.getInt("uid");
                                int gid = json.getInt("gid");
                                userList.add(new OneOSUser(name, uid, gid));
                            }
                            listener.onSuccess(url, userList);
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

    public interface OnListUserListener {
        void onStart(String url);

        void onSuccess(String url, List<OneOSUser> users);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
