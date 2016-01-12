package com.eli.oneos.db;

import com.eli.oneos.db.greendao.UserHistory;
import com.eli.oneos.db.greendao.UserHistoryDao;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserHistoryKeeper {

    /**
     * List all Users by ID Desc
     *
     * @return user list
     */
    public static List<UserHistory> all() {
        UserHistoryDao dao = DBHelper.getDaoSession().getUserHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(UserHistoryDao.Properties.Time);

        return queryBuilder.list();
    }

    /**
     * Query top user (Last Login user)
     *
     * @return last login user
     */
    public static UserHistory top() {
        UserHistoryDao dao = DBHelper.getDaoSession().getUserHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(UserHistoryDao.Properties.Time);
        queryBuilder.limit(1);
        List<UserHistory> list = queryBuilder.list();
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
    public static boolean insertOrReplace(UserHistory info) {
        if (info != null) {
            UserHistoryDao dao = DBHelper.getDaoSession().getUserHistoryDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    /**
     * Delete a user from Database
     *
     * @param info
     * @return delete result
     */
    public static boolean delete(UserHistory info) {
        if (info != null) {
            UserHistoryDao dao = DBHelper.getDaoSession().getUserHistoryDao();
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
    public static boolean update(UserHistory user) {
        if (null == user) {
            return false;
        }

        UserHistoryDao dao = DBHelper.getDaoSession().getUserHistoryDao();
        dao.update(user);
        return true;
    }
}
