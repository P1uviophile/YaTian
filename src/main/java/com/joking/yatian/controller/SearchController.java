package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.DiscussPost;
import com.joking.yatian.entity.Page;
import com.joking.yatian.service.ElasticsearchService;
import com.joking.yatian.service.LikeService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joking7
 * @ClassName SearchController
 * @description: 站内搜索Controller
 * @date 2024/8/3 下午10:31
 */
@RestController
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * @MethodName: search
     * @Description: 关键词搜索
     * @param keyword
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 上午1:39
     */
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public JSONObject search(@RequestParam("keyword") String keyword,
                             @RequestParam("pageCurrent") int pageCurrent,
                             @RequestParam("pageLimit") int pageLimit) {
        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(pageLimit);
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResults = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResults != null) {
            for (DiscussPost post : searchResults) {
                Map<String, Object> map = new HashMap<>();

                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findUserLikeCount(post.getUserId()));

                discussPosts.add(map);
            }
        }
        //model.addAttribute("discussPosts", discussPosts);
        //model.addAttribute("keyword", keyword);

        page.setPath("search?keyword=" + keyword);
        page.setRows(searchResults == null ? 0 : (int) searchResults.getTotalElements());

        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("discussPosts", discussPosts);
        response.put("keyword", keyword);
        response.put("page", page);
        return response;
    }
}
