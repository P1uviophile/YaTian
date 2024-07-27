package com.joking.yatian.entity;

import lombok.Data;

import java.util.Date;

/**
 * Comment 评论
 * @author Joking7
 * @version 2024/07/16
**/
@Data
public class Comment {
    private int id;
    private int userId;
    
    /**
     * 实体类型 : 1-贴子 2-评论 后续可拓展
     */
    private int entityType;
    
    /**
     * 实体对象id
     */
    private int entityId;
    
    /**
     * 评论对象id : 0-贴子 其他-评论评论或其他实体类
     */
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}