package com.eli.oneos.model.phone.api;

import com.eli.oneos.utils.EmptyUtils;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class MkDirAPI {
    private static final String TAG = MkDirAPI.class.getSimpleName();

    public boolean mkdir(String path) {
        if (EmptyUtils.isEmpty(path)) {
            return false;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }

        return true;
    }

}
