package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Get OneSpace OneOS version API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/6.
 */
public class OneOSVersionAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSVersionAPI.class.getSimpleName();

    private OnSystemVersionListener listener;

    public OneOSVersionAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnSystemVersionListener(OnSystemVersionListener listener) {
        this.listener = listener;
    }

    public void query() {
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_VERSION);
        logHttp(TAG, url, null);

        finalHttp.post(url, null, new AjaxCallBack<String>() {
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
                            // {result, model, version, needup}
                            String model = json.getString("model");
                            String product = json.getString("product");
                            String version = json.getString("version");
                            boolean needsUp = json.getBoolean("needup");

                            listener.onSuccess(url, model, product, version, needsUp);
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

    public interface OnSystemVersionListener {
        void onStart(String url);

        void onSuccess(String url, String model, String product, String version, boolean needsUp);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
