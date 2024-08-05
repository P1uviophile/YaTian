package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Event;
import com.joking.yatian.entity.User;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.LikeService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName LikeController
 * @description: 点赞 Controller
 * @date 2024/8/3 下午10:30
 */
@CrossOrigin
@RestController
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @MethodName: like
     * @Description: 给实体点赞
     * @param entityType
     * @param entityId
     * @param entityUserId
     * @param postId
     * @param userToken
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:19
     */
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    public JSONObject like(@RequestParam("entityType") int entityType,@RequestParam("entityType") int entityId,
                           @RequestParam("entityType")int entityUserId,@RequestParam("entityType") int postId,
                           @RequestHeader("userToken") String userToken) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(userToken).get("userId")));
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //点赞状态
        int likeStatus = likeService.findEntityLikeStatus(entityType, entityId, user.getId());
        //返回的结果
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("likeCount", likeCount);
        response.put("likeStatus", likeStatus);

        // 触发点赞事件,仅点赞需要通知
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);

            if (entityType == ENTITY_TYPE_POST) {
                // 计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey, postId);
            }
        }
        return response;
    }
}
