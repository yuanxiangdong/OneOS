package com.eli.oneos.model.oneos.trans;

import android.content.Context;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.SDCardUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * download manager, please call method getInstance get singleton instance
 */
public class DownloadManager {
    private static final String LOG_TAG = DownloadManager.class.getSimpleName();
    private static final int HTTP_BUFFER_SIZE = 1024 * 16;

    private List<DownloadElement> completeList = Collections.synchronizedList(new ArrayList<DownloadElement>());
    private List<DownloadElement> downloadList = Collections.synchronizedList(new ArrayList<DownloadElement>());
    //    private DBManager dbManager = null;
    private OnDownloadCompleteListener mCompleteListener = null;
    private OnDownloadResultListener mDownloadResultListener = new OnDownloadResultListener() {

        @Override
        public void downloadResult(String fullName, TransferState state) {
            handlerQueueThread.stopCurrentDownloadTask();

            synchronized (downloadList) {
                Iterator<DownloadElement> iterator = downloadList.iterator();
                while (iterator.hasNext()) {
                    DownloadElement element = iterator.next();
                    if (element.getSrcPath().equals(fullName)) {
                        element.setTime(System.currentTimeMillis());
                        element.setState(state);
                        if (state == TransferState.COMPLETE) {
                            completeList.add(element);
                            if (mCompleteListener != null) {
                                String user = LoginManage.getInstance().getLoginSession().getUserInfo().getName();
                                Log.e(LOG_TAG, "TODO... insert transfer recode..");
//                                dbManager.insertTransferRecord(element, user);

                                mCompleteListener.downloadComplete(element);
                            }
                            iterator.remove();
                            Log.d(LOG_TAG, "download complete");
                        } else {
                            Log.e(LOG_TAG, "download pause or failure");
                        }
                        break;
                    }
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

    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(downloadList,
            mDownloadResultListener);
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
            Log.e(LOG_TAG, "element is null");
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
        Log.i(LOG_TAG, "Remove all download");

        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        synchronized (downloadList) {
            downloadList.clear();
        }

        return true;
    }

    public boolean continueDownload(String fullName) {
        Log.i(LOG_TAG, "Continue download: " + fullName);

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
        Log.i(LOG_TAG, "Continue all download");

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
        Log.i(LOG_TAG, "Pause download: " + fullName + "; state: " + element.getState());
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
        Log.e(LOG_TAG, "Can't find this element, fullname=" + fullName);
        return null;
    }

    public void destroy() {
        handlerQueueThread.stopThread();
    }

    private static class HandlerQueueThread extends Thread {
        private static final String LOG_TAG = "HandlerQueueThread";

        private List<DownloadElement> mDownloadList = null;
        private DownloadThread downloadThread = null;
        private boolean isRunning = false;
        private boolean hasDownloadTask = false;
        private OnDownloadResultListener listener = null;

        public HandlerQueueThread(List<DownloadElement> mDownloadlist, OnDownloadResultListener listener) {
            this.mDownloadList = mDownloadlist;
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
                            Log.d(LOG_TAG, "----waiting for download task stop----: " + this.getClass().getSimpleName());
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Log.d(LOG_TAG, "-----waiting for download list is changed----");
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
                            downloadThread = new DownloadThread(element, listener);
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
            Log.d(LOG_TAG, "stop current download thread");
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
                Log.d(LOG_TAG, "notify new download task: " + this.getClass().getSimpleName());
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
                Log.d(LOG_TAG, "notify download list");
            }

        }

        public void stopThread() {
            isRunning = false;
            stopCurrentDownloadThread();
            interrupt();
        }

    }

    /**
     * The thread for download file from server, base on HTTP.
     */
    private static class DownloadThread extends Thread {
        private static final String LOG_TAG = DownloadThread.class.getSimpleName();

        private boolean isInterrupt = false;
        private DownloadElement mElement;
        private OnDownloadResultListener mListener = null;

        public DownloadThread(DownloadElement element, OnDownloadResultListener mListener) {
            if (mListener == null) {
                throw new NullPointerException("DownloadThread: DownloadResultListener is NULL");
            }
            this.mElement = element;
            this.mListener = mListener;
        }

        @Override
        public void run() {
            // httpPostDownload();
            httpGetDownload();
            Log.d(LOG_TAG, "download over");
            mListener.downloadResult(mElement.getSrcPath(), mElement.getState());
        }

        private void httpGetDownload() {
            // set element download state to start
            mElement.setState(TransferState.START);
            isInterrupt = false;

            LoginSession loginSession = LoginManage.getInstance().getLoginSession();
            if (loginSession == null) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.REQUEST_SERVER);
                Log.e(LOG_TAG, "session is null");
                return;
            }

            String session = loginSession.getSession();
            try {
                String url = OneOSAPIs.genDownloadUrl(loginSession, mElement.getFile());
                HttpGet httpGet = new HttpGet(url);
                Log.d(LOG_TAG, "Download file: " + url);
                if (mElement.getOffset() < 0) {
                    Log.w(LOG_TAG, "error position, position must greater than or equal zero");
                    mElement.setOffset(0);
                }
                httpGet.setHeader("Cookie", "session=" + session);

                if (mElement.getOffset() > 0) {
                    httpGet.setHeader("Range", "bytes=" + String.valueOf(mElement.getOffset()) + "-");
                }
                HttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity entity = httpResponse.getEntity();
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code != 200 && code != 206) {
                    Log.e(LOG_TAG, "ERROR: status code=" + code);
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.REQUEST_SERVER);
                    return;
                }

                long fileLength = entity.getContentLength();
                if (fileLength < 0) {
                    Log.e(LOG_TAG, "ERROR: content length=" + fileLength);
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.REQUEST_SERVER);
                    return;
                } else if (fileLength > SDCardUtils.getDeviceAvailableSize(mElement.getTargetPath())) {
                    Log.e(LOG_TAG, "SD Avaliable Size Insufficient");
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                    return;
                }
//                fileLength += mElement.getOffset();
//                mElement.setTotalFileLength(fileLength);

