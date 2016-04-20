package com.eli.oneos.model.upgrade;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.SharedPreferencesHelper;
import com.eli.oneos.model.http.HttpUtils;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.utils.AppVersionUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * for check and upgrade app
 *
 * @author gaoyun@eli-tech.com
 */
public class AppUpgradeManager {
    private static final String TAG = AppUpgradeManager.class.getSimpleName();

    private static final String URL_VERSION = "http://onespace.cc/download/oneos/ver1.json";
    private static final String ANDROID_VERSION_NAME = "android";

    private Activity activity;

    public AppUpgradeManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * If it detects a new version, continue to confirm the upgrade
     */
    public void detectAppUpgrade() {
        detectAppUpgrade(null);
    }

    /**
     * Detects app new version and callback
     *
     * @param callback if callback is {@code null}, see {@link AppUpgradeManager#detectAppUpgrade()}, else callback.
     */
    public void detectAppUpgrade(final OnUpgradeListener callback) {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.get(URL_VERSION, new OnHttpListener<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Version: " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject verJson = json.getJSONObject(ANDROID_VERSION_NAME);
                    String latestVersion = verJson.getString("str");
                    Log.d(TAG, "Latest App Version: " + latestVersion);
                    String curVersion = AppVersionUtils.getAppVersion();
                    String ignoreVersion = SharedPreferencesHelper.get(Constants.SP_KEY_IGNORE_VERSION_NO);
                    boolean ignored = (null != ignoreVersion) && ignoreVersion.equals(latestVersion);

                    if (!ignored && AppVersionUtils.checkUpgrade(curVersion, latestVersion)) {
                        String appUrl = verJson.getString("file");
                        ArrayList<String> logs = new ArrayList<>();
                        if (verJson.has("log")) {
                            JSONArray logArray = verJson.getJSONArray("log");
                            for (int i = 0; i < logArray.length(); i++) {
                                logs.add((i + 1) + ". " + logArray.getString(i));
                            }
                        }

                        if (null != callback) {
                            callback.onUpgrade(true, curVersion, latestVersion, appUrl, logs);
                        } else {
                            confirmUpgradeDialog(latestVersion, appUrl, logs);
                        }
                    } else {
                        if (null != callback) {
                            callback.onUpgrade(false, null, null, null, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                Log.e(TAG, "ErrorNo: " + errorNo + ", ErrorMsg: " + strMsg + ", Throws: " + t.toString());
            }
        });
    }

    public void confirmUpgradeDialog(final String serverVersion, final String url, final ArrayList<String> logs) {

        Resources resources = activity.getResources();
        String titleStr = activity.getResources().getString(R.string.have_new_version_app) + serverVersion;

        if (EmptyUtils.isEmpty(logs)) {
            DialogUtils.showConfirmDialog(activity, resources.getString(R.string.tips), titleStr, resources.getString(R.string.upgrade_app_now),
                    resources.getString(R.string.upgrade_next_time), new DialogUtils.OnDialogClickListener() {

                        @Override
                        public void onClick(boolean isPositiveBtn) {
                            if (isPositiveBtn) {
                                downloadApp(url);
                            }
                        }
                    });
        } else {
            DialogUtils.showListDialog(activity, logs, titleStr, resources.getString(R.string.ignore_this_version), resources.getString(R.string.upgrade_next_time),
                    resources.getString(R.string.upgrade_app_now), new DialogUtils.OnMultiDialogClickListener() {
                        @Override
                        public void onClick(int index) {
                            if (index == 0) {
                                downloadApp(url);
                            } else if (index == 2) {
                                // ignore this version
                                SharedPreferencesHelper.put(Constants.SP_KEY_IGNORE_VERSION_NO, serverVersion);
                            }
                        }
                    });
        }
    }

    private void downloadApp(final String url) {
        if (Utils.isWifiAvailable(activity)) {
            doDownloadApp(url);
        } else {
            DialogUtils.showConfirmDialog(activity, R.string.tips, R.string.confirm_download_not_wifi,
                    R.string.dialog_continue, R.string.cancel, new DialogUtils.OnDialogClickListener() {

                        @Override
                        public void onClick(boolean isPositiveBtn) {
                            if (isPositiveBtn) {
                                doDownloadApp(url);
                            }
                        }
                    });
        }
    }

    private void doDownloadApp(final String url) {
        final ProgressDialog mProgressDialog = new ProgressDialog(activity);
        final DownloadAppThread mDownloadAppThread = new DownloadAppThread(url, mProgressDialog);

        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(activity.getResources().getString(R.string.download_new_app));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        mProgressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    confirmCancelDownloadDialog(mProgressDialog, mDownloadAppThread);
                    return false;
                }

                return true;
            }
        });
        mDownloadAppThread.start();
    }

    private void confirmCancelDownloadDialog(final ProgressDialog mProgressDialog, final DownloadAppThread mDownloadThread) {
        DialogUtils.showConfirmDialog(activity, R.string.tips, R.string.confirm_cancel_download_app, R.string.interrupt_download,
                R.string.continue_download, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            if (null != mDownloadThread && mDownloadThread.isAlive()) {
                                mDownloadThread.stopDownload();
                            }
                        }

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private void doInstallApp(File newAppFile) {
        if (newAppFile == null || !newAppFile.exists()) {
            ToastHelper.showToast(R.string.download_app_failed);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + newAppFile.toString()), "application/vnd.android.package-archive");
        activity.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private class DownloadAppThread extends Thread {
        private ProgressDialog dialog;
        private String url;
        private boolean isInterrupt = false;
        private File newAppFile;

        public DownloadAppThread(String url, ProgressDialog dialog) {
            this.dialog = dialog;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                newAppFile = downloadAppFromServer(url, dialog);
            } catch (Exception e) {
                e.printStackTrace();
                newAppFile = null;
                Log.e(TAG, "download new app exception");
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doInstallApp(newAppFile);
                }
            });
        }

        public void stopDownload() {
            this.isInterrupt = true;
            interrupt();
        }

        private File downloadAppFromServer(String urlStr, ProgressDialog progress) throws Exception {
            if (urlStr == null) {
                return null;
            }

            String name = urlStr.substring(urlStr.lastIndexOf("/"), urlStr.length());
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);

                if (progress != null) {
                    progress.setMax(conn.getContentLength());
                }
                InputStream is = conn.getInputStream();
                File file = new File(Environment.getExternalStorageDirectory(), name);
                FileOutputStream fos = new FileOutputStream(file, false);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len;
                int total = 0;
                while (!isInterrupt && (len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    total += len;
                    if (progress != null) {
                        String numberFormat = Formatter.formatFileSize(activity, total) + "/"
                                + Formatter.formatFileSize(activity, conn.getContentLength());
                        progress.setProgress(total);
                        progress.setProgressNumberFormat(numberFormat);
                    }
                }
                fos.close();
                bis.close();
                is.close();

                if (isInterrupt) {
                    return null;
                } else {
                    return file;
                }
            } else {
                return null;
            }
        }
    }

    public interface OnUpgradeListener {
        /**
         * Check upgrade callback
         *
         * @param hasUpgrade if has new version
         * @param curVersion current app version
         * @param newVersion new app version
         * @param logs       new app change logs
         */
        void onUpgrade(boolean hasUpgrade, String curVersion, String newVersion, String appUrl, ArrayList<String> logs);
    }
}
