package com.eli.oneos.model.http;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.oneos.user.LoginManage;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import www.glinkwin.com.glink.ssudp.SSUDPRequest;
import www.glinkwin.com.glink.ssudp.SSUDPConst;
import www.glinkwin.com.glink.ssudp.SSUDPManager;

public class HttpUtils<T> {
    private static final String TAG = HttpUtils.class.getSimpleName();
    private static final int TIMEOUT = 30 * 1000;
    private static long COUNTER = 0;

    private FinalHttp finalHttp;

    public HttpUtils() {
        this(TIMEOUT);
    }

    public HttpUtils(int timeout) {
        finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());
        finalHttp.configTimeout(timeout);
    }

    public void get(String url, OnHttpListener<T> callBack) {
        if (LoginManage.getInstance().isSSUDP()) {
            if (sendSSUDP(url, null, callBack)) {
                return;
            }
        }

        log(TAG, url, null);
        finalHttp.get(url, callBack);
    }

    public void post(String url, OnHttpListener<T> callBack) {
        if (LoginManage.getInstance().isSSUDP()) {
            if (sendSSUDP(url, null, callBack)) {
                return;
            }
        }

        log(TAG, url, null);
        finalHttp.post(url, callBack);
    }

    public void post(String url, Map<String, String> params, OnHttpListener<T> callBack) {
        if (LoginManage.getInstance().isSSUDP()) {
            if (sendSSUDP(url, params, callBack)) {
                return;
            }
        }

        AjaxParams ajaxParams = new AjaxParams(params);
        log(TAG, url, ajaxParams);
        finalHttp.post(url, ajaxParams, callBack);
    }

    public Object postSync(String url, Map<String, String> params) {
        if (!LoginManage.getInstance().isSSUDP()) {
            AjaxParams ajaxParams = new AjaxParams(params);
            log(TAG, url, ajaxParams);

            return finalHttp.postSync(url, ajaxParams);
        }

        return null;
    }

    /**
     * send request throw SSUDP
     *
     * @param url
     * @param params
     * @param callBack
     * @return {@code true} if can use SSUDP, otherwise {@code false}
     */
    private boolean sendSSUDP(String url, Map<String, String> params, final OnHttpListener<T> callBack) {
        SSUDPManager ssudpManager = SSUDPManager.getInstance();
        JSONObject json = new JSONObject();
        try {
            int pos = url.indexOf(OneOSAPIs.ONE_API);
            if (pos < 0) {
                return false;
            }
            url = url.substring(pos);
            json.put("api", url);
            if (null != params) {
                Iterator iterator = params.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    json.put((String) entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String req = json.toString();
        ssudpManager.sendSSUDPRequest(req, new SSUDPRequest.OnSSUdpResponseListener() {
            @Override
            public void onSuccess(byte[] header, byte[] buffer) {
                try {
                    String response = new String(buffer, "UTF-8").trim();
                    callBack.onSuccess((T) response);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    callBack.onFailure(new Exception("SSUDP request failed!"), SSUDPConst.SSUPD_ERROR_NO_CONTENT, "No content response.");
                }
            }

            @Override
            public void onFailure(int errno, String errMsg) {
                callBack.onFailure(new Exception("SSUDP request failed!"), errno, errMsg);
            }
        });

        return true;
    }

    public static void log(String TAG, String url, AjaxParams params) {
        if (Logged.DEBUG) {
            Log.d(TAG, "ID:" + (COUNTER++) + " {Url: " + url + ", Params: " + (params == null ? "Null" : params.toString()) + "}");
        }
    }
}
