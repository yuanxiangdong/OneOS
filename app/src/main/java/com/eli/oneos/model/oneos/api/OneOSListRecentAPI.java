package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/5.
 */

public class OneOSListRecentAPI extends OneOSBaseAPI{
    private static String TAG = OneOSListRecentAPI.class.getSimpleName();
    private OnRecentListListener listener;
    private int uid;

    public OneOSListRecentAPI(LoginSession loginSession) {
        super(loginSession);
        this.session = loginSession.getSession();
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnRecentListListener(OnRecentListListener listener){
        this.listener = listener;
    }


    public void recentList(int page){

        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        httpUtils.postJson(url, new RequestBody("recent", session, params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "recentList Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            ArrayList<OneOSFile> files = null;
                            int total=0,page=0,pages=0;
                            if (json.has("data")) {
                                JSONObject datajson = json.getJSONObject("data");
                                total = datajson.getInt("total");
                                page = datajson.getInt("page");
                                pages = datajson.getInt("pages");

                                Type type = new TypeToken<List<OneOSFile>>() {
                                }.getType();
                                files = GsonUtils.decodeJSON(datajson.getString("files"), type);

                                if (!EmptyUtils.isEmpty(files)) {

                                    Iterator<OneOSFile> it = files.iterator();
                                    while(it.hasNext()){
                                        OneOSFile file = it.next();
                                        if (file.isDirectory()) {
                                            it.remove();
                                        } else {
                                            file.setIcon(FileUtils.fmtFileIcon(file.getName()));
                                            file.setFmtSize(FileUtils.fmtFileSize(file.getSize()));
                                            file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));
//                                            file.setFmtUDTime(FileUtils.fmtTimeByZone(file.getUdtime()));
//                                            file.setFmtCTTime(FileUtils.fmtTimeByZone(file.getCttime()));

                                            Log.d(TAG,"file = " + file.getName() + ", getTime = " + FileUtils.fmtTimeByZone(file.getTime()));
                                        }
                                    }
                                    Log.d(TAG,"files ======" + files);
                                }

                            }
                            listener.onSuccess(url,total, pages, page, files);
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



    public interface OnRecentListListener{

        void onStart(String url);

        void onSuccess(String url, int total, int pages, int page, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
