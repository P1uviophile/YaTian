package com.joking.yatian.quartz;

import com.joking.yatian.entity.DiscussPost;
import com.joking.yatian.util.RedisKeyUtil;
import com.joking.yatian.service.DiscussPostService;
import com.joking.yatian.service.ElasticsearchService;
import com.joking.yatian.service.LikeService;
import com.joking.yatian.util.CommunityConstant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Joking7
 * @ClassName PostScoreRefreshJob
 * @description: 热帖排行 Job
 * @date 2024/7/31 上午2:06
 */
@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 论坛纪元
     */
    private static final Date epoch;

    // 静态代码块：用staitc声明，jvm加载类时执行，仅执行一次
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化论坛纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations == null) {
            logger.info("[任务取消] 没有要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数:", operations.size());
        while (operations.size() > 0) {
            refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    /**
     * @MethodName: refresh
     * @Description: 刷新贴子分数
     * @param postId
     * @return: void
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/31 上午2:20
     */
    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.info("该帖子不存在:id=" + postId);
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数=帖子权重+距离天数
        // w可能小于1，因为log存在，所以送入log的最小值应该为0
        // getTime()单位为ms
        double score = Math.log10(Math.max(1, w)) +
                (post.getCreateTime().getTime() - epoch.getTime()) / (3600 * 60 * 24);

        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 更新elasticsearch
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}

