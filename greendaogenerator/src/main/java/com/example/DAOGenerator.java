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

        addUserInfo(schema);
        addDeviceInfo(schema);
        addBackupInfo(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void addUserInfo(Schema schema) {
        Entity note = schema.addEntity("UserInfo");
        note.addIdProperty().autoincrement();
        note.addStringProperty("name").notNull(); //.primaryKey();
        note.addStringProperty("pwd").notNull();
        note.addStringProperty("mac").notNull(); //.primaryKey();
        note.addLongProperty("time");
        note.addIntProperty("uid");
        note.addIntProperty("gid");
        note.addIntProperty("admin");
        note.addStringProperty("downloadPath");
        note.addBooleanProperty("isPreviewPicOnlyWifi"); // default is true
        note.addBooleanProperty("isTipTransferNotWifi"); // default is true
        note.addBooleanProperty("isAutoBackup");  //default is false
        note.addBooleanProperty("isBackupOnlyWifi"); // default is true
    }

    private static void addDeviceInfo(Schema schema) {
        Entity note = schema.addEntity("DeviceHistory");
        note.addStringProperty("ip").notNull().primaryKey();
        note.addStringProperty("mac").notNull();
        note.addStringProperty("port").notNull();
        note.addLongProperty("time");
        note.addBooleanProperty("isLAN");
    }

    private static void addBackupInfo(Schema schema) {
        Entity note = schema.addEntity("BackupInfo");
        note.addIdProperty().autoincrement();
        note.addStringProperty("mac").notNull();
        note.addStringProperty("user").notNull();
        note.addStringProperty("path").notNull();
        note.addLongProperty("time"); // last backup time
        note.addLongProperty("count"); // backup total count
        note.addIntProperty("priority"); // backup priority, 1 > 2 > 3 ...
        note.addStringProperty("type"); // backup file type, such as picture
    }

//    private static void addCustomerOrder(Schema schema) {
//        Entity customer = schema.addEntity("Customer");
//        customer.addIdProperty();
//        customer.addStringProperty("name").notNull();
//
//        Entity order = schema.addEntity("Order");
//        order.setTableName("ORDERS"); // "ORDER" is a reserved keyword
//        order.addIdProperty();
//        Property orderDate = order.addDateProperty("date").getProperty();
//        Property customerId = order.addLongProperty("customerId").notNull().getProperty();
//        order.addToOne(customer, customerId);
//
//        ToMany customerToOrders = customer.addToMany(order, customerId);
//        customerToOrders.setName("orders");
//        customerToOrders.orderAsc(orderDate);
//    }
}
