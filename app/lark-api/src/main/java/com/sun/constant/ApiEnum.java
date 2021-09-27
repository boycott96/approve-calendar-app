package com.sun.constant;

import java.util.Map;

public enum ApiEnum {

    /*
    API
     */
    APP_ACCESS_TOKEN("https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal"), // 获取APP凭证
    APPROVE_API("https://www.feishu.cn/approval/openapi/v2/instance/get"),// 获取审批实例详情
    CANCEL_SUBSCRIBE_APPROVE_API("https://www.feishu.cn/approval/openapi/v2/subscription/unsubscribe"), // 取消订阅某个审批事件
    SUBSCRIBE_APPROVE_API("https://www.feishu.cn/approval/openapi/v2/subscription/subscribe"); // 订阅某个审批事件

    /**
     * 属性
     */
    private final String api;

    ApiEnum(String api) {
        this.api = api;
    }

    public String getApi() {
        return api;
    }
}
