package com.xiaoyu.cloudpicturebackend.manager.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.WorkHandler;
import com.xiaoyu.cloudpicturebackend.manager.websocket.PictureEditHandler;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * ClassName: PictureEditEventWorkHandler
 * Description:
 * 图片编辑事件处理器（消费者）
 *
 * @Author: fy
 * @create: 2025-01-23 19:04
 * @version: 1.0
 */
@Component
@Slf4j
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;


    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        log.info("处理图片编辑事件");

        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        WebSocketSession session = pictureEditEvent.getSession();
        User user = pictureEditEvent.getUser();
        Long pictureId = pictureEditEvent.getPictureId();
        //获取到消息类别
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);

        //根据消息类型，处理消息
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                //其他消息类型，返回错误提示
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型错误");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                //处理精度问题
                session.sendMessage(pictureEditHandler.handlePrecisionIssue(pictureEditResponseMessage));
                break;
        }

    }
}
