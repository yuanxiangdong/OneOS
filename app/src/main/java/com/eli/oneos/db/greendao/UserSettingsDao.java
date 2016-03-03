package com.eli.oneos.db.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.eli.oneos.db.greendao.UserSettings;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table USER_SETTINGS.
*/
public class UserSettingsDao extends AbstractDao<UserSettings, Long> {

    public static final String TABLENAME = "USER_SETTINGS";

    /**
     * Properties of entity UserSettings.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Uid = new Property(0, long.class, "uid", true, "UID");
        public final static Property DownloadPath = new Property(1, String.class, "downloadPath", false, "DOWNLOAD_PATH");
        public final static Property IsAutoBackupFile = new Property(2, Boolean.class, "isAutoBackupFile", false, "IS_AUTO_BACKUP_FILE");
        public final static Property IsPreviewPicOnlyWifi = new Property(3, Boolean.class, "isPreviewPicOnlyWifi", false, "IS_PREVIEW_PIC_ONLY_WIFI");
        public final static Property IsTipTransferNotWifi = new Property(4, Boolean.class, "isTipTransferNotWifi", false, "IS_TIP_TRANSFER_NOT_WIFI");
        public final static Property IsBackupFileOnlyWifi = new Property(5, Boolean.class, "isBackupFileOnlyWifi", false, "IS_BACKUP_FILE_ONLY_WIFI");
        public final static Property FileOrderType = new Property(6, Integer.class, "fileOrderType", false, "FILE_ORDER_TYPE");
        public final static Property FileViewerType = new Property(7, Integer.class, "fileViewerType", false, "FILE_VIEWER_TYPE");
        public final static Property Time = new Property(8, Long.class, "time", false, "TIME");
    };


    public UserSettingsDao(DaoConfig config) {
        super(config);
    }
    
    public UserSettingsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'USER_SETTINGS' (" + //
                "'UID' INTEGER PRIMARY KEY NOT NULL ," + // 0: uid
                "'DOWNLOAD_PATH' TEXT," + // 1: downloadPath
                "'IS_AUTO_BACKUP_FILE' INTEGER," + // 2: isAutoBackupFile
                "'IS_PREVIEW_PIC_ONLY_WIFI' INTEGER," + // 3: isPreviewPicOnlyWifi
                "'IS_TIP_TRANSFER_NOT_WIFI' INTEGER," + // 4: isTipTransferNotWifi
                "'IS_BACKUP_FILE_ONLY_WIFI' INTEGER," + // 5: isBackupFileOnlyWifi
                "'FILE_ORDER_TYPE' INTEGER," + // 6: fileOrderType
                "'FILE_VIEWER_TYPE' INTEGER," + // 7: fileViewerType
                "'TIME' INTEGER);"); // 8: time
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'USER_SETTINGS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, UserSettings entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getUid());
 
        String downloadPath = entity.getDownloadPath();
        if (downloadPath != null) {
            stmt.bindString(2, downloadPath);
        }
 
        Boolean isAutoBackupFile = entity.getIsAutoBackupFile();
        if (isAutoBackupFile != null) {
            stmt.bindLong(3, isAutoBackupFile ? 1l: 0l);
        }
 
        Boolean isPreviewPicOnlyWifi = entity.getIsPreviewPicOnlyWifi();
        if (isPreviewPicOnlyWifi != null) {
            stmt.bindLong(4, isPreviewPicOnlyWifi ? 1l: 0l);
        }
 
        Boolean isTipTransferNotWifi = entity.getIsTipTransferNotWifi();
        if (isTipTransferNotWifi != null) {
            stmt.bindLong(5, isTipTransferNotWifi ? 1l: 0l);
        }
 
        Boolean isBackupFileOnlyWifi = entity.getIsBackupFileOnlyWifi();
        if (isBackupFileOnlyWifi != null) {
            stmt.bindLong(6, isBackupFileOnlyWifi ? 1l: 0l);
        }
 
        Integer fileOrderType = entity.getFileOrderType();
        if (fileOrderType != null) {
            stmt.bindLong(7, fileOrderType);
        }
 
        Integer fileViewerType = entity.getFileViewerType();
        if (fileViewerType != null) {
            stmt.bindLong(8, fileViewerType);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(9, time);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public UserSettings readEntity(Cursor cursor, int offset) {
        UserSettings entity = new UserSettings( //
            cursor.getLong(offset + 0), // uid
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // downloadPath
            cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0, // isAutoBackupFile
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // isPreviewPicOnlyWifi
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0, // isTipTransferNotWifi
            cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0, // isBackupFileOnlyWifi
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // fileOrderType
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // fileViewerType
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8) // time
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, UserSettings entity, int offset) {
        entity.setUid(cursor.getLong(offset + 0));
        entity.setDownloadPath(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setIsAutoBackupFile(cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0);
        entity.setIsPreviewPicOnlyWifi(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setIsTipTransferNotWifi(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
        entity.setIsBackupFileOnlyWifi(cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0);
        entity.setFileOrderType(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setFileViewerType(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setTime(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(UserSettings entity, long rowId) {
        entity.setUid(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(UserSettings entity) {
        if(entity != null) {
            return entity.getUid();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
