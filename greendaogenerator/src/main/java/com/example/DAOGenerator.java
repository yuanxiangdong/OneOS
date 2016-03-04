package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * GreenDAO generator for OneSpace
 */
public class DAOGenerator {
    private static final int DB_VERSION = 1;
    private static final String DEFAULT_PACKAGE = "com.eli.oneos.db.greendao";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, DEFAULT_PACKAGE);

        addDeviceInfoTable(schema);
        addUserInfoTable(schema);
        addUserSettingsTable(schema);
        addBackupFileTable(schema);
        addBackupInfoTable(schema);
        addTransferHistoryTable(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    /**
     * 设备信息表
     *
     * @param schema
     */
    private static void addDeviceInfoTable(Schema schema) {
        Entity note = schema.addEntity("DeviceInfo");
        note.addStringProperty("mac").notNull().primaryKey();   // 设备Mac地址
        note.addStringProperty("ip").notNull();                 // 设备IP地址
        note.addStringProperty("port").notNull();               // 设备端口号
        note.addBooleanProperty("isLAN");                       // 是否是局域网设备
        note.addLongProperty("time");                           // 创建时间
    }

    /**
     * 用户信息表
     *
     * @param schema
     */
    private static void addUserInfoTable(Schema schema) {
        Entity note = schema.addEntity("UserInfo");
        note.addIdProperty().autoincrement();
        note.addStringProperty("name").notNull();   // 用户名
        note.addStringProperty("mac").notNull();    // 设备Mac地址
        note.addStringProperty("pwd").notNull();    // 用户密码
        note.addIntProperty("admin");               // 是否为管理员用户（1:true, 2: false）
        note.addIntProperty("uid");                 // 设备数据库中的ID
        note.addIntProperty("gid");                 // 用户组ID
        note.addLongProperty("time");               // 最后登录时间
        note.addBooleanProperty("isLogout");        // 是否注销登录
        note.addBooleanProperty("isActive");        // 是否活跃（默认为false）；为false时，登录页面不显示该用户为备选
    }

    /**
     * 用户设置信息表
     *
     * @param schema
     */
    private static void addUserSettingsTable(Schema schema) {
        Entity note = schema.addEntity("UserSettings");
        note.addLongProperty("uid").notNull().primaryKey(); // 主键，用户信息表中的ID
        note.addStringProperty("downloadPath");             // 下载文件保存路径
        note.addBooleanProperty("isAutoBackupFile");        // 自动备份文件（默认为false）
        note.addBooleanProperty("isPreviewPicOnlyWifi");    // 仅Wi-Fi环境下显示预览图（默认为true）
        note.addBooleanProperty("isTipTransferNotWifi");    // 非Wi-Fi环境上传/下载文件提示（默认为true）
        note.addBooleanProperty("isBackupFileOnlyWifi");    // 仅Wi-Fi环境下自动备份文件（默认为true）
        note.addIntProperty("fileOrderType");               // 文件排序类型（默认为0：文件名）
        note.addIntProperty("fileViewerType");              // 文件视图类型（默认为0：列表）
        note.addLongProperty("time");                       // 最后更新时间
    }

    /**
     * 备份文件表
     *
     * @param schema
     */
    private static void addBackupFileTable(Schema schema) {
        Entity note = schema.addEntity("BackupFile");
        note.addIdProperty().autoincrement();
        note.addLongProperty("uid").notNull();                  // 用户信息表中的ID
        note.addStringProperty("path").notNull();               // 备份路径
        note.addBooleanProperty("auto");                        // 自动备份（默认为true）
        note.addIntProperty("type");                            // 备份类型： 图片/视频、全部
        note.addIntProperty("priority");                        // 备份优先级： 1 > 2 > 3 ...
        note.addLongProperty("time");                           // 备份时间点
        note.addLongProperty("count");                          // 备份次数
    }

    /**
     * 备份信息表：通讯录/短信
     *
     * @param schema
     */
    private static void addBackupInfoTable(Schema schema) {
        Entity note = schema.addEntity("BackupInfo");
        note.addIdProperty().autoincrement();
        note.addLongProperty("uid");        // 用户信息表中的ID
        note.addIntProperty("type");        // 备份类型： 通讯录/短信
        note.addLongProperty("count");      // 备份次数
        note.addLongProperty("time");       // 最后备份时间
    }

    /**
     * 上传下载记录表
     *
     * @param schema
     */
    private static void addTransferHistoryTable(Schema schema) {
        Entity note = schema.addEntity("TransferHistory");
        note.addIdProperty().autoincrement();
        note.addLongProperty("uid");        // 用户信息表中的ID
        note.addIntProperty("type");        // 传输类型： 上传/下载
        note.addStringProperty("name").notNull();           // 文件名
        note.addStringProperty("srcPath").notNull();        // 原文件路径
        note.addStringProperty("toPath").notNull();         // 目标文件路径
        note.addLongProperty("size");       // 文件大小
        note.addLongProperty("length");     // 传输大小
        note.addLongProperty("duration");   // 用时（s）
        note.addLongProperty("time");       // 最后更新时间
        note.addBooleanProperty("isComplete");              // 是否完成（默认：false）
    }
}
