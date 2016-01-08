package com.eli.oneos.utils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();

    private OnHttpResultListener mListener;
    private FinalHttp finalHttp;

    /**
     * Http Request Utils, Based on FinalHttp
     *
     * @param listener
     * @param timeout  unit is seconds
     */
    public HttpUtils(OnHttpResultListener listener, int timeout) {
        this.mListener = listener;
        timeout = timeout <= 0 ? 0 : timeout * 1000;
        finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());
        finalHttp.configTimeout(timeout);
    }

    public void get(String fullUrl) {
        Log.d(TAG, "Get Url:" + fullUrl);
        finalHttp.get(fullUrl, new MyAjaxCallBack(fullUrl));
    }

    public void get(String fullUrl, Map<String, String> data) {
        Uri uri = Uri.parse(fullUrl);
        Builder builder = uri.buildUpon();
        if (data != null) {
            for (Entry<String, String> entry : data.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        String buildUrl = builder.build().toString();
        Log.d(TAG, "Get Url:" + buildUrl);
        // String encodeUrl = URLEncoder.encode(url, HTTP.UTF_8);
        finalHttp.get(buildUrl, new MyAjaxCallBack(buildUrl));
    }

    public void post(String fullUrl, Map<String, String> data) {
        AjaxParams params = new AjaxParams();
        if (data != null) {
            for (Entry<String, String> entry : data.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        Log.d(TAG, "Post Url:" + fullUrl + ", params:" + params.toString());
        finalHttp.post(fullUrl, params, new MyAjaxCallBack(fullUrl));
    }

    public void postJson(String fullUrl, String jsonString) {
        Log.d(TAG, "Post Url:" + fullUrl + ", json string:" + jsonString);
        try {
            finalHttp.post(fullUrl, new StringEntity(jsonString, HTTP.UTF_8), "application/json",
                    new MyAjaxCallBack(fullUrl));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class MyAjaxCallBack extends AjaxCallBack<String> {
        private String mUrl;

        public MyAjaxCallBack(String url) {
            if (mListener != null) {
                mListener.onStart();
            }
            mUrl = url;
        }

        @Override
        public void onFailure(Throwable t, int errorNo, String strMsg) {
            super.onFailure(t, errorNo, strMsg);
            Log.e(TAG, "Response Data:" + strMsg);
            if (mListener != null) {
                mListener.onFailure(mUrl, errorNo, strMsg);
            }
        }

        @Override
        public void onSuccess(String result) {
            super.onSuccess(result);
            Log.d(TAG, "Response Data:" + result);
            if (mListener == null) {
                return;
            }

            try {
                JSONObject resultJson = new JSONObject(result);
                boolean ret = resultJson.getBoolean("result");
                Log.d(TAG, "Result: " + ret);
                if (ret) {
                    mListener.onSuccess(mUrl, result);
                } else {
                    mListener.onFailure(mUrl, 0, result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mListener.onFailure(mUrl, 0, result);
            }
        }
    }

    public interface OnHttpResultListener {
        void onStart();

        void onSuccess(String url, String result);

        void onFailure(String url, int errorNo, String errorMsg);

    }
}
