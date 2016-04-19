package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS File Manage API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSFileManageAPI.class.getSimpleName();

    private FileManageAction action;
    private OnFileManageListener listener;

    public OneOSFileManageAPI(String ip, String port, String session) {
        super(ip, port, session);
    }

    private void doManageFiles(Map<String, String> params) {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, action, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            listener.onSuccess(url, action, result);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = context.getResources().getString(R.string.operate_failed);
                            if (json.has("msg")) {
                                msg = json.getString("msg");
                            }
                            listener.onFailure(url, action, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, action, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url, action);
        }
    }

    public void attr(OneOSFile file) {
        this.action = FileManageAction.ATTR;
        String path = file.getPath();
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "attributes");
        params.put("path", path);

        doManageFiles(params);
    }

    public void delete(ArrayList<OneOSFile> delList, boolean isDelShift) {
        this.action = isDelShift ? FileManageAction.DELETE_SHIFT : FileManageAction.DELETE;
        String path = genJsonArray(delList);
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", isDelShift ? "deleteshift" : "delete");
        params.put("path", path);
        doManageFiles(params);
    }

    public void chmod(OneOSFile file, String group, String other) {
        this.action = FileManageAction.CHMOD;
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "chmod");
        params.put("path", file.getPath());
        params.put("group", group);
        params.put("other", other);
        doManageFiles(params);
    }

    public void move(ArrayList<OneOSFile> delList, String toDir) {
        this.action = FileManageAction.MOVE;
        Log.d(TAG, "Move file to: " + toDir);
        String path = genJsonArray(delList);
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "move");
        params.put("path", path);
        params.put("todir", toDir);

        doManageFiles(params);
    }

    public void copy(ArrayList<OneOSFile> delList, String toDir) {
        this.action = FileManageAction.COPY;
        String path = genJsonArray(delList);
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "copy");
        params.put("path", path);
        params.put("todir", toDir);

        doManageFiles(params);
    }

    public void rename(OneOSFile file, String newName) {
        this.action = FileManageAction.RENAME;
        String path = file.getPath();
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "rename");
        params.put("path", path);
        params.put("newname", newName);

        doManageFiles(params);
    }

    public void rename(String path, String newName) {
        this.action = FileManageAction.RENAME;
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "rename");
        params.put("path", path);
        params.put("newname", newName);

        doManageFiles(params);
    }

    public void mkdir(String path, String newName) {
        this.action = FileManageAction.MKDIR;

        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "mkdir");
        params.put("path", path);
        params.put("newname", newName);

        doManageFiles(params);
    }

    public void crypt(OneOSFile file, String pwd, boolean isEncrypt) {
        this.action = isEncrypt ? FileManageAction.ENCRYPT : FileManageAction.DECRYPT;
        String path = file.getPath();
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", isEncrypt ? "encrypt" : "decrypt");
        params.put("path", path);
        params.put("password", pwd);

        doManageFiles(params);
    }

    public void cleanRecycle() {
        this.action = FileManageAction.CLEAN_RECYCLE;
        Map<String, String> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "cleanrecycle");

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
        void onStart(String url, FileManageAction action);

        void onSuccess(String url, FileManageAction action, String response);

        void onFailure(String url, FileManageAction action, int errorNo, String errorMsg);
    }
}
