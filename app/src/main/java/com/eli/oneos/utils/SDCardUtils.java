package com.eli.oneos.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.eli.oneos.constant.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SDCardUtils {

    private static final String TAG = SDCardUtils.class.getSimpleName();

    /**
     * create local download store path
     */
    public static String createDefaultDownloadPath(String user) {
        String savePath;
        String path = Constants.DEFAULT_APP_ROOT_DIR_NAME + File.separator + user + Constants.DEFAULT_DOWNLOAD_DIR_NAME;
        if (SDCardUtils.checkSDCard()) {
            savePath = Environment.getExternalStorageDirectory() + path;
        } else {
            savePath = Environment.getDownloadCacheDirectory().getAbsolutePath() + path;
        }
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Log.i(TAG, "Create default download path: " + savePath);
        return savePath;
    }

    // /** get directory available size */
    public static long getDeviceTotalSize(String path) {
        if (path == null) {
            return -1;
        }

        List<File> mSDCardList = SDCardUtils.getSDCardList();
        if (null != mSDCardList && mSDCardList.size() > 0) {
            String sdPath = null;
            for (File root : mSDCardList) {
                String rootPath = root.getAbsolutePath();
                if (path.startsWith(rootPath)) {
                    sdPath = rootPath;
                    break;
                }
            }

            if (null != sdPath) {
                StatFs sf = new StatFs(sdPath);
                long blockCount = sf.getBlockCount();
                long blockSize = sf.getBlockSize();
                long bookTotalSize = blockCount * blockSize;
                return bookTotalSize;
            }
        }

        return -1;
    }

    // /** get directory available size */
    public static long getDeviceAvailableSize(String path) {
        if (path == null) {
            return -1;
        }

        List<File> mSDCardList = SDCardUtils.getSDCardList();
        if (null != mSDCardList && mSDCardList.size() > 0) {
            String sdPath = null;
            for (File root : mSDCardList) {
                String rootPath = root.getAbsolutePath();
                if (path.startsWith(rootPath)) {
                    sdPath = rootPath;
                    break;
                }
            }

            if (null != sdPath) {
                StatFs sf = new StatFs(sdPath);
                long blockSize = sf.getBlockSize();
                long freeBlocks = sf.getAvailableBlocks();
                return (freeBlocks * blockSize);
            }
        }

        return -1;
    }


    // /** Get Sd card total size */
    public static long getSDTotalSize(String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            long blockCount = statFs.getBlockCount();
            long blockSize = statFs.getBlockSize();
            long bookTotalSize = blockCount * blockSize;
            return bookTotalSize;
        } else {
            return getDeviceTotalSize(downloadPath);
        }
    }

    /**
     * Get free space of SD card
     **/
    public static long getSDAvailableSize(String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            return (freeBlocks * blockSize);
        } else {
            return getDeviceAvailableSize(downloadPath);
        }
    }

    public static File getExternalSDCard() {
        ArrayList<File> sdcards = getSDCardList();

        if (null != sdcards && sdcards.size() > 0) {

            String interSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            for (File sd : sdcards) {
                if (!sd.getAbsolutePath().equals(interSDPath)) {
                    return sd;
                }
            }
        }

        return null;
    }

    /**
     * 获取SD卡路径列表
     *
     * @return
     */
    public static ArrayList<File> getSDCardList() {
        ArrayList<String> sdcardPaths = new ArrayList<String>();
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process process = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                // Log.e(TAG, "-->> " + lineStr);

                String[] temp = TextUtils.split(lineStr, " ");
                // 得到的输出的第二个空格后面是路径
                String result = temp[1];
                File file = new File(result);
                if (!result.endsWith("legacy") && file.isDirectory() && file.canRead() && file.canWrite() && !isSymbolicLink(file)) {
                    // Logged.d(TAG, "directory can read can write:" + file.getPath());
                    // 可读可写的文件夹未必是sdcard，我的手机的sdcard下的Android/obb文件夹也可以得到
                    sdcardPaths.add(result);
                    // Log.d(TAG, ">>>> Add Path: " + result);
                }

                // 检查命令是否执行失败。
                if (process.waitFor() != 0 && process.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    Log.e(TAG, "CommonUtil: GetSDCardPath Command fails!");
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sdcardPaths.contains(externalPath)) {
            sdcardPaths.add(externalPath);
        }

        optimize(sdcardPaths);

        ArrayList<File> sdcardList = new ArrayList<File>();
        for (Iterator<String> iterator = sdcardPaths.iterator(); iterator.hasNext(); ) {
            String path = iterator.next();
            sdcardList.add(new File(path));
        }

        return sdcardList;
    }

    /**
     * Check the state of SDcard, if exist return true, else return false
     */
    public static boolean checkSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private static void optimize(List<String> sdcardPaths) {
        if (sdcardPaths.size() == 0) {
            return;
        }
        int index = 0;
        while (true) {
            if (index >= sdcardPaths.size() - 1) {
                String lastItem = sdcardPaths.get(sdcardPaths.size() - 1);
                for (int i = sdcardPaths.size() - 2; i >= 0; i--) {
                    if (sdcardPaths.get(i).contains(lastItem)) {
                        sdcardPaths.remove(i);
                    }
                }
                return;
            }

            String containsItem = sdcardPaths.get(index);
            for (int i = index + 1; i < sdcardPaths.size(); i++) {
                if (sdcardPaths.get(i).contains(containsItem)) {
                    sdcardPaths.remove(i);
                    i--;
                }
            }

            index++;
        }
    }

    private static boolean isSymbolicLink(File file) {
        if (null == file) {
            return true;
        }

        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
