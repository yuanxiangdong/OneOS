package com.eli.oneos.model.oneos;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileManageItem;
import com.eli.oneos.utils.EmptyUtils;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/20.
 */
public class OneOSFileManageGenerate {
    private static int OPT_BASE_ID = 0x10000000;
    private static FileManageItem OPT_COPY = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_copy, R.drawable.btn_opt_copy_pressed, R.string.copy_file, FileManageAction.COPY);
    private static FileManageItem OPT_MOVE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_move, R.drawable.btn_opt_move_pressed, R.string.move_file, FileManageAction.MOVE);
    private static FileManageItem OPT_DELETE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_delete, R.drawable.btn_opt_delete_pressed, R.string.delete_file, FileManageAction.DELETE);
    private static FileManageItem OPT_RENAME = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_rename, R.drawable.btn_opt_rename_pressed, R.string.rename_file, FileManageAction.RENAME);
    private static FileManageItem OPT_DOWNLOAD = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_download, R.drawable.btn_opt_download_pressed, R.string.download_file, FileManageAction.DOWNLOAD);
    private static FileManageItem OPT_UPLOAD = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_upload, R.drawable.btn_opt_upload_pressed, R.string.upload_file, FileManageAction.UPLOAD);
    private static FileManageItem OPT_ENCRYPT = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_encrypt, R.drawable.btn_opt_encrypt_pressed, R.string.encrypt_file, FileManageAction.ENCRYPT);
    private static FileManageItem OPT_DECRYPT = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_decrypt, R.drawable.btn_opt_decrypt_pressed, R.string.decrypt_file, FileManageAction.DECRYPT);
    private static FileManageItem OPT_ATTR = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_share, R.drawable.btn_opt_share_pressed, R.string.attr_file, FileManageAction.ATTR);
    private static FileManageItem OPT_CLEAN = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_delete, R.drawable.btn_opt_delete_pressed, R.string.clean_recycle_file, FileManageAction.CLEAN_RECYCLE);


    public static ArrayList<FileManageItem> generate(OneOSFileType fileType, ArrayList<OneOSFile> selectedList) {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null;
        }

        ArrayList<FileManageItem> mOptItems = new ArrayList<>();
        if (fileType == OneOSFileType.RECYCLE) {
            mOptItems.add(OPT_MOVE);
            mOptItems.add(OPT_DELETE);
            mOptItems.add(OPT_CLEAN);
        } else {
            mOptItems.add(OPT_COPY);
            mOptItems.add(OPT_MOVE);
            mOptItems.add(OPT_DELETE);
            mOptItems.add(OPT_DOWNLOAD);

            int count = selectedList.size();
            if (count == 1) {
                mOptItems.add(OPT_RENAME);
                OneOSFile file = selectedList.get(0);
                if (!file.isDirectory()) {
                    if (file.isEncrypt()) {
                        mOptItems.add(OPT_DECRYPT);
                    } else {
                        mOptItems.add(OPT_ENCRYPT);
                    }
                }
                mOptItems.add(OPT_ATTR);
            }
        }

        return mOptItems;
    }


}