                saveData(entity.getContent(), httpClient);

            } catch (HttpHostConnectException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.REQUEST_SERVER);
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.ENCODING_EXCEPTION);
                e.printStackTrace();
            } catch (ConnectTimeoutException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.ENCODING_EXCEPTION);
                e.printStackTrace();
            } catch (IOException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.IO_EXCEPTION);
                e.printStackTrace();
            } catch (Exception e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.UNKNOW_EXCEPTION);
                e.printStackTrace();
            }
        }

        private void httpPostDownload() {
            Context context = MyApplication.getAppContext();
            LoginSession loginSession = LoginManage.getInstance().getLoginSession();
            String session = loginSession.getSession();
            String url = mElement.getUrl();
            String srcPath = mElement.getSrcPath();

            if (session == null) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.REQUEST_SERVER);
                Log.e(LOG_TAG, "session is null");
                return;
            }

            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("session", session));
            param.add(new BasicNameValuePair("srcPath", srcPath));

            try {
                HttpPost httpRequest = new HttpPost(url);
                if (mElement.getOffset() >= 0) {
                    httpRequest.setHeader("Range", "bytes=" + String.valueOf(mElement.getOffset()) + "-");
                } else if (mElement.getOffset() < 0) {
                    Log.e(LOG_TAG, "error position, position must greater than or equal zero");
                    return;
                }

                HttpClient httpClient = new DefaultHttpClient();
                // httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
                // 5000);
                httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);

                httpRequest.setEntity(new UrlEncodedFormEntity(param, HTTP.UTF_8));
                HttpResponse httpResponse = httpClient.execute(httpRequest);

                HttpEntity entity = httpResponse.getEntity();
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code != 200 && code != 206) {
                    Log.e(LOG_TAG, "ERROR: status code=" + code);
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.REQUEST_SERVER);
                    return;
                }
                long fileLength = entity.getContentLength();
                // Log.d(LOG_TAG, "download file length = " + fileLength);
                if (fileLength < 0) {
                    Log.e(LOG_TAG, "ERROR: content length=" + fileLength);
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.REQUEST_SERVER);
                    return;
                } else if (fileLength > SDCardUtils.getDeviceAvailableSize(mElement.getTargetPath())) {
                    Log.e(LOG_TAG, "Sd Avaliable Size Insufficient");
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                    return;
                }
                Header header = httpResponse.getFirstHeader("Content-Ranges");
                if (header != null) {
                    String contentRanges = header.getValue();
                    int last = contentRanges.lastIndexOf('/');
                    String totalString = contentRanges.substring(last + 1, contentRanges.length());
                    fileLength = Long.valueOf(totalString);
                    // Log.d(LOG_TAG,
                    // "header targetPath=" + header.getTargetPath() + ", value=" +
                    // header.getValue());
                }
