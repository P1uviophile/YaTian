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

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

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
    public int getIndexPage(@RequestParam(name = "orderMode", defaultValue = "1") int orderMode,
                                   @RequestParam("pageCurrent") int pageCurrent,
                                   @RequestParam("pageLimit") int pageLimit) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, 0, 5,orderMode);

        //model.addAttribute("discussPosts", discussPosts);
        //model.addAttribute("orderMode", orderMode);

        return 1;
    }
}
