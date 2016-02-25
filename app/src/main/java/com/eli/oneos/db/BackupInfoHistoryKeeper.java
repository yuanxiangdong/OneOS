package com.eli.oneos.db;

import com.eli.oneos.db.greendao.BackupInfoHistory;
import com.eli.oneos.db.greendao.BackupInfoHistoryDao;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/25.
 */
public class BackupInfoHistoryKeeper {
    private static final String TYPE_BACKUP_CONTACTS = "backup_contacts";
    private static final String TYPE_RECOVERY_CONTACTS = "recovery_contacts";
    private static final String TYPE_BACKUP_SMS = "backup_sms";
    private static final String TYPE_RECOVERY_SMS = "recovery_sms";

    private static String getBackupType(BackupInfoType type) {
        if (type == BackupInfoType.BACKUP_CONTACTS) {
            return TYPE_BACKUP_CONTACTS;
        } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
            return TYPE_RECOVERY_CONTACTS;
        } else if (type == BackupInfoType.BACKUP_SMS) {
            return TYPE_BACKUP_SMS;
        } else {
            return TYPE_RECOVERY_SMS;
        }
    }

    public static BackupInfoHistory getBackupHistory(long uid, BackupInfoType type) {
        BackupInfoHistoryDao dao = DBHelper.getDaoSession().getBackupInfoHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoHistoryDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupInfoHistoryDao.Properties.Type.eq(getBackupType(type)));
        queryBuilder.limit(1);

        List<BackupInfoHistory> list = queryBuilder.list();
        if (!EmptyUtils.isEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    public static boolean insertOrReplace(BackupInfoHistory info) {
        if (info != null) {
            BackupInfoHistoryDao dao = DBHelper.getDaoSession().getBackupInfoHistoryDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    public static boolean update(long uid, BackupInfoType type, long time) {
        BackupInfoHistoryDao dao = DBHelper.getDaoSession().getBackupInfoHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoHistoryDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupInfoHistoryDao.Properties.Type.eq(getBackupType(type)));
        queryBuilder.limit(1);

        List<BackupInfoHistory> list = queryBuilder.list();
        if (!EmptyUtils.isEmpty(list)) {
            BackupInfoHistory history = list.get(0);
            history.setCount(history.getCount() + 1);
            history.setTime(time);
            dao.update(history);
        } else {
            BackupInfoHistory history = new BackupInfoHistory(null, uid, getBackupType(type), time, 1L);
            dao.insert(history);
        }

        return true;
    }
}
