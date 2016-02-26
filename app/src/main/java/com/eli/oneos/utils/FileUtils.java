package com.eli.oneos.utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Base64;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.PictureViewActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class FileUtils {

    /**
     * get photo date
     *
     * @param file
     * @return photo date
     */
    public static String getPhotoDate(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            if (exif != null) {
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    String date = exif.getAttribute(ExifInterface.TAG_DATETIME).substring(0, 7);
                    return date.replace(":", "-");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Constants.PHOTO_DATE_UNKNOWN;
    }

    /**
     * Compress Image
     *
     * @param imgPath
     * @param width   target width
     * @param height  target height
     * @return
     */
    public static Bitmap compressImage(String imgPath, float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，options.inJustDecodeBounds=true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        float hh = height;
        float ww = width;

        int be = 1;// 缩放比例
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;

        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);

        return bitmap;
    }

    public static String getFileTime(File file) {
        long time = file.lastModified();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String date = format.format(new Date(time));
        return date;
    }

    public static String getCurFormatTime() {
        return formatTime(System.currentTimeMillis());
    }

    public static String getCurFormatTime(String fmt) {
        return formatTime(System.currentTimeMillis(), fmt);
    }

    public static String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String date = format.format(new Date(time));
        return date;
    }

    public static String formatTime(long time, String fmt) {
        if (EmptyUtils.isEmpty(fmt)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        String date = format.format(new Date(time));
        return date;
    }

    /**
     * Converted into a standard BeiJing Time, and format output
     */
    public static String fmtTimeByZone(long time, String format) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        Date date = new Date(time * 1000L);
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(date);
    }

    /**
     * Converted into a standard BeiJing Time, and format["yyyy-MM-dd  HH:mm:ss"] output
     */
    public static String fmtTimeByZone(long time) {
        return fmtTimeByZone(time, "yyyy-MM-dd  HH:mm:ss");
    }

    /**
     * get file except folder
     */
    public static int fmtFileIcon(String name) {
        if (EmptyUtils.isEmpty(name)) {
            return R.drawable.icon_file_default;
        }

        name = name.toLowerCase().trim();
        int icon;
        if (name.endsWith(".mp3") || name.endsWith(".wma") || name.endsWith(".wav")
                || name.endsWith(".aac") || name.endsWith(".ape") || name.endsWith(".m4a")
                || name.endsWith(".flac") || name.endsWith(".ogg")) {
            icon = R.drawable.icon_file_audio;
        } else if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".rmvb")
                || name.endsWith(".3gp") || name.endsWith(".rm") || name.endsWith(".asf")
                || name.endsWith(".wmv") || name.endsWith(".flv") || name.endsWith(".mov")
                || name.endsWith(".mkv")) {
            icon = R.drawable.icon_file_video;
        } else if (name.endsWith(".txt") || name.endsWith(".log")) {
            icon = R.drawable.icon_file_txt;
        } else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif")
                || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
            icon = R.drawable.icon_file_pic;
        } else if (name.endsWith(".apk")) {
            icon = R.drawable.icon_file_apk;
        } else if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar")
                || name.endsWith(".jar") || name.endsWith(".tar.gz")) {
            icon = R.drawable.icon_file_zip;
        } else if (name.endsWith(".xls")) {
            icon = R.drawable.icon_file_xls;
        } else if (name.endsWith(".ppt")) {
            icon = R.drawable.icon_file_ppt;
        } else if (name.endsWith(".doc")) {
            icon = R.drawable.icon_file_word;
        } else if (name.endsWith(".pdf")) {
            icon = R.drawable.icon_file_pdf;
        } else if (name.endsWith(".bin") || name.endsWith(".exe")) {
            icon = R.drawable.icon_file_bin;
        } else if (name.endsWith(".torrent")) {
            icon = R.drawable.icon_file_torrent;
        } else {
            icon = R.drawable.icon_file_default;
        }

        return icon;
    }

    public static int fmtFileIcon(File file) {
        if (file.isDirectory()) {
            return R.drawable.icon_file_folder;
        }

        return fmtFileIcon(file.getName());
    }

    public static final String fmtFileSize(long len) {
        return Formatter.formatFileSize(MyApplication.getAppContext(), len);
    }

    public static void openOneOSFile(LoginSession loginSession, BaseActivity activity, int position, final ArrayList<OneOSFile> fileList) {
        OneOSFile file = fileList.get(position);
        if (file.isPicture()) {
            ArrayList<OneOSFile> picList = new ArrayList<>();
            for (OneOSFile f : fileList) {
                if (f.isPicture()) {
                    picList.add(f);
                }
            }
            openOneOSPicture(activity, position, picList);
        } else {
            String url = OneOSAPIs.genDownloadUrl(loginSession, file);
            try {
                Intent intent = new Intent();
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                String type = MIMETypeUtils.getMIMEType(file.getName());
                intent.setDataAndType(Uri.parse(url), type);
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                activity.showTipView(R.string.error_app_not_found_to_open_file, false);
            }
        }
    }

    public static void openOneOSPicture(BaseActivity activity, int position, final ArrayList<OneOSFile> picList) {
        Intent intent = new Intent(activity, PictureViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("StartIndex", position);
        bundle.putBoolean("IsLocalPicture", false);
        bundle.putSerializable("PictureList", picList);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }


    /**
     * Notification system scans the specified file
     */
    public static void requestScanFile(File mFile) {
        if (mFile == null) {
            return;
        }

        try {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(mFile));
            MyApplication.getAppContext().sendBroadcast(scanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * encode file to Base64 String
     *
     * @param path file path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String encodeFileToBase64(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * get file name by path
     */
    public static String getFileName(String fullname) {
        String filename = null;
        if (null != fullname) {
            fullname = fullname.trim();
            int index = fullname.lastIndexOf("/");
            filename = fullname.substring(index + 1, fullname.length());
        }
        return filename;
    }

    public static boolean isPictureFile(String name) {
        if (name != null) {
            name = name.toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif")
                    || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVideoFile(String name) {
        if (name != null) {
            name = name.toLowerCase();
            if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".rmvb")
                    || name.endsWith(".3gp") || name.endsWith(".rm") || name.endsWith(".asf")
                    || name.endsWith(".wmv") || name.endsWith(".flv") || name.endsWith(".mov")
                    || name.endsWith(".mkv")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPictureOrVideo(File file) {
        return isPictureFile(file.getName()) || isVideoFile(file.getName());
    }
}
