package com.eli.oneos.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.MIMETypeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2017/9/8.
 */

public class DocumentViewActivity extends BaseActivity{

    private static final String TAG = DocumentViewActivity.class.getSimpleName();
    private LoginSession mLoginSession;
    protected MainActivity mMainActivity;
    private ProgressDialog mProgressDialog;

    // 下载失败
    public static final int DOWNLOAD_ERROR = 2;
    // 下载成功
    public static final int DOWNLOAD_SUCCESS = 1;
    //private  File file1;
    private String savePath,fileName;
    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);

        initView();
    }

    private void initView() {
        // TODO Auto-generated method stub
        Intent intent = this.getIntent();
        final String url = intent.getStringExtra("url");
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        fileName = intent.getStringExtra("fileName");
        mLoginSession = LoginManage.getInstance().getLoginSession();
        savePath = mLoginSession.getDownloadPath() + "/" + fileName;
        Log.d(TAG,"Stringname == " + savePath);

        //file1 = new File(savePath);
        new Thread() {
            public void run() {

                File docFile = new File(savePath);

                //判断是否有此文件
                if (docFile.exists()) {
                    //有缓存文件,拿到路径 直接打开
                    Message msg = Message.obtain();
                    msg.obj = docFile;
                    msg.what = DOWNLOAD_SUCCESS;
                    handler.sendMessage(msg);
                    mProgressDialog.dismiss();
                    return;
                }
                //本地没有此文件 则下载打开
                File downloadfile = downLoad(url, savePath, mProgressDialog);
                Log.d(TAG,"========" + savePath);
                Message msg = Message.obtain();
                if (downloadfile != null) {
                    // 下载成功
                    msg.obj = downloadfile;
                    msg.what = DOWNLOAD_SUCCESS;
                } else {
                    // 提示用户下载失败.
                    msg.what = DOWNLOAD_ERROR;
                }
                handler.sendMessage(msg);
                mProgressDialog.dismiss();
            }
        }.start();
    }

    /**
     * 下载完成后  直接打开文件
     */
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DOWNLOAD_SUCCESS:

                   /* File file = (File) msg.obj;
                    Intent intent = new Intent("android.intent.action.VIEW");
                    String type = MIMETypeUtils.getMIMEType(fileName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), type);
                    startActivity(intent);
                    finish();*/


                    File file = (File) msg.obj;
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                    String type = MIMETypeUtils.getMIMEType(fileName);
                    intent.setDataAndType (Uri.fromFile(file), type);
                    startActivity(Intent.createChooser(intent, "标题"));
                    finish();
                    /**
                     * 弹出选择框   把本activity销毁
                     */
                    break;
                case DOWNLOAD_ERROR:
                    //Util.showToast(act,"文件加载失败");
                    break;
            }
        }
    };
/**
 *
 */


    /**
     * 传入文件 url  文件路径  和 弹出的dialog  进行 下载文档
     */
    public static File downLoad(String serverpath, String savePath, ProgressDialog pd) {
        try {
            URL url = new URL(serverpath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            if (conn.getResponseCode() == 200) {
                int max = conn.getContentLength();
                Log.d(TAG,"max size = " + max);
                FileUtils.fmtFileSize(max);
                pd.setProgressNumberFormat("%1d kb/%2d kb");
                pd.setMax(max/1024);
                InputStream is = conn.getInputStream();
                File file = new File(savePath);
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                int total = 0;
                int progress = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    total += len;
                    Log.d(TAG,"progress == " + (total*100/max));
                    if ((total*100/max) > progress) {
                        progress = total*100/max;
                        pd.setProgress(total / 1024);
                        Log.d(TAG, "progress size = " + total);
                    }
                }
                fos.flush();
                fos.close();
                is.close();
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
