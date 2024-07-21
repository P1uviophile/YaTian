package com.joking.yatian.util;

import com.joking.yatian.entity.User;
import org.springframework.stereotype.Component;

/**
 * HostHolder  持有用户信息,用于代替session对象.
 * @author Joking7
 * @version 2024/07/18 
**/
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
