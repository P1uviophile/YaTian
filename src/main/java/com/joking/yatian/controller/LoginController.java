package com.joking.yatian.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.alibaba.fastjson2.JSONObject;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.Model;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.*;

import com.google.code.kaptcha.Producer;
import com.joking.yatian.entity.User;
import com.joking.yatian.util.CommunityConstant;

/**
 * @author coolsen
 * @version 1.0.0
 * @ClassName LoginController.java
 * @Description 注册，登录
 * @createTime 4/29/2020 3:13 PM
 */
@RestController
@CrossOrigin
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    JwtUtil jwtUtil;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @PostMapping(path = "/register")
    public JSONObject register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        JSONObject comResponse = new JSONObject();
        if (map == null || map.isEmpty()) {
            //comResponse.setCode(200);
            //comResponse.setMsg("注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
        } else {
            //comResponse.setCode(401);
            //comResponse.setMsg(map.get("usernameMsg").toString()+map.get("passwordMsg").toString()+map.get("emailMsg").toString());
        }
        return comResponse;
    }

    @PostMapping(path = "/activation/{userId}/{code}")
    public JSONObject activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        JSONObject comResponse = new JSONObject();
        if (result == ACTIVATION_SUCCESS) {
            //comResponse.setCode(200);
            //comResponse.setMsg("激活成功,您的账号已经可以正常使用了!");
        } else if (result == ACTIVATION_REPEAT) {
            //comResponse.setCode(200);
            //comResponse.setMsg("无效操作,该账号已经激活过了!");
        } else {
            //comResponse.setCode(401);
            //comResponse.setMsg("激活失败,您提供的激活码不正确!");
        }
        return comResponse;
    }

    @GetMapping(path = "/getKaptcha")
    public JSONObject getKaptcha() {
        JSONObject comResponse = new JSONObject();
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存入redis，过期时间60s
        //验证码的所属
        String kaptchaOwner = CommunityUtil.generateUUID();
        //将验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            CommunityUtil.getJSONString(500,"服务器出错",null);
            return comResponse;
        }

        Map<String,Object> map = new HashMap<>();
        map.put("img",Base64.getEncoder().encodeToString(os.toByteArray()));
        map.put("kaptchaKey",kaptchaKey);
        comResponse.put("data",map);

        return  CommunityUtil.getJSONString(200,"验证码生成成功!",map);
    }

    @PostMapping(path = "/login")
    public JSONObject userLogin(@RequestParam("username") String username, @RequestParam("password") String password,
                            @RequestParam("kaptchaText") String kaptchaText, @RequestParam("kaptchaKey") String kaptchaKey) {
        JSONObject response;
        String getKaptchaKey = (String) redisTemplate.opsForValue().get(kaptchaKey);
        if(getKaptchaKey==null|| !getKaptchaKey.equals(kaptchaText)){
            response = CommunityUtil.getJSONString(401,"验证码错误!");
        }else {
            // 先把验证码删除了
            redisTemplate.delete(kaptchaKey);
            User user = userService.findUserByName(username);
            // 检查用户名是否存在
            if (user != null) {
                // 密码验证通过
                if (Objects.equals(user.getPassword(), password)) {
                    // 登录成功，返回成功响应
                    //登录成功后生成token并发送
                    response = CommunityUtil.getJSONString(200,"登录成功!");
                    response.put("loginToken",jwtUtil.getToken(username, "user"));
                } else {
                    // 密码错误，返回失败响应
                    response = CommunityUtil.getJSONString(401,"密码错误!");
                }
            } else {
                // 用户名不存在，返回失败响应
                response = CommunityUtil.getJSONString(401,"用户名不存在!");
            }
        }
        return response;
    }

}
