package com.xkcoding.idea.plugins.yapi_helper.yapi.enums;

public enum RequestBodyTypeEnum {

    FORM("form"),
    JSON("json"),
    RAW("raw");

    private String value;

    RequestBodyTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
