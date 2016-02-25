package com.eli.oneos.db;

import com.eli.oneos.db.greendao.BackupFileInfo;
import com.eli.oneos.db.greendao.BackupFileInfoDao;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BackupFileKeeper {

    /**
     * List Backup Info by mac and username
     *
     * @param mac  device mac
     * @param user login user name
     * @return
     */
    public static List<BackupFileInfo> all(String mac, String user) {
        BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupFileInfoDao.Properties.Mac.eq(mac));

        return queryBuilder.list();
    }

    /**
     * Get Backup Info from database by user, mac and path
     *
     * @param user user targetPath
     * @param mac  mac address
     * @param path backup path
     * @return BackupFileInfo or NULL
     */
    public static BackupFileInfo getBackupInfo(String mac, String user, String path) {
        BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupFileInfoDao.Properties.Mac.eq(mac));
        queryBuilder.where(BackupFileInfoDao.Properties.Path.eq(path));
        queryBuilder.limit(1);
        List<BackupFileInfo> list = queryBuilder.list();
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
    public static boolean insertOrReplace(BackupFileInfo info) {
        if (info != null) {
            BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
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
        BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileInfoDao.Properties.User.eq(user));
        queryBuilder.where(BackupFileInfoDao.Properties.Mac.eq(mac));

        List<BackupFileInfo> list = queryBuilder.list();
        if (null != list) {
            for (BackupFileInfo info : list) {
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
    public static boolean delete(BackupFileInfo info) {
        if (info != null) {
            BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
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
    public static boolean update(BackupFileInfo user) {
        if (null == user) {
            return false;
        }

        BackupFileInfoDao dao = DBHelper.getDaoSession().getBackupFileInfoDao();
        dao.update(user);
        return true;
    }
}
