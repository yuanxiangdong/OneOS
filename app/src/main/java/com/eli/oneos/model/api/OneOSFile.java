package com.eli.oneos.model.api;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class OneOSFile {
//    {"perm":"rwxr-xr-x","type":"audio","name":"haizeiw .mp3","gid":0,"path":"\/haizeiw .mp3","uid":1001,"time":1187168313,"size":6137050}

    private String path = null;
    private String perm = null;
    private String type = null;
    private String name = null;
    private int gid = 0;
    private int uid = 0;
    private long time = 0;
    private long size = 0;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPerm() {
        return perm;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
