package com.joking.yatian.service.impl;

import com.joking.yatian.dao.UserMapper;
import com.joking.yatian.entity.User;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.MailClient;
import com.joking.yatian.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * UserServiceImpl 用户服务接口实现 
 * @author Joking7
 * @version 2024/07/18 
**/
@Service
public class UserServiceImpl implements UserService, CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * TODO
     * @date 2024/7/18
     * @methodName findUserById
     * 根据用户id查询用户信息
     * @param id 用户id
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
    **/
    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName getCache
     * 从缓存中取用户值
     * @param userId 用户id
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
    **/
    public User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName initCache
     * 从数据库中查取用户数据，再存入缓存(初始化缓存)
     * @param userId 用户id
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
    **/
    public User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        //过期时间1h，3600s
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName clearCache
     * 更新用户信息，清除缓存数据
     * @param userId 用户id
     * @return void
     * @author Joing7
     * @throws
     *
    **/
    public void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName getAuthorities
     * 根据用户id查询用户权限
     * @param userId 用户id
     * @return java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>
     * @author Joing7
     * @throws
     *
    **/
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = userMapper.selectById(userId);

        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    case -1:
                        return AUTHORITY_FROZEN;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName register
     * 用户注册业务
     * @param user
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author Joing7
     * @throws
     *
     **/
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        return map;
    }

    /**
     * TODO
     * @date 2024/7/18
     * @methodName activation
     * 激活状态判断
     * @param userId
     * @param code
     * @return int
     * @author Joing7
     * @throws
     *
    **/
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);//更改状态，变为已激活
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * TODO
     * @date 2024/7/19
     * @methodName findUserByName
     * 根据用户名查询用户
     * @param username 用户名
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
    **/
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

}