package com.xkcoding.idea.plugins.yapi_helper.constant;

public enum CUrlClientType {

    CMD, BASH;

    public String getSymbolAnd() {
        switch (this) {
            case CMD:
                return "^&";
            case BASH:
                return "&";
        }
        return "";
    }

}
