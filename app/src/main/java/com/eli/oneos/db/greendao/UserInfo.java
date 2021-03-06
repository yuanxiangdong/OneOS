package com.eli.oneos.db.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

/**
 * Entity mapped to table USER_INFO.
 */
public class UserInfo {

    private Long id;
    /**
     * Not-null value.
     */
    private String name;
    /**
     * Not-null value.
     */
    private String mac;
    /**
     * Not-null value.
     */
    private String pwd;
    private Integer admin;
    private Integer uid;
    private Integer gid;
    private Integer domain;
    private Long time;
    private Boolean isLogout;
    private Boolean isActive;

    public UserInfo() {
    }

    public UserInfo(Long id) {
        this.id = id;
    }

    public UserInfo(Long id, String name, String mac, String pwd, Integer admin, Integer uid, Integer gid, Integer domain, Long time, Boolean isLogout, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.mac = mac;
        this.pwd = pwd;
        this.admin = admin;
        this.uid = uid;
        this.gid = gid;
        this.domain = domain;
        this.time = time;
        this.isLogout = isLogout;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Not-null value.
     */
    public String getName() {
        return name;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Not-null value.
     */
    public String getMac() {
        return mac;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * Not-null value.
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Integer getAdmin() {
        return admin;
    }

    public void setAdmin(Integer admin) {
        this.admin = admin;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public Integer getDomain() {
        return domain;
    }

    public void setDomain(Integer domain) {
        this.domain = domain;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getIsLogout() {
        return isLogout;
    }

    public void setIsLogout(Boolean isLogout) {
        this.isLogout = isLogout;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return String.format("{id: %d, name: %s, mac: %s, pwd: %s, admin: %d, uid: %d, gid: %d, domain: %d, time: %d, isLogout: %s}", this.id, this.name, this.mac, this.pwd, this.admin, this.uid, this.gid, this.domain, this.time, this.isLogout);
    }
}
