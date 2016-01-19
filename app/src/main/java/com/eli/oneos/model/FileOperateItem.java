package com.eli.oneos.model;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/19.
 */
public class FileOperateItem {

    private int id = 0;
    private int normalIcon = 0;
    private int pressedIcon = 0;
    private int txtId = 0;

    public FileOperateItem(int id, int normalIcon, int pressedIcon, int txtId) {
        this.id = id;
        this.normalIcon = normalIcon;
        this.pressedIcon = pressedIcon;
        this.txtId = txtId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNormalIcon() {
        return normalIcon;
    }

    public void setNormalIcon(int normalIcon) {
        this.normalIcon = normalIcon;
    }

    public int getPressedIcon() {
        return pressedIcon;
    }

    public void setPressedIcon(int pressedIcon) {
        this.pressedIcon = pressedIcon;
    }

    public int getTxtId() {
        return txtId;
    }

    public void setTxtId(int txtId) {
        this.txtId = txtId;
    }
}
