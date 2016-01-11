package com.eli.oneos.db;

import com.eli.oneos.db.greendao.DeviceHistory;
import com.eli.oneos.db.greendao.DeviceHistoryDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DeviceHistoryKeeper {

    /**
     * List all Users by ID Desc
     *
     * @return user list
     */
    public static List<DeviceHistory> all() {
        DeviceHistoryDao dao = DBHelper.getDaoSession().getDeviceHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(DeviceHistoryDao.Properties.Time);

        return queryBuilder.list();
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param history
     * @return insertOrReplace result
     */
    public static boolean insertOrReplace(DeviceHistory history) {
        if (history != null) {
            DeviceHistoryDao dao = DBHelper.getDaoSession().getDeviceHistoryDao();
            return dao.insertOrReplace(history) > 0;
        }

        return false;
    }

    /**
     * Delete a user from Database
     *
     * @param history
     * @return delete result
     */
    public static boolean delete(DeviceHistory history) {
        if (history != null) {
            DeviceHistoryDao dao = DBHelper.getDaoSession().getDeviceHistoryDao();
            dao.delete(history);

            return true;
        }

        return false;
    }
}
