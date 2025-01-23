package com.xiaoyu.cloudpicturebackend.manager.websocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * ClassName: PictureEditEventProducer
 * Description:
 * 图片编辑事件生产者
 *
 * @Author: fy
 * @create: 2025-01-23 19:20
 * @version: 1.0
 */
@Component
public class PictureEditEventProducer {

    @Resource
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    /**
     * 发布事件
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        //获取到可以放置事件的位置
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        //发布事件
        ringBuffer.publish(next);
    }

    /**
     * 关闭 disruptor（停机）
     */
    @PreDestroy
    public void destroy() {
        pictureEditEventDisruptor.shutdown();
    }

}