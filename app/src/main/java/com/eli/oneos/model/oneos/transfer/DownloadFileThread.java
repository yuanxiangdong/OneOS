package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSDownloadFileAPI;
import com.eli.oneos.model.oneos.user.LoginSession;

/**
 * The thread for download file from server, base on HTTP.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class DownloadFileThread extends Thread {
    private static final String TAG = DownloadFileThread.class.getSimpleName();
    private static final boolean IS_LOG = Logged.DOWNLOAD;

    private boolean isInterrupt = false;
    private DownloadElement mElement;
    private LoginSession loginSession = null;
    private OnDownloadResultListener mListener = null;
    private OneOSDownloadFileAPI.OnDownloadFileListener mDownloadListener = null;
    private OneOSDownloadFileAPI downloadFileAPI = null;

    public DownloadFileThread(DownloadElement element, LoginSession loginSession, OnDownloadResultListener mListener) {
        if (mListener == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "DownloadResultListener is NULL");
            throw new NullPointerException("DownloadResultListener is NULL");
        }
        this.mElement = element;
        this.loginSession = loginSession;
        this.mListener = mListener;
    }

    public DownloadFileThread(DownloadElement element, LoginSession loginSession, OneOSDownloadFileAPI.OnDownloadFileListener mDownloadListener) {
        if (mListener == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "DownloadFileListener is NULL");
            throw new NullPointerException("DownloadFileListener is NULL");
        }
        this.mElement = element;
        this.loginSession = loginSession;
        this.mDownloadListener = mDownloadListener;
    }

    @Override
    public void run() {
        // httpPostDownload();
        downloadFileAPI = new OneOSDownloadFileAPI(loginSession, mElement);
        if (mDownloadListener != null) {
            downloadFileAPI.setOnDownloadFileListener(mDownloadListener);
        } else {
            downloadFileAPI.setOnDownloadFileListener(new OneOSDownloadFileAPI.OnDownloadFileListener() {
                @Override
                public void onStart(String url, DownloadElement element) {
                    Logger.p(LogLevel.INFO, IS_LOG, TAG, "Start Download file: " + element.getSrcPath());
                }

                @Override
                public void onUploading(String url, DownloadElement element) {
                }

                @Override
                public void onComplete(String url, DownloadElement element) {
                    Logger.p(LogLevel.INFO, IS_LOG, TAG, "Complete Download file: " + element.getSrcPath() + ", state: " + element.getState());
                    mListener.onResult(element);
                }
            });
        }
        downloadFileAPI.download();
    }


    public void stopDownload() {
        isInterrupt = true;
        mElement.setState(TransferState.PAUSE);
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Stop download");
    }


    public interface OnDownloadResultListener {
        void onResult(DownloadElement element);
    }
}
