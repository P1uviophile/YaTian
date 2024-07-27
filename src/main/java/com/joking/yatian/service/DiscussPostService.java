package com.joking.yatian.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.joking.yatian.dao.DiscussPostMapper;
import com.joking.yatian.entity.DiscussPost;
import com.joking.yatian.util.RedisKeyUtil;
import com.joking.yatian.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Joking7
 * @ClassName DiscussPostServiceImpl
 * @description: TODO
 * @date 2024/7/24 上午2:28
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @Value("${redis.discuss-post.expire-seconds}")
    private int redisExpireSeconds;

    @Value("${redis.discuss-post.fixedRate}")
    private int redisFixedRate;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存：redis->mysql
                        String redisKey = RedisKeyUtil.getPostHotKey();
                        List<DiscussPost> res = (List<DiscussPost>) redisTemplate.opsForValue().get(redisKey);
                        if (res == null) {
                            logger.debug("load post list from DB.");
                            //System.out.println("load post hots from DB.");
                            res = discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                            redisTemplate.opsForValue().set(redisKey, res, 1800, TimeUnit.SECONDS); // 缓存过期时间为1800秒
                        }//else System.out.println("load post list from Redis.");
                        return res;
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {

                        // 二级缓存：redis->mysql
                        String redisKey = RedisKeyUtil.getPostRowsKey();
                        Integer res = (Integer) redisTemplate.opsForValue().get(redisKey);
                        if (res == null) {
                            logger.debug("load post rows from DB.");
                            //System.out.println("load post rows from DB.");
                            res = discussPostMapper.selectDiscussPostRows(0);
                            redisTemplate.opsForValue().set(redisKey, res, 1800, TimeUnit.SECONDS); // 缓存过期时间设置为1800秒
                        }//else System.out.println("load post rows from Redis.");
                        return res;
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 只有热门帖子(访问首页时，userId=0)
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId) {
        // 首页查询走缓存
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

}
