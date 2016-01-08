package com.eli.oneos.db;

import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserInfoDao;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserInfoKeeper {

    /**
     * List all Users by ID Desc
     *
     * @return user list
     */
    public static List<UserInfo> all() {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(UserInfoDao.Properties.Time);

        return queryBuilder.list();
    }

    /**
     * Query top user (Last Login user)
     *
     * @return last login user
     */
    public static UserInfo top() {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(UserInfoDao.Properties.Time);
        queryBuilder.limit(1);
        List<UserInfo> list = queryBuilder.list();
        if (!EmptyUtils.isEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Insert a user to Database
     *
     * @param info
     * @return insert result
     */
    public static boolean insert(UserInfo info) {
        if (info != null) {
            UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
            return dao.insert(info) > 0;
        }

        return false;
    }

    /**
     * Delete a user from Database
     *
     * @param info
     * @return delete result
     */
    public static boolean delete(UserInfo info) {
        if (info != null) {
            UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
            dao.delete(info);

            return true;
        }

        return false;
    }
}
