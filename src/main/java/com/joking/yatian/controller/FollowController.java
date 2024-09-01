package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Event;
import com.joking.yatian.entity.Page;
import com.joking.yatian.entity.User;
import com.joking.yatian.event.EventProducer;
import com.joking.yatian.service.FollowService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName FollowController
 * @description: 关注 Controller层
 * @date 2024/8/3 下午10:30
 */
@CrossOrigin
@RestController
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private JwtUtil jwtUtil;


    /**
     * @MethodName: follow
     * @Description: 关注某实体
     * @param entityType
     * @param entityId
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午9:25
     */
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    public JSONObject follow(@RequestParam("entityType") int entityType,
                             @RequestParam("entityId") int entityId,
                             @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));

        followService.follow(user.getId(), entityType, entityId);

        // 注意：目前只实现了关注人，所以setEntityUserId为entityId
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(200, "已关注!");
    }

    /**
     * @MethodName: unfollow
     * @Description: 取消关注某实体
     * @param entityType
     * @param entityId
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午9:26
     */
    @RequestMapping(path = "/unfollow", method = RequestMethod.DELETE)
    public JSONObject unfollow(@RequestParam("entityType") int entityType,
                               @RequestParam("entityId") int entityId,
                               @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(200, "已取消关注!");
    }

    /**
     * @MethodName: getFollowees
     * @Description: 查询此用户关注的实体
     * @param userId
     * @param pageCurrent
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午9:26
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public JSONObject getFollowees(@PathVariable("userId") int userId,
                               @RequestParam("pageCurrent") int pageCurrent) {
        User user = userService.findUserById(userId);
        if (user == null) {
            return CommunityUtil.getJSONString(404,"该用户不存在!");
        }
        //model.addAttribute("user", user);

        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user,u.getId()));
            }
        }
        //model.addAttribute("users", userList);

        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("page", page);
        response.put("users", userList);
        response.put("user",user);
        return response;
    }

    /**
     * @MethodName: getFollowers
     * @Description: 查询关注此用户的实体
     * @param userId
     * @param pageCurrent
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午9:28
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public JSONObject getFollowers(@PathVariable("userId") int userId,
                                   @RequestParam("pageCurrent") int pageCurrent) {
        User user = userService.findUserById(userId);
        if (user == null) {
            return CommunityUtil.getJSONString(404,"该用户不存在!");
        }
        //model.addAttribute("user", user);

        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user,u.getId()));
            }
        }
        //model.addAttribute("users", userList);
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("page", page);
        response.put("users", userList);
        response.put("user",user);
        return response;
    }

    private boolean hasFollowed(User user,int userId) {

        if (user == null) {
            return false;
        }

        return followService.hasFollowed(user.getId(), ENTITY_TYPE_USER, userId);
    }
}
