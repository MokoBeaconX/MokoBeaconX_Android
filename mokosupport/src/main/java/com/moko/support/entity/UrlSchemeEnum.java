package com.moko.support.entity;


import java.io.Serializable;

public enum UrlSchemeEnum implements Serializable {
    HTTP_WWW(0x00, "http://www."),
    HTTPS_WWW(0x01, "https://www."),
    HTTP(0x02, "http://"),
    HTTPS(0x03, "https://");

    private int urlType;
    private String urlDesc;

    UrlSchemeEnum(int urlType, String urlDesc) {
        this.urlType = urlType;
        this.urlDesc = urlDesc;
    }


    public int getUrlType() {
        return urlType;
    }

    public String getUrlDesc() {
        return urlDesc;
    }

    public static UrlSchemeEnum fromUrlType(int urlType) {
        for (UrlSchemeEnum urlSchemeEnum : UrlSchemeEnum.values()) {
            if (urlSchemeEnum.getUrlType() == urlType) {
                return urlSchemeEnum;
            }
        }
        return null;
    }

    public static UrlSchemeEnum fromUrlDesc(String urlDesc) {
        for (UrlSchemeEnum urlSchemeEnum : UrlSchemeEnum.values()) {
            if (urlSchemeEnum.getUrlDesc().equals(urlDesc)) {
                return urlSchemeEnum;
            }
        }
        return null;
    }
}
