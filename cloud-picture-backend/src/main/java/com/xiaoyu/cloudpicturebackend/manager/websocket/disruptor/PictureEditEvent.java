package com.xiaoyu.cloudpicturebackend.manager.websocket.disruptor;

import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;
    
    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}