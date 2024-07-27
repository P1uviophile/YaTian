package com.joking.yatian.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Joking7
 * @ClassName Message
 * @description: 私信实体类
 * @date 2024/7/25 上午10:49
 */
@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}

