package com.eli.oneos.model.api;

import com.eli.oneos.R;
import com.eli.oneos.model.FileOptAction;
import com.eli.oneos.model.FileOptItem;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/20.
 */
public class OneOSFileOptGenerate {
    private static int OPT_BASE_ID = 0x10000000;
    private static FileOptItem OPT_COPY = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_copy, R.drawable.btn_opt_copy_pressed, R.string.copy_file, FileOptAction.COPY);
    private static FileOptItem OPT_MOVE = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_move, R.drawable.btn_opt_move_pressed, R.string.move_file, FileOptAction.MOVE);
    private static FileOptItem OPT_DELETE = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_delete, R.drawable.btn_opt_delete_pressed, R.string.delete_file, FileOptAction.DELETE);
    private static FileOptItem OPT_RENAME = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_rename, R.drawable.btn_opt_rename_pressed, R.string.rename_file, FileOptAction.RENAME);
    private static FileOptItem OPT_DOWNLOAD = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_download, R.drawable.btn_opt_download_pressed, R.string.download_file, FileOptAction.DOWNLOAD);
    private static FileOptItem OPT_UPLOAD = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_upload, R.drawable.btn_opt_upload_pressed, R.string.upload_file, FileOptAction.UPLOAD);
    private static FileOptItem OPT_ENCRYPT = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_encrypt, R.drawable.btn_opt_encrypt_pressed, R.string.encrypt_file, FileOptAction.ENCRYPT);
    private static FileOptItem OPT_DECRYPT = new FileOptItem(OPT_BASE_ID++, R.drawable.btn_opt_decrypt, R.drawable.btn_opt_decrypt_pressed, R.string.decrypt_file, FileOptAction.DECRYPT);


    public static ArrayList<FileOptItem> generate(OneOSFileType fileType, ArrayList<OneOSFile> selectedList) {
        ArrayList<FileOptItem> mOptItems = new ArrayList<>();
        mOptItems.add(OPT_COPY);
        mOptItems.add(OPT_MOVE);
        mOptItems.add(OPT_DELETE);
        mOptItems.add(OPT_RENAME);
        mOptItems.add(OPT_DOWNLOAD);
        mOptItems.add(OPT_ENCRYPT);

        return mOptItems;
    }


}
