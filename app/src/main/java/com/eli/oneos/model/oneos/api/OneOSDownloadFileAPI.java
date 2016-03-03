package com.eli.oneos.model.oneos.api;

import android.content.Context;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
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
import java.util.List;

/**
 * OneSpace OS Download File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 */
public class OneOSDownloadFileAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSDownloadFileAPI.class.getSimpleName();
    private static final int HTTP_BUFFER_SIZE = 1024 * 16;

    private OnDownloadFileListener listener;
    private DownloadElement downloadElement;
    private boolean isInterrupt = false;
    private LoginSession loginSession;

    public OneOSDownloadFileAPI(LoginSession loginSession, DownloadElement element) {
        super(loginSession);
        this.loginSession = loginSession;
        this.downloadElement = element;
    }

    public void setOnDownloadFileListener(OnDownloadFileListener listener) {
        this.listener = listener;
    }

    public boolean download() {
        if (null != listener) {
            listener.onStart(url, downloadElement);
        }

        doDownload();

        if (null != listener) {
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "download over");
            listener.onComplete(url, downloadElement);
        }

        return downloadElement.getState() == TransferState.COMPLETE;
    }

    public void stopDownload() {
        isInterrupt = true;
        Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "Upload Stopped");
    }

    private void doDownload() {
        url = OneOSAPIs.genDownloadUrl(loginSession, downloadElement.getFile());

        // set element download state to start
        downloadElement.setState(TransferState.START);
        isInterrupt = false;
        String session = loginSession.getSession();
        try {
            HttpGet httpGet = new HttpGet(url);
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download file: " + url);
            if (downloadElement.getOffset() < 0) {
                Logger.p(LogLevel.WARN, Logged.DOWNLOAD, TAG, "error position, position must greater than or equal zero");
                downloadElement.setOffset(0);
            }
            httpGet.setHeader("Cookie", "session=" + session);

            if (downloadElement.getOffset() > 0) {
                httpGet.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
            }
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code != 200 && code != 206) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setState(TransferState.FAILED);
                if (code == 404) {
                    downloadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                } else {
                    downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                }
                return;
            }

            long fileLength = entity.getContentLength();
            if (fileLength < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (downloadElement.isCheck() && fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getTargetPath())) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "SD Available Size Insufficient");
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }
//                fileLength += downloadElement.getOffset();
//                downloadElement.setTotalFileLength(fileLength);

            saveData(entity.getContent(), httpClient);

        } catch (HttpHostConnectException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void httpPostDownload() {
        Context context = MyApplication.getAppContext();
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        String session = loginSession.getSession();
        String url = downloadElement.getUrl();
        String srcPath = downloadElement.getSrcPath();

        if (session == null) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Session is null");
            return;
        }

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("session", session));
        param.add(new BasicNameValuePair("srcPath", srcPath));

        try {
            HttpPost httpRequest = new HttpPost(url);
            if (downloadElement.getOffset() >= 0) {
                httpRequest.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
            } else if (downloadElement.getOffset() < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Error position, position must greater than or equal zero");
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
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            }
            long fileLength = entity.getContentLength();
            // Logged.d(LOG_TAG, "download file length = " + fileLength);
            if (fileLength < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getTargetPath())) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "SDCard Available Size Insufficient");
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }
            Header header = httpResponse.getFirstHeader("Content-Ranges");
            if (header != null) {
                String contentRanges = header.getValue();
                int last = contentRanges.lastIndexOf('/');
                String totalString = contentRanges.substring(last + 1, contentRanges.length());
                fileLength = Long.valueOf(totalString);
                // Logged.d(LOG_TAG,
                // "header targetPath=" + header.getTargetPath() + ", value=" +
                // header.getValue());
            }
//                downloadElement.setTotalFileLength(fileLength);

            // set element download state to start
            downloadElement.setState(TransferState.START);
            saveData(entity.getContent(), httpClient);

        } catch (HttpHostConnectException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void saveData(InputStream input, HttpClient httpClient) {
        RandomAccessFile outputFile = null;
        long curFileLength = downloadElement.getOffset();
        try {
            File dir = new File(downloadElement.getTargetPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String targetPath = downloadElement.getTargetPath() + File.separator + downloadElement.getFile().getName();
            File file = new File(targetPath);
            if (file.exists()) {
                String name = file.getName();
                String newName;
                int index = name.indexOf(".");
                if (index >= 0) {
                    String prefix = name.substring(0, index);
                    String suffix = name.substring(index, name.length());
                    newName = prefix + "-" + System.currentTimeMillis() + suffix;
                } else {
                    newName = name + "-" + System.currentTimeMillis();
                }

                file.renameTo(new File(downloadElement.getTargetPath(), newName));
            }

            outputFile = new RandomAccessFile(targetPath, "rw");
            outputFile.seek(downloadElement.getOffset());
            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int nRead;
            int callback = 0; // for download progress callback
            while (!isInterrupt) {
                nRead = input.read(buffer, 0, buffer.length);
                if (nRead < 0) {
                    break;
                }
                outputFile.write(buffer, 0, nRead);
                curFileLength += nRead;
                downloadElement.setLength(curFileLength);
                callback++;
                if (null != listener && callback == 32) {
                    // callback every 512KB
                    listener.onUploading(url, downloadElement);
                    callback = 0;
                }
            }

            if (isInterrupt) {
                downloadElement.setState(TransferState.PAUSE);
                Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download interrupt");
            } else {
                if (downloadElement.getSize() > 0 && curFileLength != downloadElement.getSize()) {
                    Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download file length is not equals file real length");
                    downloadElement.setState(TransferState.FAILED);
                    downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
                } else {
                    downloadElement.setState(TransferState.COMPLETE);
                }
            }

            httpClient.getConnectionManager().shutdown();
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Shut down http connection");
        } catch (FileNotFoundException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FILE_NOT_FOUND);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (SocketException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Input/Output Stream closed error");
                e.printStackTrace();
            }
        }
    }

    public interface OnDownloadFileListener {
        void onStart(String url, DownloadElement element);

        void onUploading(String url, DownloadElement element);

        void onComplete(String url, DownloadElement element);
    }
}
