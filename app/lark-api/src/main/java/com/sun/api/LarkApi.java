package com.sun.api;

import com.alibaba.fastjson.JSONObject;
import com.sun.config.httpclient.HttpClientService;
import com.sun.bean.common.ResultBody;
import com.sun.bean.pojo.Dept;
import com.sun.bean.pojo.User;
import com.sun.constant.ApiEnum;
import com.sun.constant.HeaderConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LarkApi {

    // 部门 API
    private final String dept_api = "https://open.feishu.cn/open-apis/contact/v3/departments";
    // 用户 API
    private final String user_api = "https://open.feishu.cn/open-apis/contact/v3/users";
    // 审批事件
    private final String subscribe_api = "https://www.feishu.cn/approval/openapi/v2/subscription/subscribe";
    private final String cancel_subscribe_api = "https://www.feishu.cn/approval/openapi/v2/subscription/unsubscribe";
    private final String approve_api = "https://www.feishu.cn/approval/openapi/v2/instance/get";
    // 消息 API
    private final String message_api = "https://open.feishu.cn/open-apis/message/v4/send/";


    @Value("${lark.app_id}")
    private String appId;

    @Value("${lark.app_secret}")
    private String appSecret;

    @Value("${lark.open_id}")
    private String openId;

    private final HttpClientService httpClientService;

    @Autowired
    public LarkApi(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    /**
     * 获取飞书企业自建应用的token值
     *
     * @return token
     */
    public String get_access_token() {
        // 参数定义
        Header[] headers = HeaderConstant.getHeaderStandard();
        Map<String, Object> map = new HashMap<>();
        map.put("app_id", appId);
        map.put("app_secret", appSecret);

        CloseableHttpResponse response = null;
        StringBuilder app_access_token = new StringBuilder("");
        try {
            response = httpClientService.doPost(ApiEnum.APP_ACCESS_TOKEN.getApi(), headers, map);

            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.info("内容：" + str);
                JSONObject jsonObject = JSONObject.parseObject(str);

                app_access_token.append(jsonObject.get("app_access_token").toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return app_access_token.toString();
    }

    /**
     * 创建飞书远程部门
     *
     * @param dept 部门参数
     * @return 对创建是否成功进行返回
     */
    public ResultBody createDept(Dept dept) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("name", dept.getName());
        map.put("parent_department_id", dept.getParentDepartmentId());
        map.put("department_id", dept.getDepartmentId());

        CloseableHttpResponse response;
        try {
            String api = dept_api + "?department_id_type=department_id";
            response = httpClientService.doPost(api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("创建部门成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 远程API更新部门数据
     *
     * @param dept 部门参数
     * @return 是否成功进行返回
     */
    public ResultBody updateDept(Dept dept) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        // 设置请求体参数
        map.put("name", dept.getName());
        map.put("parent_department_id", dept.getParentDepartmentId());

        CloseableHttpResponse response;
        try {
            // 拼接路径参数和查询参数
            String api = dept_api + "/" + dept.getDepartmentId() + "?department_id_type=department_id";
            response = httpClientService.doPatch(api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("更新部门成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 远程API删除部门
     *
     * @param id 部门ID
     * @return 是否成功进行返回
     */
    public ResultBody deleteDept(String id) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        CloseableHttpResponse response;
        try {
            // 拼接路径参数和查询参数
            String api = dept_api + "/" + id + "?department_id_type=department_id";
            response = httpClientService.doDelete(api, headers);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("删除部门成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 远程API创建用户
     *
     * @param user 用户实体信息
     * @return
     */
    public ResultBody createUser(User user) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("name", user.getName());
        map.put("mobile", user.getMobile());
        map.put("email", user.getEmail());
        map.put("user_id", user.getUserId());
        map.put("employee_type", user.getEmployeeType());
        map.put("department_ids", new String[]{user.getDepartmentId()});

        CloseableHttpResponse response;
        try {
            String api = user_api + "?department_id_type=department_id";
            response = httpClientService.doPost(api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("创建用户成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 远程更新用户信息
     *
     * @param user 用户信息
     * @return
     */
    public ResultBody updateUser(User user) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        // 设置请求体参数
        map.put("name", user.getName());
        map.put("mobile", user.getMobile());
        map.put("email", user.getEmail());
        map.put("user_id", user.getUserId());
        map.put("employee_type", user.getEmployeeType());
        map.put("department_ids", new String[]{user.getDepartmentId()});

        CloseableHttpResponse response;
        try {
            String api = user_api + "/" + user.getUserId() + "?user_id_type=user_id&department_id_type=department_id";
            // 拼接路径参数和查询参数
            response = httpClientService.doPatch(api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("更新用户成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 远程删除用户
     *
     * @param id 用户ID
     * @return
     */
    public ResultBody deleteUser(String id) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        CloseableHttpResponse response;
        try {
            // 拼接路径参数和查询参数
            String api = user_api + "/" + id + "?user_id_type=user_id";
            response = httpClientService.doDelete(api, headers);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("删除用户成功" + str);
                    return ResultBody.success();
                } else {
                    log.info(str);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 订阅对应的审批事件
     *
     * @param approvalCode
     * @return
     */
    public ResultBody subscribeApprove(String approvalCode) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("approval_code", approvalCode);
        CloseableHttpResponse response = null;
        try {
            // 拼接路径参数和查询参数
            response = httpClientService.doPost(subscribe_api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("获取对应的数据" + str);
                    return ResultBody.success(jsonObject);
                } else {
                    log.info(str);
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("text", jsonObject.get("msg").toString());
                    this.sendMessage(map1);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResultBody.error("未知错误");
    }

    /**
     * 取消订阅对应的审批事件
     *
     * @param approvalCode
     * @return
     */
    public ResultBody cancelSubscribeApprove(String approvalCode) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("approval_code", approvalCode);
        CloseableHttpResponse response = null;
        try {
            // 拼接路径参数和查询参数
            response = httpClientService.doPost(cancel_subscribe_api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("获取对应的数据" + str);
                    return ResultBody.success(jsonObject);
                } else {
                    log.info(str);
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("text", jsonObject.get("msg").toString());
                    this.sendMessage(map1);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResultBody.error("未知错误");
    }

    public void sendMessage(Map<String, Object> content) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("open_id", openId);
        map.put("msg_type", "text");
        map.put("content", content);
        CloseableHttpResponse response = null;
        try {
            String api = message_api;
            // 拼接路径参数和查询参数
            response = httpClientService.doPost(api, headers, map);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultBody getInstance(String instanceCode) {
        String token = this.get_access_token();
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json; charset=utf-8"),
                new BasicHeader("Authorization", "Bearer " + token)};
        Map<String, Object> map = new HashMap<>();
        map.put("instance_code", instanceCode);
        CloseableHttpResponse response = null;
        try {
            // 拼接路径参数和查询参数
            response = httpClientService.doPost(approve_api, headers, map);
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(str);
                if (jsonObject.get("code").equals(0)) {
                    log.info("获取对应的数据" + str);
                    return ResultBody.success(jsonObject);
                } else {
                    log.info(str);
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("text", jsonObject.get("msg").toString());
                    this.sendMessage(map1);
                    return ResultBody.error(jsonObject.get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResultBody.error("未知错误");
    }
}
