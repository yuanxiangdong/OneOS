package com.eli.oneos.model.oneos.transfer;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public enum TransferException {
    NONE,
    UNKNOW_EXCEPTION,
    LOCAL_SPACE_INSUFFICIENT,
    REQUEST_SERVER,
    ENCODING_EXCEPTION,
    IO_EXCEPTION,
    FILE_NOT_FOUND,
    SOCKET_TIMEOUT,
    SERVER_SPACE_INSUFFICIENT,
    WIFI_UNAVALIABLE
}
