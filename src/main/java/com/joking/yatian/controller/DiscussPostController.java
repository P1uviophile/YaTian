package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.*;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.*;
import com.joking.yatian.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Joking7
 * @ClassName DiscussPostController
 * @description: 帖子发布
 * @date 2024/8/1 下午3:50
 */
@CrossOrigin
@RestController
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
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

    /**
     * @MethodName: addDiscussPost
     * @Description: 新增贴子
     * @param token
     * @param title
     * @param content
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午1:49
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public JSONObject addDiscussPost(@RequestHeader("userToken") String token, @RequestParam("title") String title, @RequestParam("content") String content) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(200, "发布成功!");
    }

    /**
     * @MethodName: getDiscussPost
     * @Description: 获取贴子信息
     * @param discussPostId
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午1:56
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public JSONObject getDiscussPost(@PathVariable("discussPostId") int discussPostId,@RequestHeader("userToken") String token) {
        JSONObject response = CommunityUtil.getJSONString(200);
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        if(post.getStatus()==2) return CommunityUtil.getJSONString(404,"该内容已被删除!");
        //model.addAttribute("post", post);

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

    /**
     * @MethodName: setTop
     * @Description: 管理员置顶
     * @param id
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午2:11
     */
    // TODO SpringBoot Security 设置管理员权限
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    public JSONObject setTop(@RequestParam("discussPostId") int id) {
        discussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(0)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * @MethodName: setWonderful
     * @Description: 管理员加精
     * @param id
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午2:11
     */
    // TODO SpringBoot Security 设置管理员权限
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    public JSONObject setWonderful(@RequestParam("discussPostId")int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(0)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * @MethodName: setDelete
     * @Description: 管理员删帖
     * @param id
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午2:12
     */
    // TODO : SpringBoot Security 设置管理员权限
    @RequestMapping(path = "/deleteByAdmin", method = RequestMethod.POST)
    public JSONObject setDeleteByAdmin(@RequestParam("discussPostId")int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件 (现在不需要消息队列推送)
        /*
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
         */

        elasticsearchService.deleteDiscussPost(id);
        return CommunityUtil.getJSONString(200,"删除成功!");
    }


    /**
     * @MethodName: setDelete
     * @Description: 用户删帖
     * @param id
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/2 上午2:12
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public JSONObject setDelete(@RequestParam("discussPostId")int id,@RequestHeader("userToken") String token) {

        // 未登录
        if(token==null) return CommunityUtil.getJSONString(401,"无权限");

        // 判断是否本人删除
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        if(discussPostService.findDiscussPostById(id).getUserId()!=user.getId())
            return CommunityUtil.getJSONString(401,"无权限");

        // 删除
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件 (现在不需要消息队列推送)
        /*
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
         */

        elasticsearchService.deleteDiscussPost(id);
        return CommunityUtil.getJSONString(200,"删除成功!");
    }
}
