package com.joking.yatian.service;

import com.joking.yatian.dao.CommentMapper;
import com.joking.yatian.entity.Comment;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Joking7
 * @ClassName CommentService
 * @description: 评论Service
 * @date 2024/7/25 上午9:45
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * @MethodName: findCommentsByEntity
     * @Description: 根据实体(目前只有贴子和评论)查询评论
     * @param entityType 实体类型
     * @param entityId 实体id
     * @param offset 分页用
     * @param limit 分页用
     * @return: List<Comment>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/27 下午5:38
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * @MethodName: findCommentCount
     * @Description: 查询实体的评论数
     * @param entityType
     * @param entityId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/27 下午5:39
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * @MethodName: addComment
     * @Description: 添加评论,使用Transactional注解保证抛出异常之后事务回滚
     * @param comment
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/27 下午5:39
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    /**
     * @MethodName: findCommentById
     * @Description: 通过评论id查询评论
     * @param id
     * @return: Comment
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/27 下午5:46
     */
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}