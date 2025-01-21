package com.xiaoyu.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.entity.Space;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author xiaoyu
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-01-09 16:02:26
*/
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 校验空间
     * @param space
     * @param add 是否新增
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间包装类（单条）
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间封装(分页)
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 根据spaceLevel自动填充space信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser,Space space);


}
