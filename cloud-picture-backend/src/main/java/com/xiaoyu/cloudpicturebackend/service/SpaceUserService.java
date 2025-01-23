package com.xiaoyu.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.entity.Space;
import com.xiaoyu.cloudpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.vo.SpaceUserVO;
import com.xiaoyu.cloudpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张飞宇
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-01-21 15:03:29
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 创建空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    /**
     * 校验空间成员
     * @param spaceUser
     * @param add 是否新增
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询对象
     * @param spaceuserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceuserQueryRequest);

    /**
     * 获取空间成员包装类（单条）
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 分页获取空间成员封装(列表)
     * @param spaUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaUserList);


}
