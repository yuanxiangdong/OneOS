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
     * @return last list user
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
     * Get user from database by user and mac
     *
     * @param user user targetPath
     * @param mac  mac address
     * @return UserInfo or NULL
     */
    public static UserInfo getUserInfo(String user, String mac) {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.Name.eq(user));
        queryBuilder.where(UserInfoDao.Properties.Mac.eq(mac));
        queryBuilder.limit(1);
        List<UserInfo> list = queryBuilder.list();
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
    public static boolean insertOrReplace(UserInfo info) {
        if (info != null) {
            UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    public static UserInfo insertOrReplace(String user, String pwd, String mac, Long time, int uid, int gid, int admin) {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        UserInfo userInfo = getUserInfo(user, mac);
        if (userInfo == null) {
            userInfo = new UserInfo(user, pwd, mac, time, uid, gid, admin, null);
            dao.insert(userInfo);
        } else {
            userInfo.setPwd(pwd);
            userInfo.setTime(time);
            userInfo.setUid(uid);
            userInfo.setGid(gid);
            userInfo.setAdmin(admin);
            dao.update(userInfo);
        }

        return userInfo;
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

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(UserInfo user) {
        if (null == user) {
            return false;
        }

        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        dao.update(user);
        return true;
    }
}
