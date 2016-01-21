package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.GsonUtils;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * OneSpace OS File Manage API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSFileManageAPI.class.getSimpleName();

    private FileManageAction action;
    private ArrayList<OneOSFile> fileList;

    private OnFileManageListener listener;

    public OneOSFileManageAPI(String ip, String port, String session) {
        super(ip, port, session);
    }

    private void doManageFiles(AjaxParams params) {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);

        finalHttp.post(url, params, new AjaxCallBack<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, fileList, action, errorNo, strMsg);
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
                            listener.onSuccess(url, fileList, action);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = context.getResources().getString(R.string.operate_failed);
                            if (json.has("msg")) {
                                msg = json.getString("msg");
                            }
                            listener.onFailure(url, fileList, action, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, fileList, action, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url, fileList, action);
        }
    }

    public void delete(ArrayList<OneOSFile> delList, boolean isDelShift) {
        this.fileList = delList;
        this.action = isDelShift ? FileManageAction.DELETE_SHIFT : FileManageAction.DELETE;

        Log.d(TAG, "url = " + url);
        String path = genJsonArray(delList);
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        Log.d(TAG, "session = " + session);
        params.put("cmd", isDelShift ? "deleteshift" : "delete");
        Log.d(TAG, "cmd = " + (isDelShift ? "deleteshift" : "delete"));
        params.put("path", path);
        Log.d(TAG, "path = " + path);
        doManageFiles(params);
    }

    public void move(ArrayList<OneOSFile> delList, String toDir) {
        this.fileList = delList;
        this.action = FileManageAction.MOVE;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        String path = genJsonArray(delList);
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "move");
        params.put("path", path);
        params.put("todir", toDir);

        doManageFiles(params);
    }

    public void copy(ArrayList<OneOSFile> delList, String toDir) {
        this.fileList = delList;
        this.action = FileManageAction.COPY;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        String path = genJsonArray(delList);
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "copy");
        params.put("path", path);
        params.put("todir", toDir);

        doManageFiles(params);
    }

    public void rename(OneOSFile file, String newName) {
        this.fileList = new ArrayList<>();
        this.fileList.add(file);
        this.action = FileManageAction.RENAME;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        String path = file.getPath();
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "rename");
        params.put("path", path);
        params.put("newname", newName);

        doManageFiles(params);
    }

    public void mkdir(OneOSFile file, String newName) {
        this.fileList = new ArrayList<>();
        this.fileList.add(file);
        this.action = FileManageAction.MKDIR;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        String path = file.getPath();
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "mkdir");
        params.put("path", path);
        params.put("newname", newName);

        doManageFiles(params);
    }

    public void crypt(OneOSFile file, String pwd, boolean isEncrypt) {
        this.fileList = new ArrayList<>();
        this.fileList.add(file);
        this.action = isEncrypt ? FileManageAction.ENCRYPT : FileManageAction.DECRYPT;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        String path = file.getPath();
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", isEncrypt ? "encrypt" : "decrypt");
        params.put("path", path);
        params.put("password", pwd);

        doManageFiles(params);
    }

    public void cleanRecycle() {
        this.action = FileManageAction.CLEAN_RECYCLE;

        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "cleanrecycle");
        // params.put("path", "[]");

        doManageFiles(params);
    }

    private String genJsonArray(ArrayList<OneOSFile> fileList) {
        if (EmptyUtils.isEmpty(fileList)) {
            return "";
        }

        ArrayList<String> pathList = new ArrayList<>();
        for (OneOSFile file : fileList) {
            pathList.add(file.getPath());
        }

        return GsonUtils.encodeJSON(pathList);
    }

    public void setOnFileManageListener(OnFileManageListener listener) {
        this.listener = listener;
    }

    public interface OnFileManageListener {
        void onStart(String url, ArrayList<OneOSFile> fileList, FileManageAction action);

        void onSuccess(String url, ArrayList<OneOSFile> fileList, FileManageAction action);

        void onFailure(String url, ArrayList<OneOSFile> fileList, FileManageAction action, int errorNo, String errorMsg);
    }
}
