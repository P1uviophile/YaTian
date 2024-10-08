package com.joking.yatian.util;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CommunityUtil 论坛工具类
 * @author Joking7
 * @version 2024/07/18 
**/
public class CommunityUtil {
    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static void addJSONString(JSONObject json, Map<String, Object> map) {
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
    }

    public static String getJSONString( String key,String str) {
        JSONObject json = new JSONObject();
        json.put(key, str);
        return json.toJSONString();
    }

    public static JSONObject getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json;
    }

    public static JSONObject getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static JSONObject getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
