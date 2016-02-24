package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.user.LoginSession;

import net.tsz.afinal.http.AjaxParams;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * OneSpace OS Upload File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 */
public class OneOSUploadFileAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSUploadFileAPI.class.getSimpleName();
    private static final int HTTP_UPLOAD_RETRY_TIMES = 5;
    private static final int HTTP_BUFFER_SIZE = 1024 * 8;
    /**
     * chuck block size: 4mb
     */
    private static final int HTTP_BLOCK_SIZE = 1024 * 1024 * 4;

    private OnUploadFileListener listener;
    private UploadElement uploadElement;
    private boolean isInterrupt = false;
    private LoginSession loginSession;

    public OneOSUploadFileAPI(LoginSession loginSession, UploadElement element) {
        super(loginSession);
        this.loginSession = loginSession;
        this.uploadElement = element;
    }

    public void setOnUploadFileListener(OnUploadFileListener listener) {
        this.listener = listener;
    }

    public void upload() {
        if (null != listener) {
            listener.onStart(url, uploadElement);
        }

        if (uploadElement.isCheck() ||
                !checkExist(uploadElement.getTargetPath() + uploadElement.getSrcName(), uploadElement.getSize())) {
            doUpload();
        } else {
            uploadElement.setState(TransferState.COMPLETE);
        }

        if (null != listener) {
            listener.onComplete(url, uploadElement);
        }
    }

    public void stopUpload() {
        isInterrupt = true;
        Log.d(TAG, "stop upload");
    }

    /**
     * check if file exist in server
     *
     * @param path    file server path
     * @param srcSize
     * @return true if exist, otherwise false
     */
    private boolean checkExist(String path, long srcSize) {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_MANAGE);
        AjaxParams params = new AjaxParams();
        params.put("session", session);
        params.put("cmd", "attributes");
        params.put("path", path);
        logDebug(TAG, url, params);
        try {
            String result = (String) finalHttp.postSync(url, params);
            Log.d(TAG, "File Attr: " + result);
            JSONObject json = new JSONObject(result);
            boolean ret = json.getBoolean("result");
            if (ret) {
                long size = json.getLong("size");
                if (size == srcSize) {
                    Log.e(TAG, "****Upload file is exist: " + path);
                    return true;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.d(TAG, "****Upload file is not exist");
        }

        return false;
    }

    private void doUpload() {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_UPLOAD);
        logDebug(TAG, url, null);

        uploadElement.setState(TransferState.START);
        String session = loginSession.getSession();
        String srcPath = uploadElement.getSrcPath();
        String targetPath = uploadElement.getTargetPath();
        boolean isUploadToPrivateDir = uploadElement.isUploadToPrivateDir();
        long userFreeSpace = -1;

        isUploadToPrivateDir = false; // TODO.. for test
//        if (isUploadToPrivateDir) {
//            try {
//                userFreeSpace = getUserServerFreeSpace(session, loginSession.getUserInfo().getUid(), loginSession.getBaseUrl());
//                if (userFreeSpace < 0) {
//                    uploadElement.setState(TransferState.FAILED);
//                    if (userFreeSpace == -1) {
//                        uploadElement.setException(TransferException.REQUEST_SERVER);
//                    } else {
//                        uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
//                    }
//                    return;
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//                uploadElement.setState(TransferState.FAILED);
//                uploadElement.setException(TransferException.ENCODING_EXCEPTION);
//            } catch (IOException e) {
//                e.printStackTrace();
//                uploadElement.setState(TransferState.FAILED);
//                uploadElement.setException(TransferException.REQUEST_SERVER);
//            } catch (Exception e) {
//                e.printStackTrace();
//                uploadElement.setState(TransferState.FAILED);
//                uploadElement.setException(TransferException.UNKNOW_EXCEPTION);
//            }
//        }

        File uploadFile = new File(srcPath);
        if (!uploadFile.exists()) {
            Log.e(TAG, "upload file is not exist");
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.FILE_NOT_FOUND);
            return;
        }

        long fileLen = uploadFile.length();
        long uploadPosition = 0;
        if (isUploadToPrivateDir && (userFreeSpace <= fileLen - uploadPosition)) {
            Log.e(TAG, "File Length = " + fileLen + " ; Disk space = " + userFreeSpace);
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
            return;
        }

        // Modified to new position, to make sure anim_progress is correct
        uploadElement.setLength(uploadPosition);
        uploadElement.setOffset(uploadPosition);

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
                URL mUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
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
                sb.append(targetPath);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                if (uploadElement.isOverwrite()) {
                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"overwrite\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append("1");
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();
                }

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
                    uploadElement.setLength(uploadLen);
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
                        uploadElement.setException(TransferException.REQUEST_SERVER);
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
                uploadElement.setException(TransferException.REQUEST_SERVER);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.FILE_NOT_FOUND);
            } catch (IOException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.IO_EXCEPTION);
            } finally {
                if (!isInterrupt && retry > 0) {
                    try {
                        chunkIndex--;
                        uploadLen -= blockUpLen;
                        uploadElement.setLength(uploadLen);
                        Thread.sleep(retry * retry * 100);
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
            uploadElement.setState(TransferState.COMPLETE);
        } else {
            if (isInterrupt) {
                uploadElement.setState(TransferState.PAUSE);
            } else {
                uploadElement.setState(TransferState.FAILED);
            }
        }
    }

    public interface OnUploadFileListener {
        void onStart(String url, UploadElement element);

        void onComplete(String url, UploadElement element);
    }
}
