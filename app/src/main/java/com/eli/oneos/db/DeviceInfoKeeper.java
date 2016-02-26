package com.eli.oneos.db;

import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.DeviceInfoDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DeviceInfoKeeper {

    public static List<DeviceInfo> all() {
        DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(DeviceInfoDao.Properties.Time);

        return queryBuilder.list();
    }

    public static boolean insertOrReplace(DeviceInfo history) {
        if (history != null) {
            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
            return dao.insertOrReplace(history) > 0;
        }

        return false;
    }

    public static boolean delete(DeviceInfo history) {
        if (history != null) {
            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
            dao.delete(history);

            return true;
        }

        return false;
    }
}
