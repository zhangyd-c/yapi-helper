package com.xkcoding.idea.plugins.yapi_helper.yapi.enums;

public enum ResponseBodyTypeEnum {

    JSON("json"),
    RAW("raw");

    private String value;

    ResponseBodyTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
