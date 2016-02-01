package com.eli.oneos.model.user;

public class UserInfo {

    private String name;
    private String pwd;
    private Long time;
    private Integer uid;
    private Integer gid;
    private Integer admin;

    public UserInfo() {
    }

    public UserInfo(String name) {
        this.name = name;
    }

    public UserInfo(String name, String pwd, Long time, Integer uid, Integer gid, Integer admin) {
        this.name = name;
        this.pwd = pwd;
        this.time = time;
        this.uid = uid;
        this.gid = gid;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getAdmin() {
        return admin;
    }

    public void setAdmin(Integer admin) {
        this.admin = admin;
    }

}
