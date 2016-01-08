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

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void addUserInfo(Schema schema) {
        Entity note = schema.addEntity("UserInfo");
        note.addIdProperty().primaryKey().autoincrement();
        note.addStringProperty("name").notNull();
        note.addStringProperty("pwd").notNull();
        note.addLongProperty("time");
        note.addIntProperty("uid");
        note.addIntProperty("gid");
        note.addIntProperty("admin");
    }

    private static void addDeviceInfo(Schema schema) {
        Entity note = schema.addEntity("DeviceInfo");
        note.addIdProperty().primaryKey().autoincrement();
        note.addStringProperty("mac").notNull();
        note.addStringProperty("ip").notNull();
        note.addStringProperty("port").notNull();
        note.addStringProperty("model").notNull();
        note.addStringProperty("version").notNull();
        note.addBooleanProperty("isLAN");
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
