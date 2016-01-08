package com.eli.oneos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eli.oneos.MyApplication;
import com.eli.oneos.db.greendao.DaoMaster;
import com.eli.oneos.db.greendao.DaoSession;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DBHelper {

    private static final String DB_NAME = "oneos_db";

    /**
     * Get Writable Database
     *
     * @return Writable Database
     */
    public static SQLiteDatabase getWritableDB() {
        Context context = MyApplication.getAppContext();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);

        return helper.getWritableDatabase();
    }

    /**
     * Get GreenDao Session
     *
     * @return DaoSession
     */
    public static DaoSession getDaoSession() {
        DaoMaster daoMaster = new DaoMaster(getWritableDB());

        return daoMaster.newSession();
    }

}
