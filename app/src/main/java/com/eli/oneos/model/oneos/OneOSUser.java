package com.eli.oneos.model.oneos;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/24.
 */
public class OneOSUser {
    private String name = null;
    private int uid = 0;
    private int gid = 0;
    private long used = 0;
    private long space = 0;

    public OneOSUser(String name, int uid, int gid) {
        this.name = name;
        this.uid = uid;
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getSpace() {
        return space;
    }

    public void setSpace(long space) {
        this.space = space;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }
}
