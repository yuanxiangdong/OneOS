package com.eli.oneos.db.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table SSUDPINFO.
 */
public class SSUDPInfo {

    /** Not-null value. */
    private String mac;
    private String name;
    private String cid;
    private String pwd;
    private Long time;

    public SSUDPInfo() {
    }

    public SSUDPInfo(String mac) {
        this.mac = mac;
    }

    public SSUDPInfo(String mac, String name, String cid, String pwd, Long time) {
        this.mac = mac;
        this.name = name;
        this.cid = cid;
        this.pwd = pwd;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
