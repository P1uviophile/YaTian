package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.DiscussPost;
import com.joking.yatian.entity.Page;
import com.joking.yatian.entity.User;
import com.joking.yatian.service.DiscussPostService;
import com.joking.yatian.service.LikeService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName HomeController
 * @description: 论坛主页Controller
 * @date 2024/8/2 下午2:48
 */
@RestController
@CrossOrigin
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    /**
     * @MethodName: getIndexPage
     * @Description: 获取主页信息
     * @param orderMode
     * @param pageCurrent
     * @param pageLimit
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:20
     */
    @RequestMapping(path = {"/index","/"}, method = RequestMethod.GET)
    public JSONObject getIndexPage(@RequestParam(name = "orderMode", defaultValue = "1") int orderMode,
                                   @RequestParam("pageCurrent") int pageCurrent,
                                   @RequestParam("pageLimit") int pageLimit) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        Page page = new Page();
        page.setLimit(pageLimit);
        page.setCurrent(pageCurrent);
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                //点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("discussPosts", discussPosts);
        response.put("page", page);
        response.put("orderMode", orderMode);

        //model.addAttribute("discussPosts", discussPosts);
        //model.addAttribute("orderMode", orderMode);

        return response;
    }

}
