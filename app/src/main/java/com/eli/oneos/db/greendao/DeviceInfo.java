package com.eli.oneos.db.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table DEVICE_INFO.
 */
public class DeviceInfo {

    /** Not-null value. */
    private String mac;
    /** Not-null value. */
    private String ip;
    /** Not-null value. */
    private String port;
    private Boolean isLAN;
    private Long time;

    public DeviceInfo() {
    }

    public DeviceInfo(String mac) {
        this.mac = mac;
    }

    public DeviceInfo(String mac, String ip, String port, Boolean isLAN, Long time) {
        this.mac = mac;
        this.ip = ip;
        this.port = port;
        this.isLAN = isLAN;
        this.time = time;
    }

    /** Not-null value. */
    public String getMac() {
        return mac;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /** Not-null value. */
    public String getIp() {
        return ip;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /** Not-null value. */
    public String getPort() {
        return port;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPort(String port) {
        this.port = port;
    }

    public Boolean getIsLAN() {
        return isLAN;
    }

    public void setIsLAN(Boolean isLAN) {
        this.isLAN = isLAN;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
