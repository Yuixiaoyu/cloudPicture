package com.xiaoyu.cloudpicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.xiaoyu.cloudpicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.xiaoyu.cloudpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.vo.UserVO;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: PictureEditHandler
 * Description:
 * 图片编辑 websocket 处理器
 *
 * @Author: fy
 * @create: 2025-01-23 17:19
 * @version: 1.0
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;


    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //构造响应，发送加入编辑的消息通知
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        //广播给所有用户,包括自己
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 收到前端发送的消息，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        //获取消息内容，将消息json转换为PictureEditRequestMessage对象
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        //从session中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        //根据消息类型，处理消息（生产消息到disruptor队列）
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);

    }

    /**
     * 进入编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        //没有用户正在编辑图片，才能进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            //设置用户正在编辑该图片
            pictureEditingUsers.put(pictureId, user.getId());
            //构造响应，发送加入编辑消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户 %s 开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            //广播给所有用户,包括自己
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }

    }

    /**
     * 处理编辑操作
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        //正在编辑的用户
        Long editingUserId = pictureEditingUsers.get(pictureId);
        //获取编辑操作
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            log.error("无效的编辑操作: {}", editAction);
            return;
        }
        //确认是当前的编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            //构造响应，发送具体的操作通知给其他用户
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("用户 %s 执行 %s", user.getUserName(),actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            //广播给除了当前用户以外的其他用户，否则会造成重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage,session);
        }
    }

    /**
     * 退出编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        //正在编辑的用户
        Long editingUserId = pictureEditingUsers.get(pictureId);
        //确认是当前的编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            //移除用户正在编辑该图片
            pictureEditingUsers.remove(pictureId);

            //构造响应，发送退出编辑通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户 %s 退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            //广播给所有用户，包括自己
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 关闭连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        //从session中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        //移除当前用户的编辑状态
        handleExitEditMessage(null, session,user, pictureId);
        //删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        //通知其他用户，该用户已经离开
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        //广播给所有用户，包括自己
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 广播消息给该图片的所有连接的用户，（支持排除掉某个用户）==>自己
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession
     * @throws Exception
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            //处理精度问题
            TextMessage textMessage = handlePrecisionIssue(pictureEditResponseMessage);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 处理用户id精度问题
     *
     * @param pictureEditResponseMessage
     * @return
     * @throws JsonProcessingException
     */
    public TextMessage handlePrecisionIssue(PictureEditResponseMessage pictureEditResponseMessage) throws JsonProcessingException {
        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
        objectMapper.registerModule(module);
        // 序列化为 JSON 字符串
        String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
        return new TextMessage(message);
    }

    /**
     * 广播消息给该图片的所有连接的用户
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @throws Exception
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}
