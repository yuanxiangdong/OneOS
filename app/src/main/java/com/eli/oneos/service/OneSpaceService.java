package com.eli.oneos.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.backup.file.BackupFileManager;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.DownloadManager;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.transfer.UploadManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OneSpaceService extends Service {
    private static final String TAG = OneSpaceService.class.getSimpleName();

    private Context context;
    private ServiceBinder mBinder;
    private DownloadManager mDownloadManager;
    private UploadManager mUploadManager;
    private BackupFileManager mBackupPhotoManager;
    private List<DownloadManager.OnDownloadCompleteListener> mDownloadCompleteListenerList = new ArrayList<DownloadManager.OnDownloadCompleteListener>();
    private List<UploadManager.OnUploadCompleteListener> mUploadCompleteListenerList = new ArrayList<UploadManager.OnUploadCompleteListener>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = MyApplication.getAppContext();

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
        public OneSpaceService getService() {
            return OneSpaceService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }


    // ==========================================Auto Backup file==========================================
    public void startBackupFile() {
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        if (!loginSession.getUserSettings().getIsAutoBackupFile()) {
            Log.e(TAG, "Do not open auto backup photo");
            return;
        }
        if (mBackupPhotoManager != null) {
            mBackupPhotoManager.stopBackup();
        }

        mBackupPhotoManager = new BackupFileManager(loginSession, context);
        mBackupPhotoManager.startBackup();
        Log.d(TAG, "======Start Backup Service=======");
    }

    public void stopBackupFile() {
        if (mBackupPhotoManager != null) {
            mBackupPhotoManager.stopBackup();
            mBackupPhotoManager = null;
        }
    }

    public void resetBackupFile() {
        stopBackupFile();
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        BackupFileKeeper.reset(loginSession.getUserInfo().getId());
        startBackupFile();
    }

    public int getBackupFileCount() {
        if (mBackupPhotoManager == null) {
            return 0;
        }
        return mBackupPhotoManager.getBackupListSize();
    }
    // ==========================================Auto Backup file==========================================


    // ========================================Download and Upload file======================================

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
        Log.d("OneSpaceService", "pause download: " + fullName);
        mDownloadManager.pauseDownload(fullName);
    }

    public void pauseDownload() {
        Log.d("OneSpaceService", "pause activeUsers download");
        mDownloadManager.pauseDownload();
    }

    public void continueDownload(String fullName) {
        mDownloadManager.continueDownload(fullName);
    }

    public void continueDownload() {
        Log.d("OneSpaceService", "continue activeUsers download");
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
        Log.d("OneSpaceService", "pause upload: " + filepath);
        mUploadManager.pauseUpload(filepath);
    }

    public void pauseUpload() {
        Log.d("OneSpaceService", "pause activeUsers upload");
        mUploadManager.pauseUpload();
    }

    public void continueUpload(String filepath) {
        Log.d("OneSpaceService", "continue upload: " + filepath);
        mUploadManager.continueUpload(filepath);
    }

    public void continueUpload() {
        Log.d("OneSpaceService", "continue activeUsers  upload");
        mUploadManager.continueUpload();
    }

    public void cancelUpload(String filepath) {
        mUploadManager.removeUpload(filepath);
    }

    public void cancelUpload() {
        mUploadManager.removeUpload();
    }
    // ========================================Download and Upload file======================================

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ACTIVITY_SERVICE, "Transfer service destroy.");
        mDownloadManager.destroy();
        mUploadManager.destroy();
        stopBackupFile();
    }
}
