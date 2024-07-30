package com.joking.yatian.service;

import com.joking.yatian.entity.User;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Joking7
 * @ClassName FollowService
 * @description: 关注 服务类
 * @date 2024/7/31 上午1:16
 */

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * @MethodName: follow
     * @Description: 关注某实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return: void
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:19
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /**
     * @MethodName: unfollow
     * @Description: 对某实体取消关注
     * @param userId
     * @param entityType
     * @param entityId
     * @return: void
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:20
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }


    /**
     * @MethodName: findFolloweeCount
     * @Description: 查询用户关注了多少实体
     * @param userId
     * @param entityType
     * @return: long
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:20
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * @MethodName: findFollowerCount
     * @Description: 查询用户的粉丝是多少
     * @param entityType
     * @param entityId
     * @return: long
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:21
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * @MethodName: hasFollowed
     * @Description: 查询用户是否已关注该实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return: boolean
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:21
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        //通过查询该实体的分数，判断是否存在
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * @MethodName: findFollowees
     * @Description: 查询某用户关注的人
     * @param userId
     * @param offset 分页开始
     * @param limit
     * @return: List<Map<String,Object>>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:21
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().range(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String,Object> map=new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return  list;
    }

    /**
     * @MethodName: findFollowers
     * @Description: 查询某用户的粉丝
     * @param userId
     * @param offset
     * @param limit
     * @return: List<Map<String,Object>>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:22
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }
}
