package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.*;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.*;
import com.joking.yatian.util.*;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author Joking7
 * @ClassName testController
 * @description: TODO
 * @date 2024/7/25 上午1:57
 */
@RestController
public class testController implements CommunityConstant {
    @Autowired
    private EventProducer eventProducer;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public JSONObject share(@RequestParam("htmlUrl") String htmlUrl) {
        // 文件名
        String fileName = CommunityUtil.generateUUID();

        // 异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        JSONObject response = CommunityUtil.getJSONString(200,"生成长图成功!");
        response.put("shareUrl", shareBucketUrl + "/" + fileName);

        return response;
    }

    @RequestMapping(path = "/test2", method = RequestMethod.GET)
    public JSONObject test2() {
        return CommunityUtil.getJSONString(200,"ceshi2");
    }

    @RequestMapping(path = "/test3", method = RequestMethod.GET)
    public JSONObject test3() {
        return CommunityUtil.getJSONString(200,"ceshi3");
    }

    @RequestMapping(path = "/test1", method = RequestMethod.GET)
    public JSONObject test1() {
        return CommunityUtil.getJSONString(200,"ceshi1");
    }
}
