package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Event;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName ShareController
 * @description: 分享 Controller
 * @date 2024/8/3 下午10:30
 */
@CrossOrigin
@RestController
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    /**
     * @MethodName: share
     * @Description: 生成分享长图 返回图片url
     * @param htmlUrl
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:19
     */
    @RequestMapping(path = "/share", method = RequestMethod.POST)
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
}
