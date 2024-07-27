package com.joking.yatian.dao;

import com.joking.yatian.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Joking7
 * @ClassName MessageMapper
 * @description: TODO
 * @date 2024/7/28 上午1:12
 */
@Mapper
public interface MessageMapper {

    /**
     * @MethodName: selectConversations
     * @Description: 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
     * @param userId
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:28
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * @MethodName: selectConversationCount
     * @Description:  查询当前用户的会话数量.
     * @param userId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:30
     */
    int selectConversationCount(int userId);

    /**
     * @MethodName: selectLetters
     * @Description: 查询某个会话所包含的私信列表.
     * @param conversationId
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:30
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * @MethodName: selectLetterCount
     * @Description: 查询某个会话所包含的私信数量.
     * @param conversationId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:38
     */
    int selectLetterCount(String conversationId);

    /**
     * @MethodName: selectLetterUnreadCount
     * @Description: 查询未读私信的数量
     * @param userId
     * @param conversationId
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:38
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * @MethodName: insertMessage
     * @Description: 新增消息
     * @param message
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:38
     */
    int insertMessage(Message message);

    /**
     * @MethodName: updateStatus
     * @Description: 修改消息的状态
     * @param ids
     * @param status
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:39
     */
    int updateStatus(List<Integer> ids, int status);

    /**
     * @MethodName: selectLatestNotice
     * @Description: 查询某个主题下最新的通知
     * @param userId
     * @param topic
     * @return: Message
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:39
     */
    Message selectLatestNotice(int userId,String topic);

    /**
     * @MethodName: selectNoticeCount
     * @Description: 查询某个主题下包含的通知数量
     * @param userId
     * @param topic
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:39
     */
    int selectNoticeCount(int userId,String topic);

    /**
     * @MethodName: selectNoticeUnreadCount
     * @Description: 查询未读通知的消息数量
     * @param userId
     * @param topic
     * @return: int
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:39
     */
    int selectNoticeUnreadCount(int userId,String topic);

    /**
     * @MethodName: selectNotices
     * @Description: 查询某个主题包含的通知列表
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return: List<Message>
     * @throws:
     * @author: Joking7
     * @Date: 2024/7/28 上午1:39
     */
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
