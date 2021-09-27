package com.sun.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.api.LarkApi;
import com.sun.bean.common.CodeEnum;
import com.sun.bean.common.ResultBody;
import com.sun.config.websocket.WebsocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 对所有的请求，进行一个接收
 */
@RestController
@RequestMapping("/")
@Slf4j
public class AnyController {

    private final String verification_type = "url_verification";

    private final WebsocketServer websocketServer;

    private final LarkApi larkApi;

    @Autowired
    public AnyController(WebsocketServer websocketServer, LarkApi larkApi) {
        this.websocketServer = websocketServer;
        this.larkApi = larkApi;
    }

    /**
     * 所有请求的通道，对不同的飞书反馈请求，进行处理
     * 订阅请求进行处理
     *
     * @return
     */
    @RequestMapping()
    public Object accessRequest(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            // 将请求转化成输入流
            ServletInputStream inputStream = request.getInputStream();

            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String str; // 初始化
            StringBuilder value = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                value.append(str);
            }
            log.info(value.toString());
            JSONObject jsonObject = JSONObject.parseObject(value.toString());
            // 1.飞书access——token，2.获取消息token的反馈
            if (jsonObject != null) {
                if (verification_type.equals(jsonObject.get("type"))) {
                    map.put("challenge", jsonObject.get("challenge"));
                } else if ("event_callback".equals(jsonObject.get("type"))) {
                    // 处理请求体
                    JSONObject event = jsonObject.getJSONObject("event");
                    // 判断是否是消息
                    if ("message".equals(event.get("type"))) {
                        // 判断内容是否为费用报销订阅
                        if ("订阅费用报销".equals(event.get("text"))) {
                            // 订阅费用报销
                            ResultBody resultBody = larkApi.subscribeApprove("8EB4B367-8318-4C98-94D2-BE83E607BA21");
                            if (resultBody.getCode().equals(CodeEnum.SUCCESS.getResultCode())) {
                                Map<String, Object> textMap = new HashMap<>();
                                textMap.put("text", "费用报销审批，已经成功订阅");
                                larkApi.sendMessage(textMap);
                            }
                        } else if ("取消订阅费用报销".equals(event.get("text"))) {
                            ResultBody resultBody = larkApi.cancelSubscribeApprove("8EB4B367-8318-4C98-94D2-BE83E607BA21");
                            if (resultBody.getCode().equals(CodeEnum.SUCCESS.getResultCode())) {
                                Map<String, Object> textMap = new HashMap<>();
                                textMap.put("text", "费用报销审批，已经成功取消订阅");
                                larkApi.sendMessage(textMap);
                            }
                        }
                    } else if ("approval_instance".equals(event.get("type")) || "approval_task".equals(event.get("type"))) {
                        // 判断是否是审批实例
                        Map<String, Object> textMap = new HashMap<>();
                        String status = event.get("status").toString();
                        StringBuilder result = new StringBuilder("审批状态: " + status);
                        if (status.equals("APPROVED")) {
                            ResultBody resultBody = larkApi.getInstance(event.get("instance_code").toString());
                            if (resultBody.getCode().equals(CodeEnum.SUCCESS.getResultCode())) {
                                // 获取审批意见
                                JSONObject res = (JSONObject) resultBody.getResult();
                                JSONArray jsonArray = res.getJSONObject("data").getJSONArray("timeline");
                                jsonArray.stream().forEach(item -> {
                                    JSONObject jsonItem = (JSONObject) item;
                                    if (jsonItem.get("type").equals("PASS")) {
                                        result.append(",审批意见: ")
                                                .append(jsonItem.get("comment") == null ? "暂无" : jsonItem.get("comment"));
                                    }
                                });

                            }

                        }
                        textMap.put("text", result.toString());
                        larkApi.sendMessage(textMap);
                    }

                } else if (jsonObject.get("type") == null) {
                    // 获取消息卡片的数据
                    String openId = jsonObject.get("open_id").toString();
                    JSONObject actionValue = jsonObject.getJSONObject("action").getJSONObject("value");
                    if ("ok".equals(actionValue.get("status"))) {
                        // 获取消息卡片的同意反馈
                        this.websocketServer.sendInfo(openId, "OK");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return map;
    }
}
