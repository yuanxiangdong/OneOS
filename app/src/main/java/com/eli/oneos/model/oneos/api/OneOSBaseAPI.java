package com.eli.oneos.model.oneos.api;

import android.content.Context;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.impl.client.BasicCookieStore;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public abstract class OneOSBaseAPI {
    private static final int TIMEOUT = 20 * 1000;

    protected Context context = null;
    protected FinalHttp finalHttp = null;
    protected String url = null;
    protected String ip = null;
    protected String session = null;
    protected String port = OneOSAPIs.ONE_API_DEFAULT_PORT;

    protected OneOSBaseAPI(LoginSession loginSession) {
        this.ip = loginSession.getDeviceInfo().getIp();
        this.port = loginSession.getDeviceInfo().getPort();
        this.session = loginSession.getSession();
        initHttp();
    }

    protected OneOSBaseAPI(DeviceInfo info) {
        this.ip = info.getIp();
        this.port = info.getPort();
        initHttp();
    }

    protected OneOSBaseAPI(String ip, String port) {
        this.ip = ip;
        this.port = port;
        initHttp();
    }

    protected OneOSBaseAPI(String ip, String port, String session) {
        this.ip = ip;
        this.port = port;
        this.session = session;
        initHttp();
    }

    protected void initHttp() {
        initHttp(TIMEOUT);
    }

    protected void initHttp(int timeout) {
        context = MyApplication.getAppContext();
        finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());
        finalHttp.configTimeout(timeout);
    }

    public String genOneOSAPIUrl(String action) {
        return OneOSAPIs.PREFIX_HTTP + ip + ":" + port + action;
    }

    public void logHttp(String TAG, String url, AjaxParams params) {
        Log.d(TAG, "Url: " + url + ", Params: " + (params == null ? "Null" : params.toString()));
    }
}
