package com.protel.network;

/**
 * Created by erdemmac on 30/11/15.
 */
public class MultiPartInfo {
    public String mediaType, name, fileName;
    public byte[] body;

    public MultiPartInfo(String mediaType, String name, String fileName, byte[] body) {
        this.body = body;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.name = name;
    }

    public MultiPartInfo(String name, String value) {
        this.body = null;
        this.fileName = value;
        this.mediaType = null;
        this.name = name;
    }
}
