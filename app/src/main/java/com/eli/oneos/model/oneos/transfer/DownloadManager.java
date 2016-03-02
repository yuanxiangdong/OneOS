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

/**
 * download manager, please call method getInstance get singleton instance
 */
public class DownloadManager {
    private static final String LOG_TAG = DownloadManager.class.getSimpleName();

    private List<DownloadElement> completeList = Collections.synchronizedList(new ArrayList<DownloadElement>());
    private List<DownloadElement> downloadList = Collections.synchronizedList(new ArrayList<DownloadElement>());
    //    private DBManager dbManager = null;
    private OnDownloadCompleteListener mCompleteListener = null;
    private DownloadFileThread.OnDownloadResultListener mDownloadResultListener = new DownloadFileThread.OnDownloadResultListener() {

        @Override
        public void onResult(DownloadElement mElement) {
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Download Result: " + mElement.getState());

            handlerQueueThread.stopCurrentDownloadTask();

            synchronized (downloadList) {
                mElement.setTime(System.currentTimeMillis());
                TransferState state = mElement.getState();
                if (state == TransferState.COMPLETE) {
                    long uid = LoginManage.getInstance().getLoginSession().getUserInfo().getId();
                    TransferHistory history = new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(true), mElement.getSrcName(),
                            mElement.getSrcPath(), mElement.getTargetPath(), mElement.getSize(), 0L, System.currentTimeMillis());
                    TransferHistoryKeeper.insert(history);

                    if (mCompleteListener != null) {
                        mCompleteListener.downloadComplete(mElement);
                    }
                    downloadList.remove(mElement);
                } else {
                    Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, LOG_TAG, "Download Exception: " + state);
                }
            }

