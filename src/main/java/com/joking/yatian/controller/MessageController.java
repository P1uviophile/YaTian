package com.joking.yatian.controller;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Message;
import com.joking.yatian.entity.Page;
import com.joking.yatian.entity.User;
import com.joking.yatian.service.MessageService;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import com.joking.yatian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author Joking7
 * @ClassName MessageController
 * @description: 消息 Controller
 * @date 2024/8/3 下午10:31
 */
@RestController
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    //@Autowired
    //private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @MethodName: getLetterList
     * @Description: 私信列表
     * @param pageCurrent
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:18
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public JSONObject getLetterList(@RequestParam("pageCurrent")int pageCurrent,
                                @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        // 分页信息
        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        //model.addAttribute("conversations", conversations);
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        //model.addAttribute("letterUnreadCount", letterUnreadCount);
        response.put("letterUnreadCount", letterUnreadCount);
        response.put("page", page);
        return response;
    }

    /**
     * @MethodName: getLetterDetail
     * @Description: 私信详情
     * @param conversationId
     * @param pageCurrent
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午10:30
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public JSONObject getLetterDetail(@PathVariable("conversationId") String conversationId,
                                      @RequestParam("pageCurrent") int pageCurrent,
                                      @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        // 分页信息
        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        //model.addAttribute("letters", letters);

        // 私信目标
        //model.addAttribute("target", getLetterTarget(conversationId));
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("letters", letters);
        response.put("page", page);
        response.put("target", getLetterTarget(user,conversationId));
        // 设置已读
        List<Integer> ids = getLetterIds(user,letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return response;
    }

    /**
     * @MethodName: getLetterTarget
     * @Description: 获取私信发送目标用户
     * @param user
     * @param conversationId
     * @return: User
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午10:31
     */
    private User getLetterTarget(User user,String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (user.getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    /**
     * @MethodName: getLetterIds
     * @Description: 获取未读的私信列表
     * @param user
     * @param letterList
     * @return: List<Integer>
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/4 下午10:32
     */
    private List<Integer> getLetterIds(User user,List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (user.getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    /**
     * @MethodName: sendLetter
     * @Description: 发送私信
     * @param toName
     * @param content
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:18
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    public JSONObject sendLetter(@RequestParam("toName") String toName,
                                 @RequestParam("content") String content,
                                 @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(404, "目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(200,"发送成功!");
    }

    /**
     * @MethodName: getNoticeList
     * @Description: 通知列表,指系统发送的通知
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:17
     */
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public JSONObject getNoticeList(@RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);

        JSONObject response = CommunityUtil.getJSONString(200);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            // 将转义字符还原回去
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 还原为map
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            //model.addAttribute("commentNotice", messageVO);
            response.put("commentNotice", messageVO);
        }


        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            //model.addAttribute("likeNotice", messageVO);
            response.put("likeNotice", messageVO);
        }


        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            //model.addAttribute("followNotice", messageVO);
            response.put("followNotice", messageVO);
        }


        // 查询未读私信总数量,无会话id限制
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        //model.addAttribute("letterUnreadCount", letterUnreadCount);
        response.put("letterUnreadCount", letterUnreadCount);
        // 查询未读通知总数量,无topic限制
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        //model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        response.put("noticeUnreadCount", noticeUnreadCount);

        return response;
    }

    /**
     * @MethodName: getNoticeDetail
     * @Description: 通知 详情
     * @param topic
     * @param pageCurrent
     * @param token
     * @return: JSONObject
     * @throws:
     * @author: Joking7
     * @Date: 2024/8/5 下午8:18
     */
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public JSONObject getNoticeDetail(@PathVariable("topic") String topic,
                                      @RequestParam("pageCurrent") int pageCurrent,
                                      @RequestHeader("userToken") String token) {
        User user = userService.findUserById(Integer.parseInt(jwtUtil.parseToken(token).get("userId")));
        Page page = new Page();
        page.setCurrent(pageCurrent);
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        //model.addAttribute("notices", noticeVoList);
        JSONObject response = CommunityUtil.getJSONString(200);
        response.put("notices", noticeVoList);
        response.put("page", page);
        // 设置已读
        List<Integer> ids = getLetterIds(user,noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return response;
    }
}