package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Comment;
import com.joking.yatian.entity.DiscussPost;
import com.joking.yatian.entity.Event;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.CommentService;
import com.joking.yatian.service.DiscussPostService;
import com.joking.yatian.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author Joking7
 * @ClassName CommentController
 * @description: 评论Controller
 * @date 2024/8/2 下午3:32
 */
@RestController
@CrossOrigin
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @MethodName: addComment
     * @Description: 发布贴子
     * @param discussPostId
     * @param content
     * @param userToken
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:20
     */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public JSONObject addComment(@PathVariable("discussPostId") int discussPostId,
                                 @RequestParam("comment") String content, @RequestHeader("userToken") String userToken) {

        int userId = Integer.parseInt(jwtUtil.parseToken(userToken).get("userId"));
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setContent(content);
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(userId)
                .setEntityType(comment.getEntityType())
                .setEntityUserId(comment.getEntityId())
                .setData("postId", discussPostId);

        if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 触发发帖事件，因为评论帖子时，帖子的评论数量就更改了，需要更新elasticsearch中的数据
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(userId)
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }

        return CommunityUtil.getJSONString(200,"评论成功!");
    }
}
