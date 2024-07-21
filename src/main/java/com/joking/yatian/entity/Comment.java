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
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}