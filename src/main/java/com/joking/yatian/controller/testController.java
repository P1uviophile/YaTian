package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Event;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.DiscussPostService;
import com.joking.yatian.service.ElasticsearchService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName testController
 * @description: TODO
 * @date 2024/7/25 上午1:57
 */
@RestController
public class testController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @PostMapping(path = "/test")
    public JSONObject share(@RequestParam("html") String htmlUrl) {
        System.out.println(htmlUrl);
        // 文件名
        String fileName = CommunityUtil.generateUUID();

        // 异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);
        System.out.println(event.getData().toString());

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();

        // 废弃，这是原来本地的方式
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);

        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }
}
