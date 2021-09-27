package com.sun.constant;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.HashMap;
import java.util.Map;

public class HeaderConstant {

    /**
     * 获取标准化得头部参数
     *
     * @return header
     */
    public static Header[] getHeaderStandard() {
        return new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8")};
    }

    /**
     * 通过参数得形式进行设置token值
     *
     * @param token 飞书 access_token
     * @return header
     */
    public Header[] setHeader(String token) {
        return new Header[]{
                new BasicHeader("Authorization", "Bearer " + token),
                new BasicHeader("Content-Type", "application/json; charset=utf-8")};
    }

    /**
     * 直接获取redis数据库中得值
     *
     * @return header
     */
    public Header[] getHeader() {
        Map<String, String> map = new HashMap<>();
        // todo
        return null;
    }
}
