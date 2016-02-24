package com.eli.oneos.model.oneos.upgrade;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;

import com.eli.oneos.R;
import com.eli.oneos.utils.AppVersionUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * for check and upgrade app
 *
 * @author shz
 * @since V 1.6.19 RC
 */
public class AppUpgradeManager {
    private static final String TAG = AppUpgradeManager.class.getSimpleName();

    private static final String URL_VERSION = "http://onespace.cc/download/ver.json";
    // private static final String URL_APK = "http://onespace.cc/download/";

    // private static final int MSG_GET_CURRENT_VERSION = 0x01;
    // private static final int MSG_GET_SERVER_VERSION = 0x02;
    private static final int MSG_DONWLOAD_APP_OVER = 0x03;
    private static final int MSG_CHECK_UPGRADE_OVER = 0x04;

    private OnUpgradeListener listener;
    private Activity activity;

    private String curVersion = null;
    private String serverVersion = null;
    private String appUrl = null;
    private File newAppFile = null;
    private ProgressDialog mProgressDialog;
    private DownloadAppThread mDownloadAppThread;

    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // case MSG_GET_CURRENT_VERSION:
                // break;
                // case MSG_GET_SERVER_VERSION:
                // isNeedsToUpgrade();
                // break;
                case MSG_DONWLOAD_APP_OVER:
                    doInstallApp();
                    break;
                case MSG_CHECK_UPGRADE_OVER:
                    if (null != listener) {
                        listener.onUpgrade(isNeedsToUpgrade(), curVersion, serverVersion, appUrl);
                    }
                    break;
            }
        }
    };

    public AppUpgradeManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * check app upgrade
     */
    public void checkAppUpgrade() {
        CheckAppUpdateThread mThread = new CheckAppUpdateThread();
        mThread.start();
    }

    /**
     * upgrade app
     */
    public void upgradeApp() {
        confirmUpdateDialog();
    }

    private boolean isNeedsToUpgrade() {
        if (serverVersion == null || appUrl == null) {
            // ToastHelper.showToast(R.string.get_version_from_server_exception);
            return false;
        }

        int curVersionCode = 0;
        int serverVersionCode = 0;
        try {
            curVersionCode = covertVersionToNumber(curVersion);
            serverVersionCode = covertVersionToNumber(serverVersion);
        } catch (Exception e) {
            e.printStackTrace();
            // ToastHelper.showToast(R.string.get_version_from_server_exception);
        }

        if (serverVersionCode > curVersionCode) {
            return true;
        }

        return false;
    }

    private void sendMessageToUI(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        if (obj != null) {
            msg.obj = obj;
        }
        if (handle != null) {
            handle.sendMessage(msg);
        }
    }

    private class CheckAppUpdateThread extends Thread {

        @Override
        public void run() {
            curVersion = AppVersionUtils.getAppVersion();
            getServerAppVersion();
            sendMessageToUI(MSG_CHECK_UPGRADE_OVER, null);
        }
    }

    private void doInstallApp() {
        if (newAppFile == null || !newAppFile.exists()) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            ToastHelper.showToast(R.string.download_app_failed);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + newAppFile.toString()),
                "application/vnd.android.package-archive");
        activity.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void doDownloadApp() {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(activity.getResources().getString(R.string.download_new_app));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        mProgressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    confirmCancelDowloadDialog(mProgressDialog, mDownloadAppThread);
                    return false;
                }

                return true;
            }
        });
        mDownloadAppThread = new DownloadAppThread(mProgressDialog);
        mDownloadAppThread.start();
    }

    private void downloadApp() {
        if (Utils.isWifiAvailable(activity)) {
            doDownloadApp();
        } else {
            DialogUtils.showConfirmDialog(activity, R.string.tip, R.string.confirm_download_not_wifi,
                    R.string.dialog_continue, R.string.cancel,
                    new DialogUtils.OnDialogClickListener() {

                        @Override
                        public void onClick(boolean isPositiveBtn) {
                            if (isPositiveBtn) {
                                doDownloadApp();
                            }
                        }
                    });
        }
    }

    private void confirmUpdateDialog() {
        Resources resources = activity.getResources();
        String titleStr = activity.getResources().getString(R.string.have_new_version_app) + serverVersion;
        DialogUtils.showConfirmDialog(activity, resources.getString(R.string.tip), titleStr, resources.getString(R.string.confirm_update_app),
                resources.getString(R.string.ignore_update_app), new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            downloadApp();
                        }
                    }
                });
    }

    private void confirmCancelDowloadDialog(final ProgressDialog mDialog, final DownloadAppThread mDownloadThread) {

        DialogUtils.showConfirmDialog(activity, R.string.tip, R.string.confirm_cancel_download_app, R.string.interrupt_download,
                R.string.continue_download, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            if (null != mDownloadAppThread && mDownloadAppThread.isAlive()) {
                                mDownloadAppThread.stopDownload();
                            }
                        }
                    }
                });
    }

    // version formate is [xx.xx.xx.xx xx]
    private int covertVersionToNumber(String version) throws NumberFormatException {
        String regEx = "[^.0-9]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(version);
        String numStr = matcher.replaceAll("").trim();
        // Logged.d(TAG, "Version Num String: " + numStr);
        String[] nums = numStr.split("\\.");
        int ver = 0;
        for (int i = 0; i < nums.length; i++) {
            // Logged.d(TAG, "Version Num string " + i + ": " + nums[i]);
            int leverl = 4 - i - 1;
            ver += Integer.valueOf(nums[i]) * Math.pow(10, leverl * 2);
        }

        // Logged.d(TAG, "Version Num: " + ver);

        return ver;
    }

    private void getServerAppVersion() {
        try {
            URL url = new URL(URL_VERSION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            if (conn.getResponseCode() == HttpStatus.SC_OK) {
                InputStream is = conn.getInputStream();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                String resultStr = outputStream.toString();
                outputStream.close();
                is.close();

                JSONObject json = new JSONObject(resultStr);
                JSONObject apkJson = json.getJSONObject("android");
                serverVersion = apkJson.getString("str");
                appUrl = apkJson.getString("file");
                Log.d(TAG, "Server App Version: " + serverVersion + "; File DownLoad URL: "
                        + appUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class DownloadAppThread extends Thread {
        private ProgressDialog dialog;
        private boolean isInterrupt = false;

        public DownloadAppThread(ProgressDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void run() {
            try {
                newAppFile = downloadAppFromServer(appUrl, dialog);
            } catch (Exception e) {
                e.printStackTrace();
                newAppFile = null;
                Log.e(TAG, "download new app exception");
            }
            sendMessageToUI(MSG_DONWLOAD_APP_OVER, null);
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

    public void setOnUpgradeListener(OnUpgradeListener listener) {
        this.listener = listener;
    }

    public interface OnUpgradeListener {
        /**
         * Check upgrade callback. if do not have new version
         *
         * @param hasUpgrade if has new version
         * @param curVersion current app version
         * @param newVersion new app version
         * @param url        new app download url
         */
        void onUpgrade(boolean hasUpgrade, String curVersion, String newVersion, String url);
    }
}
