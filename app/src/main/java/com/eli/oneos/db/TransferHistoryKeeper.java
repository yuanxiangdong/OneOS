package com.eli.oneos.db;

import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.db.greendao.TransferHistoryDao;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/26.
 */
public class TransferHistoryKeeper {

    public static int getTransferType(boolean isDownload) {
        if (isDownload) {
            return 1;
        }

        return 2;
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<TransferHistory> all(boolean isDownload) {
        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(TransferHistoryDao.Properties.Type.eq(getTransferType(isDownload)));
        queryBuilder.orderDesc(TransferHistoryDao.Properties.Time);

        return queryBuilder.list();
    }

    public static boolean insert(TransferHistory user) {
        if (null == user) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.insertOrReplace(user);
        return true;
    }

    public static boolean delete(long uid) {
        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        QueryBuilder<TransferHistory> queryBuilder = dao.queryBuilder();
        DeleteQuery<TransferHistory> deleteQuery = queryBuilder.where(TransferHistoryDao.Properties.Uid.eq(uid)).buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();

        return true;
    }

    public static boolean delete(TransferHistory user) {
        if (null == user) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.delete(user);
        return true;
    }

    public static boolean update(TransferHistory user) {
        if (null == user) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.update(user);
        return true;
    }
}
