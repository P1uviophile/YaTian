package com.joking.yatian.service;

import com.joking.yatian.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * UserService 用户服务接口 
 * @author Joking7 
 * @version 2024/07/18 
**/
@Service
public interface UserService {

    /**
     * @date 2024/7/18
     * @methodName findUserById
     * 根据用户id查询用户信息
     * @param id 用户id
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
     **/
    public User findUserById(int id);

    /**
     * @date 2024/7/18
     * @methodName getAuthorities
     * @param userId 用户id
     * @return java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>
     * @author Joing7
     * @throws
     *
     **/
    public Collection<? extends GrantedAuthority> getAuthorities(int userId);

    /**
     * @date 2024/7/18
     * @methodName register
     * 用户注册业务
     * @param user
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author Joing7
     * @throws
     *
    **/
    public Map<String, Object> register(User user);

    /**
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
    public int activation(int userId, String code);

    /**
     * @date 2024/7/19
     * @methodName findUserByName
     * 根据用户名查询用户
     * @param username 用户名
     * @return com.joking.yatian.entity.User
     * @author Joing7
     * @throws
     *
     **/
    public User findUserByName(String username);

    /**
     * @date 2024/7/21
     * @methodName checkPassword
     * @param user 用户
     * @param password 输入的密码
     * @return boolean
     * @author Joing7
     * @throws
     *
     **/
    public boolean checkPassword(User user,String password);

}
