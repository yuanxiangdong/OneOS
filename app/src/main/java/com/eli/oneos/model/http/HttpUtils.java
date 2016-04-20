package com.eli.oneos.model.http;

import android.util.Log;

import com.eli.oneos.model.log.Logged;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.Map;

public class HttpUtils {
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

    public void get(String url, OnHttpListener<? extends Object> callBack) {
        log(TAG, url, null);
        finalHttp.get(url, callBack);
    }

    public void post(String url, OnHttpListener<? extends Object> callBack) {
        log(TAG, url, null);
        finalHttp.post(url, callBack);
    }

    public void post(String url, Map<String, String> params, OnHttpListener<? extends Object> callBack) {
        AjaxParams ajaxParams = new AjaxParams(params);
        log(TAG, url, ajaxParams);
        finalHttp.post(url, ajaxParams, callBack);
    }

    public Object postSync(String url, Map<String, String> params) {
        AjaxParams ajaxParams = new AjaxParams(params);
        log(TAG, url, ajaxParams);

        return finalHttp.postSync(url, ajaxParams);
    }

    public static void log(String TAG, String url, AjaxParams params) {
        if (Logged.DEBUG) {
            Log.d(TAG, "ID:" + (COUNTER++) + " {Url: " + url + ", Params: " + (params == null ? "Null" : params.toString()) + "}");
        }
    }
}
