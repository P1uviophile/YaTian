package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.User;
import com.joking.yatian.service.FollowService;
import com.joking.yatian.service.LikeService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.HostHolder;
import com.joking.yatian.util.JwtUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author Joking7
 * @ClassName UserController
 * @description: 用户Controller
 * @date 2024/7/31 下午2:03
 */
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.name}")
    private String headerName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @MethodName: getSettingPage
     * @Description: 获取设置表单
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 下午9:33
     */
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public JSONObject getSettingPage(@RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        JSONObject res = CommunityUtil.getJSONString(200);
        res.put("username", user.getUsername());
        res.put("userHeaderUrl",user.getHeaderUrl());
        return res;
    }

    /**
     * @MethodName: updateUser
     * @Description: 更新用户名
     * @param token
     * @param newName
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 下午9:33
     */
    @PostMapping(path = "/username/update")
    public JSONObject updateUser(@RequestHeader("userToken") String token, @RequestParam("newName") String newName) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        if(userService.updateName(user.getId(),newName)!=-1) {
            return CommunityUtil.getJSONString(200,"改名成功!");
        }else return CommunityUtil.getJSONString(403,"该名字已被占用!");
    }

    /**
     * @MethodName: updatePassword
     * @Description: 更新密码
     * @param token
     * @param oldPassword
     * @param newPassword
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 下午9:38
     */
    @PostMapping(path = "password/update")
    public JSONObject updatePassword(@RequestHeader("userToken") String token, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        if(userService.updatePassword(user.getId(),newPassword,oldPassword)!=-1) {
            return CommunityUtil.getJSONString(200,"更改密码成功!");
        }else return CommunityUtil.getJSONString(403,"密码错误!");
    }

    /**
     * @MethodName: updateHeaderUrl
     * @Description: 更新头像
     * @param file 前端返回的头像文件
     * @param token JWT的token 用于解析id
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 下午2:51
     */
    @RequestMapping(path = "/header/update", method = RequestMethod.POST)
    public JSONObject updateHeaderUrl(@RequestParam("file") MultipartFile file, @RequestHeader("userToken") String token) {
        int userId = Integer.parseInt(jwtUtil.parseToken(token).get("userId"));
        User user = userService.findUserById(userId);
        if(user.getStatus()>-1){
            // 生成上传凭证
            Auth auth = Auth.create(accessKey, secretKey);
            String fileName = userId +"-"+(new Date().getTime());
            String uploadToken = auth.uploadToken(headerName, fileName);
            // 指定上传机房
            UploadManager manager = new UploadManager(new Configuration());
            try {
                // 开始上传图片
                Response response = manager.put(file.getBytes(), fileName, uploadToken);
                // 处理响应结果
                JSONObject json = JSONObject.parseObject(response.bodyString());
                userService.updateHeader(userId,headerBucketUrl+"/"+fileName);
                return CommunityUtil.getJSONString(200,"更新头像成功!");
            }catch (QiniuException e){
                return CommunityUtil.getJSONString(500,"七牛云上传头像失败");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            return CommunityUtil.getJSONString(403,"没有权限更新头像!");
        }
    }

    /**
     * @MethodName: getProfilePage
     * @Description: 获取用户主页
     * @param userId 要获取的用户id
     * @param token 请求用户的token 用于解析用户的id
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 下午8:19
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public JSONObject getProfilePage(@PathVariable("userId") int userId,@RequestHeader("userToken") String token) {
        User user = userService.findUserById(userId);
        JSONObject response = CommunityUtil.getJSONString(200,"获取用户主页成功");

        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        //model.addAttribute("user", user);
        response.put("userId", userId);
        response.put("username", user.getUsername());
        response.put("userHeaderUrl", user.getHeaderUrl());

        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        //model.addAttribute("likeCount", likeCount);
        response.put("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        //model.addAttribute("followeeCount",followeeCount);
        response.put("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        //model.addAttribute("followerCount",followerCount);
        response.put("followerCount", followerCount);

        int curId = Integer.parseInt(jwtUtil.parseToken(token).get("userId"));
        //是否已关注
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(curId, ENTITY_TYPE_USER, userId);
        }
        response.put("hasFollowed", hasFollowed);

        return response;
    }
}
