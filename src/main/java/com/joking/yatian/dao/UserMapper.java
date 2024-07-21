package com.joking.yatian.dao;

import com.joking.yatian.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper UserMapper
 * @author Joking7 
 * @version 2024/07/17 
**/
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}