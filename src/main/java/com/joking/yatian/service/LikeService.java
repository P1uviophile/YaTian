package com.joking.yatian.service;

import com.joking.yatian.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author Joking7
 * @ClassName LikeService
 * @description: 点赞 服务类
 * @date 2024/7/31 上午1:22
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @MethodName: like
     * @Description: 对实体点赞, 对被点赞人加赞
     * @param userId 点赞人
     * @param entityType
     * @param entityId
     * @param entityUserId 被赞的人(作者)
     * @return: void
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:27
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                //多个更新操作，需要事务
                operations.multi();
                if (isMember) {
                    //取消赞
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    //点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });

    }

    /**
     * @MethodName: findEntityLikeCount
     * @Description: 查看某实体的点赞数量
     * @param entityType
     * @param entityId
     * @return: long
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:28
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * @MethodName: findEntityLikeStatus
     * @Description: 查询某人对某实体的点赞状态
     * @param entityType
     * @param entityId
     * @param userId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:29
     */
    public int findEntityLikeStatus(int entityType,int entityId,int userId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //此处返回int，是为了进行扩展。比如扩展踩，为止2.等等情况
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }

    /**
     * @MethodName: findUserLikeCount
     * @Description: 查询某个用户获得赞，用于在个人主页查看收获了多少赞
     * @param userId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午1:29
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();// count.intValue()数据的整数形式;
    }
}

