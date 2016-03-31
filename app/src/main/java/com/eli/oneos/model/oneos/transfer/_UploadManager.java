package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.user.LoginManage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class _UploadManager {
    private static final String TAG = _UploadManager.class.getSimpleName();

    private List<UploadElement> completeList = Collections.synchronizedList(new ArrayList<UploadElement>());
    private List<UploadElement> uploadList = Collections.synchronizedList(new ArrayList<UploadElement>());
    private List<OnUploadCompleteListener> completeListenerList = new ArrayList<>();
    private static _UploadManager instance = new _UploadManager();
    private UploadFileThread.OnUploadResultListener uploadResultListener = new UploadFileThread.OnUploadResultListener() {

        @Override
        public void onResult(UploadElement mElement) {
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "Upload Result: " + mElement.getState());

            handlerQueueThread.stopCurrentUploadTask();

            synchronized (uploadList) {
                mElement.setTime(System.currentTimeMillis());
                TransferState state = mElement.getState();
                if (state == TransferState.COMPLETE) {
                    long uid = LoginManage.getInstance().getLoginSession().getUserInfo().getId();
                    TransferHistory history = new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(false), mElement.getSrcName(),
                            mElement.getSrcPath(), mElement.getToPath(), mElement.getSize(), mElement.getSize(), 0L, System.currentTimeMillis(), true);
                    TransferHistoryKeeper.insert(history);

                    for (OnUploadCompleteListener listener : completeListenerList) {
                        listener.uploadComplete(mElement);
                    }
                    uploadList.remove(mElement);
                } else {
                    Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Upload pause or failure");
                }
            }

            try {
                Thread.sleep(10); // sleep 10ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handlerQueueThread.notifyNewUploadTask();
        }
    };
    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(uploadList, uploadResultListener);

    private _UploadManager() {
        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }
    }

    /**
     * Singleton instance method
     *
     * @return singleton instance of class
     */
    public static _UploadManager getInstance() {
        return instance;
    }


    public boolean addUploadCompleteListener(OnUploadCompleteListener listener) {
        if (!completeListenerList.contains(listener)) {
            return completeListenerList.add(listener);
        }

        return false;
    }

    public boolean removeUploadCompleteListener(OnUploadCompleteListener listener) {
        return completeListenerList.remove(listener);
    }

    /**
     * Enqueue a new upload. The upload will start automatically once the upload
     * manager is ready to execute it and connectivity is available.
     *
     * @param element the parameters specifying this upload
     * @return an ID for the upload, unique across the system. This ID is used
     * to make future calls related to this upload. If enqueue failed,
     * return -1.
     */
    public int enqueue(UploadElement element) {
        if (element == null) {
            Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "upload element is null");
            return -1;
        }

        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }

        boolean success = uploadList.add(element);
        if (success) {
            synchronized (uploadList) {
                uploadList.notify();
            }
            return element.hashCode();
        } else {
            return -1;
        }
    }

    public boolean pauseUpload(String filePath) {
        UploadElement element = findElement(filePath);

        if (element == null) {
            return false;
        }

        boolean isElementStart = (element.getState() == TransferState.START) ? true : false;
        Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "Pause upload: " + filePath + "; state: " + element.getState());
        if (isElementStart && handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
        }

        element.setOffset(element.getLength());
        element.setState(TransferState.PAUSE);
        if (isElementStart) {
            synchronized (uploadList) {
                uploadList.notify();
            }
        }
        return true;
    }

    /**
     * pause activeUsers upload
     *
     * @return
     */
    public boolean pauseUpload() {
        Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Pause activeUsers upload");

        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
        }

        if (null != uploadList) {
            synchronized (uploadList) {
                for (UploadElement element : uploadList) {
                    element.setOffset(element.getLength());
                    element.setState(TransferState.PAUSE);
                }
            }
        }

        return true;
    }

    public boolean continueUpload(String filepath) {
        Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Continue upload: " + filepath);

        UploadElement element = findElement(filepath);
        if (element == null) {
            return false;
        }

        element.setState(TransferState.WAIT);
        synchronized (uploadList) {
            uploadList.notify();
        }
        return true;
    }

    public boolean continueUpload() {
        Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Continue activeUsers upload");

        boolean hasTask = false;
        if (null != uploadList) {
            synchronized (uploadList) {
                for (UploadElement element : uploadList) {
                    element.setOffset(element.getLength());
                    if (element.getState() == TransferState.START) {
                        hasTask = true;
                    } else {
                        element.setState(TransferState.WAIT);
                    }
                }

                if (!hasTask) { // notify new task if needs
                    synchronized (uploadList) {
                        uploadList.notify();
                    }
                }
            }
        }

        return true;
    }

    /**
     * Cancel uploads and remove them from the upload manager. Each upload will
     * be stopped if it was running, and it will no longer be accessible through
     * the upload manager. If there is a uploaded file, partial or complete, it
     * is deleted.
     *
     * @param filePath file full toPath at server, uniqueness
     * @return the id of upload actually removed, if remove failed, return -1.
     */
    public int removeUpload(String filePath) {
        UploadElement element = findElement(filePath);
        if (element != null) {
            if (element.getState() == TransferState.START) {
                if (handlerQueueThread != null) {
                    handlerQueueThread.stopCurrentUploadThread();
                }
            }
            if (uploadList.remove(element)) {
                synchronized (uploadList) {
                    uploadList.notify();
                }
                return element.hashCode();
            }
        }

        return -1;
    }

    public boolean removeUpload() {
        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
        }

        synchronized (uploadList) {
            uploadList.clear();
        }

        return true;
    }

    private UploadElement findElement(String filePath) {
        for (UploadElement element : uploadList) {
            if (element.getSrcPath().equals(filePath)) {
                return element;
            }
        }
        return null;
    }

    public ArrayList<UploadElement> getUploadList() {
        ArrayList<UploadElement> destList = new ArrayList<UploadElement>(Arrays.asList(new UploadElement[uploadList.size()]));
        synchronized (uploadList) {
            Collections.copy(destList, uploadList);
        }
        return destList;
    }

    public ArrayList<UploadElement> getCompleteList() {
        ArrayList<UploadElement> destList = new ArrayList<UploadElement>(Arrays.asList(new UploadElement[completeList.size()]));
        synchronized (completeList) {
            Collections.copy(destList, completeList);
        }
        return destList;
    }

    public void destroy() {
        handlerQueueThread.stopThread();
    }

    private static class HandlerQueueThread extends Thread {
        private List<UploadElement> mUploadList;
        private UploadFileThread uploadThread = null;
        private boolean isRunning = false;
        private boolean hasUploadTask = false;
        private UploadFileThread.OnUploadResultListener listener;

        public HandlerQueueThread(List<UploadElement> uploadList, UploadFileThread.OnUploadResultListener listener) {
            this.mUploadList = uploadList;
            this.listener = listener;
        }

        @Override
        public synchronized void start() {
            if (!isRunning) {
                isRunning = true;
                super.start();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                if (hasUploadTask) {
                    synchronized (this) {
                        try {
                            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "waiting for upload task stop.");
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "waiting for upload list is changed.");
                    synchronized (mUploadList) {
                        mUploadList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (mUploadList) {
                    for (UploadElement element : mUploadList) {
                        if (element.getState() == TransferState.WAIT) {
                            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "start upload task");
                            hasUploadTask = true;
                            uploadThread = new UploadFileThread(element, LoginManage.getInstance().getLoginSession(), listener);
                            uploadThread.start();
                            break;
                        }
                    }
                }
            }
        }

        /**
         * stop current upload thread
         */
        private synchronized void stopCurrentUploadThread() {
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "stop current upload thread");
            if (uploadThread != null) {
                uploadThread.stopUpload();
                uploadThread = null;
            }

        }

        /**
         * stop current upload task, called when current upload thread over,
         * before remove upload list
         */
        public synchronized void stopCurrentUploadTask() {
            hasUploadTask = false;
            synchronized (this) {
                Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "notify new upload task: " + this.getClass().getSimpleName());
                this.notify();
            }
        }

        /**
         * notified to start a new upload task, called after upload thread over
         * and upload list removed
         */
        public synchronized void notifyNewUploadTask() {

            synchronized (mUploadList) {
                mUploadList.notify();
                Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "notify upload list");
            }

        }

        public void stopThread() {
            isRunning = false;
            stopCurrentUploadThread();
            interrupt();
        }
    }

    public interface OnUploadCompleteListener {
        void uploadComplete(UploadElement element);
    }
}
