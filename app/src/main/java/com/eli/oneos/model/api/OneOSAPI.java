package com.eli.oneos.model.api;

import android.content.Context;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.OneOSAPIs;

import net.tsz.afinal.FinalHttp;

import org.apache.http.impl.client.BasicCookieStore;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public abstract class OneOSAPI {
    private static final int TIMEOUT = 20 * 1000;

    protected Context context = null;
    protected FinalHttp finalHttp = null;
    protected String url = null;
    protected String ip = null;
    protected String session = null;
    protected String port = OneOSAPIs.ONE_API_DEFAULT_PORT;

    protected OneOSAPI(String ip, String port) {
        this.ip = ip;
        this.port = port;
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
}
