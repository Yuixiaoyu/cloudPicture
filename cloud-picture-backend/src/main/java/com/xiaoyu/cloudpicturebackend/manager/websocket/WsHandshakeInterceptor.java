package com.xiaoyu.cloudpicturebackend.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaoyu.cloudpicturebackend.manager.auth.SpaceUserAuthManager;
import com.xiaoyu.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.xiaoyu.cloudpicturebackend.model.entity.Picture;
import com.xiaoyu.cloudpicturebackend.model.entity.Space;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.enums.SpaceTypeEnum;
import com.xiaoyu.cloudpicturebackend.service.PictureService;
import com.xiaoyu.cloudpicturebackend.service.SpaceService;
import com.xiaoyu.cloudpicturebackend.service.SpaceUserService;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * ClassName: WsHandshakeInterceptor
 * Description:
 *      websocket 拦截器,建立连接之前校验
 * @Author: fy
 * @create: 2025-01-23 16:47
 * @version: 1.0
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {


    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    /**
     * 建立连接前校验
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes websocket session 属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取当前登陆用户
        if(request instanceof ServletServerHttpRequest){
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)){
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            //获取登陆用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)){
                log.error("用户未登陆，拒绝握手");
                return false;
            }
            //校验用户是否有当前图片的编辑权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)){
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space= null;
            if (spaceId!=null){
                //如果是团队空间，并且 有编辑者权限，才能建立连接
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)){
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() !=SpaceTypeEnum.TEAM.getValue()){
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                log.error("用户没有当前图片的编辑权限，拒绝握手");
                return false;
            }
            //设置用户登陆信息等属性到websocket会话中
            attributes.put("user",loginUser);
            attributes.put("userId",loginUser.getId());
            attributes.put("pictureId",Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
