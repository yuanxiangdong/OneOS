package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.logger.LogLevel;
import com.eli.oneos.model.logger.Logged;
import com.eli.oneos.model.logger.Logger;
import com.eli.oneos.model.oneos.api.OneOSUploadFileAPI;
import com.eli.oneos.model.oneos.backup.BackupElement;
import com.eli.oneos.model.oneos.user.LoginSession;

/**
 * The thread for upload file to server, based on HTTP or Socket.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class UploadFileThread extends Thread {
    private final String TAG = UploadFileThread.class.getSimpleName();

    private BackupElement mElement;
    private OnUploadListener mListener = null;
    private OneOSUploadFileAPI uploadFileAPI;
    private LoginSession mLoginSession;

    public UploadFileThread(BackupElement element, LoginSession mLoginSession, OnUploadListener mListener) {
        if (mListener == null || mLoginSession == null) {
            logger(LogLevel.ERROR, "OnUploadListener or LoginSession is NULL");
            new Throwable(new NullPointerException("OnUploadListener or LoginSession is NULL"));
        }
        this.mLoginSession = mLoginSession;
        this.mElement = element;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        uploadFileAPI = new OneOSUploadFileAPI(mLoginSession, mElement);
        uploadFileAPI.setOnUploadFileListener(new OneOSUploadFileAPI.OnUploadFileListener() {
            @Override
            public void onStart(String url, UploadElement element) {
                logger(LogLevel.INFO, "Start Upload file: " + element.getSrcPath());
            }

            @Override
            public void onComplete(String url, UploadElement element) {
                logger(LogLevel.INFO, "Complete Upload file: " + element.getSrcPath() + ", state: " + element.getState());
                mListener.onComplete(element);
            }
        });
        uploadFileAPI.upload();
    }

    public void stopBackupPhoto() {
        uploadFileAPI.stopUpload();
        mElement.setState(TransferState.PAUSE);
        interrupt();
        logger(LogLevel.DEBUG, "Stop Upload file");
    }

    private void logger(LogLevel level, String msg) {
        if (Logged.UPLOAD) {
            Logger.p(level, TAG, msg);
        }
    }

    public interface OnUploadListener {
        void onComplete(UploadElement mElement);
    }
}