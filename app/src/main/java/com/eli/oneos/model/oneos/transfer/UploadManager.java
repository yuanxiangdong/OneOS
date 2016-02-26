package com.eli.oneos.model.oneos.transfer;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class UploadManager {
    private static final String TAG = "UploadManager";
    private static final int HTTP_UPLOAD_RETRY_TIMES = 5;
    private static final int HTTP_BUFFER_SIZE = 1024 * 8;
    /**
     * chuck block size: 5mb
     */
    private static final int HTTP_BLOCK_SIZE = 1024 * 1024 * 4;

    private List<UploadElement> completeList = Collections.synchronizedList(new ArrayList<UploadElement>());
    private List<UploadElement> uploadList = Collections.synchronizedList(new ArrayList<UploadElement>());
    private OnUploadCompleteListener mCompleteListener = null;
    private static UploadManager instance = new UploadManager();
    //    private DBManager dbManager = null;
    private OnUploadResultListener uploadResultListener = new OnUploadResultListener() {

        @Override
        public void uploadResult(String filePath, TransferState state) {
            handlerQueueThread.stopCurrentUploadTask();

            synchronized (uploadList) { // TODO.. confirm !!
                Iterator<UploadElement> iterator = uploadList.iterator();
                while (iterator.hasNext()) {
                    UploadElement element = iterator.next();
                    if (element.getSrcPath().equals(filePath)) {
                        element.setTime(System.currentTimeMillis());
                        element.setState(state);
                        if (state == TransferState.COMPLETE) {
                            Log.d(TAG, "upload complete");
                            if (mCompleteListener != null) {
                                String user = LoginManage.getInstance().getLoginSession().getUserInfo().getName();
                                Log.e(TAG, "TODO... insert transfer recode..");
//                                dbManager.insertTransferRecord(element, user);

                                mCompleteListener.uploadComplete(element);
                            }
                            completeList.add(element);
                            iterator.remove();

                            Log.d(TAG, "upload complete");
                        } else {
                            Log.e(TAG, "upload pause or failure");
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

            handlerQueueThread.notifyNewUploadTask();
        }
    };
    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(uploadList, uploadResultListener);

    private UploadManager() {
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
    public static UploadManager getInstance() {
        return instance;
    }

    public void setOnUploadCompleteListener(OnUploadCompleteListener listener) {
        mCompleteListener = listener;
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
            Log.e(TAG, "upload element is null");
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
        Log.i(TAG, "Pause upload: " + filePath + "; state: " + element.getState());
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
     * pause all upload
     *
     * @return
     */
    public boolean pauseUpload() {
        Log.i(TAG, "Pause all upload");

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
        Log.i(TAG, "Continue upload: " + filepath);

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
        Log.i(TAG, "Continue all upload");

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
     * @param filePath file full targetPath at server, uniqueness
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
        // private List<UploadElement> mCompletelist;
        private List<UploadElement> mUploadlist;
        private UploadThread uploadThread = null;
        private boolean isRunning = false;
        private boolean hasUploadTask = false;
        private OnUploadResultListener listener = null;

        public HandlerQueueThread(List<UploadElement> mUploadlist, OnUploadResultListener listener) {
            this.mUploadlist = mUploadlist;
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
                            Log.d(TAG, "waiting for upload task stop.");
                            wait();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Log.d(TAG, "waiting for upload list is changed.");
                    synchronized (mUploadlist) {
                        mUploadlist.wait();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                synchronized (mUploadlist) { // TODO.. confirm !!
                    for (UploadElement element : mUploadlist) {
                        if (element.getState() == TransferState.WAIT) {
                            Log.d(TAG, "start upload task");
                            hasUploadTask = true;
                            uploadThread = new UploadThread(element, listener);
                            uploadThread.start();
                            break;
                        }
                    }
                }
            }
        }

        // private void stopCurrentUploadTask() {
        // if (uploadThread != null) {
        // uploadThread.stopUpload();
        // uploadThread = null;
        // }
        //
        // hasUploadTask = false;
        // synchronized (this) {
        // notify();
        // }
        // }

        /**
         * stop current upload thread
         */
        private synchronized void stopCurrentUploadThread() {
            Log.d(TAG, "stop current upload thread");
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
                Log.d(TAG, "notify new upload task: " + this.getClass().getSimpleName());
                this.notify();
            }
        }

        /**
         * notified to start a new upload task, called after upload thread over
         * and upload list removed
         */
        public synchronized void notifyNewUploadTask() {

            synchronized (mUploadlist) {
                mUploadlist.notify();
                Log.d(TAG, "notify upload list");
            }

        }

        public void stopThread() {
            isRunning = false;
            stopCurrentUploadThread();
            interrupt();
        }
    }

    /**
     * The thread for upload file from server, based on HTTP or Socket.
     */
    private static class UploadThread extends Thread /* implements ProgressListener */ {
        private static final String LOG_TAG = UploadThread.class.getSimpleName();
        private UploadElement mElement;
        private OnUploadResultListener mListener = null;
        // private MultiEntity mMultiEntity;
        private boolean isInterrupt = false;

        public UploadThread(UploadElement element, OnUploadResultListener mListener) {
            if (mListener == null) {
                throw new NullPointerException("UploadThread: Upload Result StatusBarListener is NULL");
            }
            this.mElement = element;
            this.mListener = mListener;
        }

        @Override
        public void run() {
            httpPost();
            // uploadSocket();
            mListener.uploadResult(mElement.getSrcPath(), mElement.getState());
        }

        private void uploadSocket() {
            mElement.setState(TransferState.START);

            LoginSession loginSession = LoginManage.getInstance().getLoginSession();
            if (loginSession == null) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.UNKNOWN_EXCEPTION);
                Log.e(LOG_TAG, "user info is null");
                return;
            }

            String session = loginSession.getSession();
            String filePath = mElement.getSrcPath();
            String savePath = mElement.getTargetPath();
            String ip = loginSession.getDeviceInfo().getIp();

            long md5 = 0;
            long thumbnail = 0;

            long userFreeSpace = -1;
            if (mElement.isUploadToPrivateDir()) {
                if (session == null || savePath == null) {
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                    Log.e(LOG_TAG, "session is null");
                    return;
                }

                try {
                    userFreeSpace = getUserServerFreeSpace(session, loginSession.getUserInfo().getUid(), loginSession.getBaseUrl());
                    if (userFreeSpace < 0) {
                        mElement.setState(TransferState.FAILED);
                        if (userFreeSpace == -1) {
                            mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                        } else {
                            mElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.ENCODING_EXCEPTION);
                } catch (IOException e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                } catch (Exception e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.UNKNOWN_EXCEPTION);
                }

            }

            File uploadFile = new File(filePath);
            if (!uploadFile.exists()) {
                Log.e(LOG_TAG, "upload file is not exist");
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FILE_NOT_FOUND);
                return;
            }
            savePath += uploadFile.getName();
            long fileLength = uploadFile.length();
            long uploadPosition = 0;
            try {
                uploadPosition = getUploadPosition(savePath, session, fileLength, ip, md5, thumbnail);
            } catch (IOException e) {
                e.printStackTrace();
                // continue to upload file from the beginning
            }

            if (mElement.isUploadToPrivateDir() && (userFreeSpace <= fileLength - uploadPosition)) {
                Log.e(TAG, "File Length = " + fileLength + " ; Disk space = " + userFreeSpace);
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                return;
            }

            // Modified to new position, to make sure anim_progress is correct
            mElement.setLength(uploadPosition);
            mElement.setOffset(uploadPosition);
//            mElement.setTotalFileLength(fileLength);

            // --------- Use socket to upload files -----------
            Socket socket = new Socket();
            RandomAccessFile fileReader = null;
            OutputStream outStream = null;
            BufferedReader bReader = null;
            try {
                // savePath = android.net.Uri.encode(savePath);
                String head = "CMD:upload\r\nSESSION:" + session + "\r\nPOSITION:" + uploadPosition + "\r\nLENGTH:" + fileLength + "\r\nFULLNAME:" + savePath
                        + "\r\nMD5:" + md5 + "\r\nTHUMBNAIL:" + thumbnail + "\r\n";
                Log.e("HEAD", "---------------upload---------------");
                Log.d("HEAD", head);
                Log.e("HEAD", "------------------------------------");

                Log.e(TAG, "====Upload IP: " + ip);
                socket.connect(new InetSocketAddress(ip, OneOSAPIs.OneOS_UPLOAD_SOCKET_PORT), 10000);
                fileReader = new RandomAccessFile(mElement.getSrcPath(), "r");
                fileReader.seek(mElement.getOffset());
                outStream = socket.getOutputStream();
                outStream.write(head.getBytes());
                byte[] buffer = new byte[HTTP_BUFFER_SIZE];
                int read = 0;
                long curLength = mElement.getOffset();
                while (!isInterrupt && (read = fileReader.read(buffer, 0, buffer.length)) != -1) {
                    outStream.write(buffer, 0, read);
                    curLength += read;
                    mElement.setLength(curLength);
                }
                outStream.flush();

                if (isInterrupt) {
                    mElement.setState(TransferState.PAUSE);
                } else {
                    if (curLength == mElement.getLength()) {
                        Log.d(LOG_TAG, "---------get response------");
                        bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String result = bReader.readLine();
                        Log.d(LOG_TAG, "---get upload result: " + result);
                        if (result.indexOf("upload") != -1) {
                            mElement.setState(TransferState.COMPLETE);
                        } else {
                            Log.e(LOG_TAG, "result = " + result);
                        }
                    } else {
                        mElement.setState(TransferState.FAILED);
                        mElement.setException(TransferException.UNKNOWN_EXCEPTION);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FILE_NOT_FOUND);
            } catch (SocketException e) {
                e.printStackTrace();
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FAILED_REQUEST_SERVER);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FAILED_REQUEST_SERVER);
            } catch (IOException e) {
                e.printStackTrace();
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.IO_EXCEPTION);
            } catch (Exception e) {
                e.printStackTrace();
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.UNKNOWN_EXCEPTION);
            } finally {
                try {
                    if (fileReader != null) {
                        fileReader.close();
                    }
                    if (outStream != null) {
                        outStream.close();
                    }
                    if (bReader != null) {
                        bReader.close();
                    }
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "close stream exception");
                }
            }
        }

        public void httpPost() {
            mElement.setState(TransferState.START);
            LoginSession loginSession = LoginManage.getInstance().getLoginSession();
            if (loginSession == null) {
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.UNKNOWN_EXCEPTION);
                Log.e(LOG_TAG, "user info is null");
                return;
            }

            String session = loginSession.getSession();
            String filePath = mElement.getSrcPath();
            String savePath = mElement.getTargetPath();
            // String ip = userInfo.getRealIP();
            // long md5 = 0;
            // long thumbnail = 0;

            long userFreeSpace = -1;
            if (mElement.isUploadToPrivateDir()) {
                if (session == null || savePath == null) {
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                    Log.e(LOG_TAG, "session is null");
                    return;
                }

                try {
                    userFreeSpace = getUserServerFreeSpace(session, loginSession.getUserInfo().getUid(), loginSession.getBaseUrl());
                    if (userFreeSpace < 0) {
                        mElement.setState(TransferState.FAILED);
                        if (userFreeSpace == -1) {
                            mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                        } else {
                            mElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.ENCODING_EXCEPTION);
                } catch (IOException e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                } catch (Exception e) {
                    e.printStackTrace();
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.UNKNOWN_EXCEPTION);
                }
            }

            File uploadFile = new File(filePath);
            if (!uploadFile.exists()) {
                Log.e(LOG_TAG, "upload file is not exist");
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.FILE_NOT_FOUND);
                return;
            }
            // savePath += uploadFile.getTargetPath();
            long fileLen = uploadFile.length();
            long uploadPosition = 0;
            // try {
            // uploadPosition = getUploadPosition(savePath, session, fileLength,
            // ip, md5, thumbnail);
            // } catch (IOException e) {
            // e.printStackTrace();
            // // continue to upload file from the beginning
            // }

            if (mElement.isUploadToPrivateDir() && (userFreeSpace <= fileLen - uploadPosition)) {
                Log.e(TAG, "File Length = " + fileLen + " ; Disk space = " + userFreeSpace);
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                return;
            }

            // Modified to new position, to make sure anim_progress is correct
            mElement.setLength(uploadPosition);
            mElement.setOffset(uploadPosition);