//                mElement.setTotalFileLength(fileLength);

                // set element download state to start
                mElement.setState(TransferState.START);
                saveData(entity.getContent(), httpClient);

            } catch (HttpHostConnectException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.REQUEST_SERVER);
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.ENCODING_EXCEPTION);
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
            } catch (IOException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.IO_EXCEPTION);
                e.printStackTrace();
            } finally {
                if (mListener != null) {
                    Log.d(LOG_TAG, "download over");
                    mListener.downloadResult(mElement.getSrcPath(), mElement.getState());
                }
            }
        }

        private void saveData(InputStream input, HttpClient httpClient) {
            RandomAccessFile outputFile;
            long curFileLength = mElement.getOffset();
            try {
                String targetPath = mElement.getTargetPath() + File.separator + mElement.getFile().getName();
                File file = new File(targetPath);
                if (file.exists()) {
                    file.renameTo(new File((targetPath + System.currentTimeMillis())));
                }

                outputFile = new RandomAccessFile(targetPath, "rw");
                outputFile.seek(mElement.getOffset());
                // Log.d(LOG_TAG, "write position = " +
                // mElement.getOffset());
                byte[] buffer = new byte[HTTP_BUFFER_SIZE];
                int nRead = 0;
                while (!isInterrupt) {
                    nRead = input.read(buffer, 0, buffer.length);
                    if (nRead < 0) {
                        break;
                    }
                    outputFile.write(buffer, 0, nRead);
                    curFileLength += nRead;
                    mElement.setLength(curFileLength);
                }

                if (isInterrupt) {
                    mElement.setState(TransferState.PAUSE);
                    Log.d(LOG_TAG, "download interrupt");
                } else {
                    if (curFileLength != mElement.getSize()) {
                        Log.e(LOG_TAG, "download file length is not equals file real length");
                        mElement.setState(TransferState.FAILED);
                        mElement.setException(TransferException.UNKNOW_EXCEPTION);
                    } else {
                        mElement.setState(TransferState.COMPLETE);
                    }
                }

                httpClient.getConnectionManager().shutdown();
                Log.d(LOG_TAG, "shut down http connection");
            } catch (FileNotFoundException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FILE_NOT_FOUND);
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
            } catch (SocketException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
            } catch (IOException e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.IO_EXCEPTION);
                e.printStackTrace();
            } catch (Exception e) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.UNKNOW_EXCEPTION);
                e.printStackTrace();
            } finally {
                // if (mListener != null) {
                // mListener.downloadResult(mElement.getSrcPath(),
                // mElement.getState());
                // }
                // try {
                // if (input != null) {
                // input.close();
                // }
                // if (outputFile != null) {
                // outputFile.close();
                // }
                // } catch (IOException e) {
                // Log.e(LOG_TAG, "input/output closed error.");
                // e.printStackTrace();
                // }
            }
        }

        public void stopDownload() {
            isInterrupt = true;
            mElement.setState(TransferState.PAUSE);
            Log.d(LOG_TAG, "stop download");
        }

    }

    public interface OnDownloadResultListener {
        /**
         * Download over, the result is complete or failed
         */
        void downloadResult(String path, TransferState state);
    }

    public interface OnDownloadCompleteListener {
        /**
         * Download complete
         */
        void downloadComplete(DownloadElement element);
    }
}
