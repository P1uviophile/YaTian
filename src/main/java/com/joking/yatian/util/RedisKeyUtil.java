package com.joking.yatian.util;

/**
 * RedisKeyUtil Redis Key 生成工具
 * @author Joking7
 * @version 2024/07/18 
**/
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    /**
     * 图片验证码
     */
    private static final String PREFIX_KAPTCHA = "kaptcha";

    /**
     * 登录凭证(集成JWT后废用
     */
    //private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    /**
     * 网站数据统计
     */
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";


    /**
     * @date 2024/7/18
     * @methodName getEntityLikeKey
     * 某个实体收到的赞，如帖子，评论
     * like:entity:entityType:entityId -> set(userId) 对应set，存入userId
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + entityType + SPLIT + entityId;
    }

    /**
     * @date 2024/7/18
     * @methodName getUserLikeKey
     * 某个用户收到的总赞数
     * like:user:userId ->int
     * @param userId 用户id
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * @date 2024/7/18
     * @methodName getFolloweeKey
     * 某个用户关注的实体
     * followee:userId:entityType ->zset(entityId,date),用有序集合存，存的是关注的哪个实体，分数是当前时间。
     * 为了后期统计方便，统计关注了哪些，进行排序列举
     * @param userId
     * @param entityType
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * @date 2024/7/18
     * @methodName getFollowerKey
     * 某个实体拥有的粉丝，实体可能是用户，或者是帖子
     * follower:entityType:entityId ->zset(userId,date)，存入用户Id
     * @param entityType
     * @param entityId
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityType;
    }

    /**
     * @date 2024/7/18
     * @methodName getKaptchaKey
     * 登录验证码
     * owner是指随机生成的uuid
     * @param owner
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * @date 2024/7/18
     * @methodName getTicketKey
     * 登录的凭证
     * @param ticket
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    //public static String getTicketKey(String ticket) {return PREFIX_TICKET + SPLIT + ticket;}

    /**
     * @date 2024/7/18
     * @methodName getUserKey
     * 用户
     * @param userId
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * @date 2024/7/18
     * @methodName getUVKey
     * 单日uv
     * @param date
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * @date 2024/7/18
     * @methodName getUVKey
     * 区间UV
     * @param startDate
     * @param endData
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getUVKey(String startDate, String endData) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endData;
    }

    /**
     * @date 2024/7/18
     * @methodName getDAUKey
     * 单日DAU
     * @param date
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * @date 2024/7/18
     * @methodName getDAUKey
     * 区间DAU
     * @param startDate
     * @param endDate
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * @date 2024/7/18
     * @methodName getPostScoreKey
     * 帖子分数
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    public static String getPostRowsKey() {
        return PREFIX_POST + SPLIT + "rows";
    }

    public static String getPostHotKey() {
        return PREFIX_POST + SPLIT + "hot";
    }
}
