package com.joking.yatian.controller;

import com.joking.yatian.service.DiscussPostService;
import com.joking.yatian.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Joking7
 * @ClassName testController
 * @description: TODO
 * @date 2024/7/25 上午1:57
 */
@RestController
public class testController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/test")
    public String test() {
        System.out.println(redisTemplate.opsForValue().get(RedisKeyUtil.getPostRowsKey()));
        String ans = String.valueOf(discussPostService.findDiscussPostRows(0));
        return ans;
    }
}