            try {
                Thread.sleep(10); // sleep 10ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handlerQueueThread.notifyNewDownloadTask();
        }
    };

    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(downloadList, mDownloadResultListener);
    private static DownloadManager INSTANCE = new DownloadManager();

    private DownloadManager() {
//        if (dbManager == null) {
//            dbManager = new DBManager();
//        }
        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }
    }

    /**
     * Singleton instance method
     *
     * @return singleton instance of class
     */
    public static DownloadManager getInstance() {
        return INSTANCE;
    }

    public void setOnDownloadCompleteListener(OnDownloadCompleteListener listener) {
        mCompleteListener = listener;
    }

    /**
     * Enqueue a new download. The download will start automatically once the download manager is
     * ready to execute it and connectivity is available.
     *
     * @param element the parameters specifying this download
     * @return an ID for the download, unique across the system. This ID is used to make future
     * calls related to this download. If enqueue failed, return -1.
     */
    public synchronized int enqueue(DownloadElement element) {
        if (element == null) {
            Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, LOG_TAG, "Download element is null");
            return -1;
        }

        if (downloadList.add(element)) {
            synchronized (downloadList) {
                downloadList.notify();
            }
            return element.hashCode();
        } else {
            return -1;
        }
    }

    /**
     * Cancel downloads and remove them from the download manager. Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager. If there is
     * a downloaded file, partial or complete, it is deleted.
     *
     * @param fullName file full targetPath at server, uniqueness
     * @return the id of download actually removed, if remove failed, return -1.
     */
    public int removeDownload(String fullName) {
        DownloadElement element = findElement(fullName);
        if (element != null) {
            boolean isElementStart = element.getState() == TransferState.START ? true : false;
            if (isElementStart && handlerQueueThread != null) {
                handlerQueueThread.stopCurrentDownloadThread();
            }
            if (downloadList.remove(element)) {
                if (isElementStart) {
                    synchronized (downloadList) {
                        downloadList.notify();
                    }
                }
                return element.hashCode();
            }
        }

        return -1;
    }

    public boolean removeDownload() {
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Remove activeUsers download");

        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        synchronized (downloadList) {
            downloadList.clear();
        }

        return true;
    }

    public boolean continueDownload(String fullName) {
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Continue download: " + fullName);

        DownloadElement element = findElement(fullName);
        if (element == null) {
            return false;
        }

        element.setState(TransferState.WAIT);
        synchronized (downloadList) {
            downloadList.notify();
        }
        return true;
    }

    public boolean continueDownload() {
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Continue activeUsers downloads");

        boolean hasTask = false;
        if (null != downloadList) {
            synchronized (downloadList) {
                for (DownloadElement element : downloadList) {
                    element.setOffset(element.getLength());
                    if (element.getState() == TransferState.START) {
                        hasTask = true;
                    } else {
                        element.setState(TransferState.WAIT);
                    }
                }

                if (!hasTask) { // notify new task if needs
                    synchronized (downloadList) {
                        downloadList.notify();
                    }
                }
            }
        }

        return true;
    }

    public boolean pauseDownload(String fullName) {
        DownloadElement element = findElement(fullName);

        if (element == null) {
            return false;
        }

        boolean isElementStart = (element.getState() == TransferState.START) ? true : false;
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Pause download: " + fullName + "; state: " + element.getState());
        if (isElementStart && handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        element.setOffset(element.getLength());
        element.setState(TransferState.PAUSE);
        if (isElementStart) {
            synchronized (downloadList) {
                downloadList.notify();
            }
        }
        return true;
    }

    public boolean pauseDownload() {
        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        if (null != downloadList) {
            synchronized (downloadList) {
                for (DownloadElement element : downloadList) {
                    element.setOffset(element.getLength());
                    element.setState(TransferState.PAUSE);
                }
            }
        }

        return true;
    }

    public ArrayList<DownloadElement> getDownloadList() {
        ArrayList<DownloadElement> destList = new ArrayList<DownloadElement>(
                Arrays.asList(new DownloadElement[downloadList.size()]));
        synchronized (downloadList) {
            Collections.copy(destList, downloadList);
        }
        return destList;
    }

    public ArrayList<DownloadElement> getCompleteList() {
        ArrayList<DownloadElement> destList = new ArrayList<DownloadElement>(Arrays.asList(new DownloadElement[completeList.size()]));
        synchronized (completeList) {
            Collections.copy(destList, completeList);
        }
        return destList;
    }

    private DownloadElement findElement(String fullName) {
        for (DownloadElement element : downloadList) {
            if (element.getSrcPath().equals(fullName)) {
                return element;
            }
        }
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, LOG_TAG, "Can't find element: " + fullName);
        return null;
    }

    public void destroy() {
        handlerQueueThread.stopThread();
    }

    private static class HandlerQueueThread extends Thread {
        private static final String TAG = "HandlerQueueThread";

        private List<DownloadElement> mDownloadList = null;
        private DownloadFileThread downloadThread = null;
        private boolean isRunning = false;
        private boolean hasDownloadTask = false;
        private DownloadFileThread.OnDownloadResultListener listener = null;

        public HandlerQueueThread(List<DownloadElement> mDownloadList, DownloadFileThread.OnDownloadResultListener listener) {
            this.mDownloadList = mDownloadList;
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

                if (hasDownloadTask) {
                    synchronized (this) {
                        try {
                            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "----waiting for download task stop----: " + this.getClass().getSimpleName());
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "-----waiting for download list is changed----");
                    synchronized (mDownloadList) {
                        mDownloadList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mDownloadList) {
                    for (DownloadElement element : mDownloadList) {
                        if (element.getState() == TransferState.WAIT) {
                            hasDownloadTask = true;
                            downloadThread = new DownloadFileThread(element, LoginManage.getInstance().getLoginSession(), listener);
                            downloadThread.start();
                            element.setState(TransferState.START);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * stop current download thread
         */
        private synchronized void stopCurrentDownloadThread() {
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Stop current download thread");
            if (downloadThread != null) {
                downloadThread.stopDownload();
                downloadThread = null;
            }
        }

        /**
         * stop current download task, called when current download thread over, before remove
         * download list
         */
        public synchronized void stopCurrentDownloadTask() {
            hasDownloadTask = false;
            synchronized (this) {
                Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Notify new download task: " + this.getClass().getSimpleName());
                this.notify();
            }
        }

        /**
         * notified to start a new download task, called after download thread over and download
         * list removed
         */
        public synchronized void notifyNewDownloadTask() {

            synchronized (mDownloadList) {
                mDownloadList.notify();
                Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Notify download list");
            }
        }

        public void stopThread() {
            isRunning = false;
            stopCurrentDownloadThread();
            interrupt();
        }
    }

    public interface OnDownloadCompleteListener {
        void downloadComplete(DownloadElement element);
    }
}