//            mElement.setTotalFileLength(fileLen);
            Log.e(TAG, "======== Upload start, url:" + mElement.getUrl());

            String PREFIX = "--";
            String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
            String LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data"; // 内容类型

            long retry = 0; // exception retry times
            long uploadLen = 0;
            int chunkNum = (int) Math.ceil((double) fileLen / (double) HTTP_BLOCK_SIZE);
            int chunkIndex = 0;
            for (chunkIndex = 0; chunkIndex < chunkNum; chunkIndex++) {
                Log.d(TAG, "=====>>> BlockIndex:" + chunkIndex + ", BlockNum:" + chunkNum + ", BlockSize:" + HTTP_BLOCK_SIZE);
                long blockUpLen = 0;
                try {
                    URL url = new URL(mElement.getUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(20000);
                    conn.setConnectTimeout(20000);
                    conn.setDoInput(true); // 允许输入流
                    conn.setDoOutput(true); // 允许输出流
                    conn.setUseCaches(false); // 不允许使用缓存
                    conn.setRequestMethod("POST"); // 请求方式
                    conn.setRequestProperty("Charset", HTTP.UTF_8); // 设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                    // conn.connect();

                    DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                    StringBuffer sb = new StringBuffer();

                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"session\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(session);
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"savepath\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(savePath);
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"chunks\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(chunkNum);
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"chunk\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(chunkIndex);
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"name\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(uploadFile.getName());
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadFile.getName() + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream;charset=" + HTTP.UTF_8 + LINE_END);
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();

                    RandomAccessFile inputStream = new RandomAccessFile(uploadFile, "r");
                    inputStream.seek(chunkIndex * HTTP_BLOCK_SIZE);
                    byte[] bytes = new byte[HTTP_BUFFER_SIZE];
                    int len = 0;
                    while (!isInterrupt && (len = inputStream.read(bytes)) != -1) {
                        outStream.write(bytes, 0, len);
                        blockUpLen += len;
                        uploadLen += len;
                        mElement.setLength(uploadLen);
                        if (blockUpLen >= HTTP_BLOCK_SIZE) {
                            break;
                        }
                    }
                    inputStream.close();

                    if (!isInterrupt) {
                        outStream.write(LINE_END.getBytes());
                        byte[] end = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                        outStream.write(end);
                        outStream.flush();
                        outStream.close();

                        int code = conn.getResponseCode();
                        if (code != HttpURLConnection.HTTP_OK) {
                            Log.e(TAG, "Http Response Error, code = " + code);
                            // mElement.setState(TransferState.FAILED);
                            mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                            // return;
                            retry++;
                        } else {
                            retry = 0;
                        }
                    } else {
                        outStream.close();
                        Log.d(TAG, "======== Stop upload");
                        break;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    retry++;
                    // mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FAILED_REQUEST_SERVER);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    retry++;
                    // mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.FILE_NOT_FOUND);
                } catch (IOException e) {
                    e.printStackTrace();
                    retry++;
                    // mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.IO_EXCEPTION);
                } finally {
                    if (!isInterrupt && retry > 0) {
                        try {
                            chunkIndex--;
                            uploadLen -= blockUpLen;
                            mElement.setLength(uploadLen);
                            sleep(retry * retry * 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!isInterrupt && retry > HTTP_UPLOAD_RETRY_TIMES) {
                        Log.w(TAG, "=====>>>Upload exception, retry " + HTTP_UPLOAD_RETRY_TIMES + " times, exit ...");
                        break;
                    }
                }
            }

            Log.d(TAG, "========Upload Over; FileLen = " + fileLen + ", UploadLen = " + uploadLen);
            if (fileLen == uploadLen) {
                mElement.setState(TransferState.COMPLETE);
            } else {
                if (isInterrupt) {
                    mElement.setState(TransferState.PAUSE);
                } else {
                    mElement.setState(TransferState.FAILED);
                }
            }
        }

        /**
         * stop for HTTP upload
         */
        public void stopUpload() {
            // try {
            isInterrupt = true;
            mElement.setState(TransferState.PAUSE);
            // if (mMultiEntity != null) {
            // mMultiEntity.cancelUpload(true);
            // }
            // } catch (Exception e) {
            // e.getStackTrace();
            // Logged.e(TAG, "stop upload exception");
            // }
        }

        /**
         * stop for TCP upload
         */
        // public void stopUpload() {
        // isInterrupt = true;
        // mElement.setState(TransferState.PAUSE);
        // Logged.d(LOG_TAG, "stop upload");
        // }

        /**
         * get upload position from server, for continue to upload form
         * breakpoint, it should be checked before upload
         *
         * @param fullName
         * @param session
         * @param fileLength
         * @param ip
         * @return breakpoint position
         * @throws IOException
         */
        private long getUploadPosition(String fullName, String session, long fileLength, String ip, long MD5, long thumbnail) throws IOException {
            long position = 0;
            // fullName = android.net.Uri.encode(fullName);

            Socket socket = new Socket();
            OutputStream socketOutStream = null;
            BufferedReader bReader = null;
            String head = "CMD:check\r\nSESSION:" + session + "\r\nPOSITION:0\r\nLENGTH:" + fileLength + "\r\nFULLNAME:" + fullName + "\r\nMD5:" + MD5
                    + "\r\nTHUMBNAIL:" + thumbnail + "\r\n";
            Log.e("HEAD", "---------------check----------------");
            Log.d("HEAD", head);
            Log.e("HEAD", "------------------------------------");
            socket.connect(new InetSocketAddress(ip, OneOSAPIs.OneOS_UPLOAD_SOCKET_PORT), 5000);
            socketOutStream = socket.getOutputStream();
            socketOutStream.write(head.getBytes());

            String result = null;
            bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed()) {
                result = bReader.readLine();
                if (result == null) {
                    break;
                }

                if (result.indexOf("POSITION:") != -1) {
                    Log.e("HEAD", "-----------check result-------------");
                    Log.d("HEAD", result);
                    Log.e("HEAD", "------------------------------------");
                    result = result.trim();
                    try {
                        position = Long.valueOf(result.substring("POSITION:".length()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    Log.d(LOG_TAG, "get breakpoint: " + result);
                    break;
                }
            }

            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "close stream exception");
            }

            return position;
        }

        /**
         * get user server free space from server
         *
         * @param session
         * @param userId
         * @param baseUrl
         * @return user free space
         * @throws ClientProtocolException
         * @throws IOException
         * @throws JSONException
         */
        private long getUserServerFreeSpace(String session, long userId, String baseUrl) throws ClientProtocolException, IOException, JSONException {
            long userFreeSpace = 0;

            // --------- Query server storage space whether enough to upload
            // file -----------
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("session", session));
            params.add(new BasicNameValuePair("uid", String.valueOf(userId)));
            HttpPost httpRequest = new HttpPost(baseUrl + OneOSAPIs.USER_MANAGE);
            DefaultHttpClient client = new DefaultHttpClient();
            // httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
            // 5000);
            client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = client.execute(httpRequest);
            String userSpaceStr = null;
            if (response.getStatusLine().getStatusCode() == 200) {
                userSpaceStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObj = new JSONObject(userSpaceStr);
                boolean isRequested = jsonObj.getBoolean("result");
                if (isRequested) {
                    long totalSize = jsonObj.getLong("total") * 1024 * 1024 * 1024;
                    userFreeSpace = totalSize - jsonObj.getLong("used");
                } else {
                    userFreeSpace = -1;
                }
            } else {
                userFreeSpace = -1;
                throw new ClientProtocolException();
            }

            return userFreeSpace;
        }

        // @Override
        // public void transferred(long num) {
        // mElement.setLength(num);
        // if (mElement.getLength() == num) {
        // mElement.setState(TransferState.COMPLETE);
        // }
        // }
    }

    private interface OnUploadResultListener {
        void uploadResult(String filePath, TransferState state);
    }

    public interface OnUploadCompleteListener {
        void uploadComplete(UploadElement element);
    }
}
