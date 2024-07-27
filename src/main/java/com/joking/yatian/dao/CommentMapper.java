package com.joking.yatian.dao;

import com.joking.yatian.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Joking7
 * @ClassName CommentMapper
 * @description: TODO
 * @date 2024/7/25 上午9:42
 */
@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
