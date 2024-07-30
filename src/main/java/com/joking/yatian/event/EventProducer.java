package com.joking.yatian.event;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Joking7
 * @ClassName EventProducer
 * @description: 消息队列事件生产者
 * @date 2024/7/28 上午1:08
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event){
        // 将事件发送到指定主题，其中把内容转换为json对象，消费者受到json后，可以将json转换成Event
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
