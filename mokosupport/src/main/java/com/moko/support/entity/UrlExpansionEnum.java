package com.moko.support.entity;


import java.io.Serializable;

public enum UrlExpansionEnum implements Serializable {
    COM_SEPARATOR(0x00, ".com/"),
    ORG_SEPARATOR(0x01, ".org/"),
    EDU_SEPARATOR(0x02, ".edu/"),
    NET_SEPARATOR(0x03, ".net/"),
    INFO_SEPARATOR(0x04, ".info/"),
    BIZ_SEPARATOR(0x05, ".biz/"),
    GOV_SEPARATOR(0x06, ".gov/"),
    COM(0x07, ".com"),
    ORG(0x08, ".org"),
    EDU(0x09, ".edu"),
    NET(0x0a, ".net"),
    INFO(0x0b, ".info"),
    BIZ(0x0c, ".biz"),
    GOV(0x0d, ".gov");


    private int urlExpanType;
    private String urlExpanDesc;

    UrlExpansionEnum(int urlExpanType, String urlExpanDesc) {
        this.urlExpanType = urlExpanType;
        this.urlExpanDesc = urlExpanDesc;
    }


    public int getUrlExpanType() {
        return urlExpanType;
    }

    public String getUrlExpanDesc() {
        return urlExpanDesc;
    }

    public static UrlExpansionEnum fromUrlExpanType(int urlExpanType) {
        for (UrlExpansionEnum urlExpansionEnum : UrlExpansionEnum.values()) {
            if (urlExpansionEnum.getUrlExpanType() == urlExpanType) {
                return urlExpansionEnum;
            }
        }
        return null;
    }

    public static UrlExpansionEnum fromUrlExpanDesc(String urlExpanDesc) {
        for (UrlExpansionEnum urlExpansionEnum : UrlExpansionEnum.values()) {
            if (urlExpansionEnum.getUrlExpanDesc().equals(urlExpanDesc)) {
                return urlExpansionEnum;
            }
        }
        return null;
    }
}
