package com.eli.oneos.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.trans.DownloadElement;
import com.eli.oneos.model.oneos.trans.DownloadManager;
import com.eli.oneos.model.oneos.trans.UploadElement;
import com.eli.oneos.model.oneos.trans.UploadManager;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransferService extends Service {
    private ServiceBinder mBinder;
    private DownloadManager mDownloadManager;
    private UploadManager mUploadManager;
    private List<DownloadManager.OnDownloadCompleteListener> mDownloadCompleteListenerList = new ArrayList<DownloadManager.OnDownloadCompleteListener>();
    private List<UploadManager.OnUploadCompleteListener> mUploadCompleteListenerList = new ArrayList<UploadManager.OnUploadCompleteListener>();

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadManager = DownloadManager.getInstance();
        mUploadManager = UploadManager.getInstance();
        mDownloadManager.setOnDownloadCompleteListener(new DownloadManager.OnDownloadCompleteListener() {

            @Override
            public void downloadComplete(DownloadElement element) {
                FileUtils.requestScanFile(element.getDownloadFile());

                for (DownloadManager.OnDownloadCompleteListener listener : mDownloadCompleteListenerList) {
                    listener.downloadComplete(element);
                }
            }
        });
        mUploadManager.setOnUploadCompleteListener(new UploadManager.OnUploadCompleteListener() {

            @Override
            public void uploadComplete(UploadElement element) {
                for (UploadManager.OnUploadCompleteListener listener : mUploadCompleteListenerList) {
                    listener.uploadComplete(element);
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new ServiceBinder();
        }
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        public TransferService getService() {
            return TransferService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }

    /**
     * set on download complete listener
     */
    public boolean setOnDownloadCompleteListener(DownloadManager.OnDownloadCompleteListener listener) {
        if (!mDownloadCompleteListenerList.contains(listener)) {
            return mDownloadCompleteListenerList.add(listener);
        }
        return true;
    }

    /**
     * set on upload complete listener
     */
    public boolean setOnUploadCompleteListener(UploadManager.OnUploadCompleteListener listener) {
        if (!mUploadCompleteListenerList.contains(listener)) {
            return mUploadCompleteListenerList.add(listener);
        }
        return true;
    }

    // Download Operation
    public long addDownloadTask(OneOSFile file, String savePath) {
        DownloadElement element = new DownloadElement(file, savePath);
        return mDownloadManager.enqueue(element);
    }

    public ArrayList<DownloadElement> getDownloadList() {
        return mDownloadManager.getDownloadList();
    }

    public void pauseDownload(String fullName) {
        Log.d("TransferService", "pause download: " + fullName);
        mDownloadManager.pauseDownload(fullName);
    }

    public void pauseDownload() {
        Log.d("TransferService", "pause all download");
        mDownloadManager.pauseDownload();
    }

    public void continueDownload(String fullName) {
        mDownloadManager.continueDownload(fullName);
    }

    public void continueDownload() {
        Log.d("TransferService", "continue all download");
        mDownloadManager.continueDownload();
    }

    public void cancelDownload(String path) {
        mDownloadManager.removeDownload(path);
    }

    public void cancelDownload() {
        mDownloadManager.removeDownload();
    }

    // Upload Operation
    public long addUploadTask(File file, String savepath) {
        UploadElement element = new UploadElement(file, savepath);
        return mUploadManager.enqueue(element);
    }

    public ArrayList<UploadElement> getUploadList() {
        return mUploadManager.getUploadList();
    }

    public void pauseUpload(String filepath) {
        Log.d("TransferService", "pause upload: " + filepath);
        mUploadManager.pauseUpload(filepath);
    }

    public void pauseUpload() {
        Log.d("TransferService", "pause all upload");
        mUploadManager.pauseUpload();
    }

    public void continueUpload(String filepath) {
        Log.d("TransferService", "continue upload: " + filepath);
        mUploadManager.continueUpload(filepath);
    }

    public void continueUpload() {
        Log.d("TransferService", "continue all  upload");
        mUploadManager.continueUpload();
    }

    public void cancelUpload(String filepath) {
        mUploadManager.removeUpload(filepath);
    }

    public void cancelUpload() {
        mUploadManager.removeUpload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ACTIVITY_SERVICE, "Transfer service destroy.");
        mDownloadManager.destroy();
        mUploadManager.destroy();
    }
}
