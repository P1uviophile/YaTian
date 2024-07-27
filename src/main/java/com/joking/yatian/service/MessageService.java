package com.joking.yatian.service;

import com.joking.yatian.dao.MessageMapper;
import com.joking.yatian.entity.Message;
import com.joking.yatian.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Joking7
 * @ClassName MessageService
 * @description: MessageService
 * @date 2024/7/28 上午1:46
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * @MethodName: findConversations
     * @Description: 查询会话列表
     * @param userId
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:17
     */
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * @MethodName: findConversationCount
     * @Description: 查询用户会话数量
     * @param userId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:17
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * @MethodName: findLetters
     * @Description: 查询会话的消息
     * @param conversationId
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:18
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * @MethodName: findLetterCount
     * @Description: 查询会话的消息数量
     * @param conversationId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:16
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * @MethodName: findLetterUnreadCount
     * @Description: 查询会话的未读消息数量
     * @param userId
     * @param conversationId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:15
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * @MethodName: addMessage
     * @Description: 添加消息
     * @param message
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:15
     */
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * @MethodName: readMessage
     * @Description: 已读消息(更新消息状态)
     * @param ids
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:14
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * @MethodName: findLatestNotice
     * @Description: 查询某topic的最近消息
     * @param userId
     * @param topic
     * @return: Message
     * @throws: 
     * @author: Joking7
     * @Date: 2024/7/28 上午2:18
     */
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * @MethodName: findNoticeCount
     * @Description: 查询topic的提醒数量
     * @param userId
     * @param topic
     * @return: int
     * @throws: 
     * @author: Joking7
     * @Date: 2024/7/28 上午2:19
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * @MethodName: findNoticeUnreadCount
     * @Description: 查询topic的未读的提醒数量
     * @param userId
     * @param topic
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:35
     */
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * @MethodName: findNotices
     * @Description:
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午2:35
     */
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
