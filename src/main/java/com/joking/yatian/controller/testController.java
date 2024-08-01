package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.*;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.*;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.JwtUtil;
import com.joking.yatian.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping (path = "/test/{discussPostId}")
    public JSONObject getDiscussPost(@PathVariable("discussPostId") int discussPostId,@RequestHeader("userToken") String token) {
        JSONObject response = CommunityUtil.getJSONString(200);
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        //model.addAttribute("post", post);

        // 处理贴子被删除的情况
        if(post.getStatus()==2) return CommunityUtil.getJSONString(404,"该内容已被删除!");

        if (post == null) {
            response = CommunityUtil.getJSONString(404,"没有找到贴子");
            return response;
        }
        response.put("discussPost", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        //model.addAttribute("user", user);
        response.put("user", user);

        User thisUser = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        response.put("likeCount", likeCount);
        //model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = thisUser == null ? 0 :
                likeService.findEntityLikeStatus(ENTITY_TYPE_POST, discussPostId, thisUser.getId());
        response.put("likeStatus", likeStatus);
        //model.addAttribute("likeStatus", likeStatus);

        Page page = new Page();
        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        response.put("page", page);

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                likeStatus = thisUser == null ? 0 : likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, comment.getId(), thisUser.getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                        likeStatus = thisUser == null ? 0 : likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, reply.getId(), thisUser.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        response.put("comments", commentVoList);
        //model.addAttribute("comments", commentVoList);

        return response;
    }
}
