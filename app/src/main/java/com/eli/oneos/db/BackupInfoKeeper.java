package com.eli.oneos.db;

import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.db.greendao.BackupInfoDao;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BackupInfoKeeper {

    /**
     * List Backup Info by mac and username
     *
     * @param mac  device mac
     * @param user login user name
     * @return
     */
    public static List<BackupInfo> all(String mac, String user) {
        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupInfoDao.Properties.Mac.eq(mac));

        return queryBuilder.list();
    }

    /**
     * Get Backup Info from database by user, mac and path
     *
     * @param user user targetPath
     * @param mac  mac address
     * @param path backup path
     * @return BackupInfo or NULL
     */
    public static BackupInfo getBackupInfo(String mac, String user, String path) {
        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupInfoDao.Properties.Mac.eq(mac));
        queryBuilder.where(BackupInfoDao.Properties.Path.eq(path));
        queryBuilder.limit(1);
        List<BackupInfo> list = queryBuilder.list();
        if (!EmptyUtils.isEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return insertOrReplace result
     */
    public static boolean insertOrReplace(BackupInfo info) {
        if (info != null) {
            BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    /**
     * Reset Backup by mac and username
     *
     * @param mac  device mac
     * @param user login user name
     * @return
     */
    public static boolean reset(String mac, String user) {
        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupInfoDao.Properties.Mac.eq(mac));

        List<BackupInfo> list = queryBuilder.list();
        if (null != list) {
            for (BackupInfo info : list) {
                info.setTime(0L);
                update(info);
            }
        }

        return true;
    }

    /**
     * Delete a user from Database
     *
     * @param info
     * @return delete result
     */
    public static boolean delete(BackupInfo info) {
        if (info != null) {
            BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
            dao.delete(info);

            return true;
        }

        return false;
    }

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(BackupInfo user) {
        if (null == user) {
            return false;
        }

        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        dao.update(user);
        return true;
    }
}
