package com.eli.oneos.db.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.eli.oneos.db.greendao.DeviceInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table DEVICE_INFO.
*/
public class DeviceInfoDao extends AbstractDao<DeviceInfo, String> {

    public static final String TABLENAME = "DEVICE_INFO";

    /**
     * Properties of entity DeviceInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Mac = new Property(0, String.class, "mac", true, "MAC");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property LanIp = new Property(2, String.class, "lanIp", false, "LAN_IP");
        public final static Property LanPort = new Property(3, String.class, "lanPort", false, "LAN_PORT");
        public final static Property WanIp = new Property(4, String.class, "wanIp", false, "WAN_IP");
        public final static Property WanPort = new Property(5, String.class, "wanPort", false, "WAN_PORT");
        public final static Property SsudpCid = new Property(6, String.class, "ssudpCid", false, "SSUDP_CID");
        public final static Property SsudpPwd = new Property(7, String.class, "ssudpPwd", false, "SSUDP_PWD");
        public final static Property Domain = new Property(8, Integer.class, "domain", false, "DOMAIN");
        public final static Property Time = new Property(9, Long.class, "time", false, "TIME");
    };


    public DeviceInfoDao(DaoConfig config) {
        super(config);
    }
    
    public DeviceInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'DEVICE_INFO' (" + //
                "'MAC' TEXT PRIMARY KEY NOT NULL ," + // 0: mac
                "'NAME' TEXT," + // 1: name
                "'LAN_IP' TEXT," + // 2: lanIp
                "'LAN_PORT' TEXT," + // 3: lanPort
                "'WAN_IP' TEXT," + // 4: wanIp
                "'WAN_PORT' TEXT," + // 5: wanPort
                "'SSUDP_CID' TEXT," + // 6: ssudpCid
                "'SSUDP_PWD' TEXT," + // 7: ssudpPwd
                "'DOMAIN' INTEGER," + // 8: domain
                "'TIME' INTEGER);"); // 9: time
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'DEVICE_INFO'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DeviceInfo entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getMac());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String lanIp = entity.getLanIp();
        if (lanIp != null) {
            stmt.bindString(3, lanIp);
        }
 
        String lanPort = entity.getLanPort();
        if (lanPort != null) {
            stmt.bindString(4, lanPort);
        }
 
        String wanIp = entity.getWanIp();
        if (wanIp != null) {
            stmt.bindString(5, wanIp);
        }
 
        String wanPort = entity.getWanPort();
        if (wanPort != null) {
            stmt.bindString(6, wanPort);
        }
 
        String ssudpCid = entity.getSsudpCid();
        if (ssudpCid != null) {
            stmt.bindString(7, ssudpCid);
        }
 
        String ssudpPwd = entity.getSsudpPwd();
        if (ssudpPwd != null) {
            stmt.bindString(8, ssudpPwd);
        }
 
        Integer domain = entity.getDomain();
        if (domain != null) {
            stmt.bindLong(9, domain);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(10, time);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public DeviceInfo readEntity(Cursor cursor, int offset) {
        DeviceInfo entity = new DeviceInfo( //
            cursor.getString(offset + 0), // mac
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // lanIp
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // lanPort
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // wanIp
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // wanPort
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // ssudpCid
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // ssudpPwd
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8), // domain
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9) // time
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DeviceInfo entity, int offset) {
        entity.setMac(cursor.getString(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLanIp(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLanPort(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setWanIp(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setWanPort(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setSsudpCid(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSsudpPwd(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setDomain(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
        entity.setTime(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DeviceInfo entity, long rowId) {
        return entity.getMac();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DeviceInfo entity) {
        if(entity != null) {
            return entity.getMac();
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
