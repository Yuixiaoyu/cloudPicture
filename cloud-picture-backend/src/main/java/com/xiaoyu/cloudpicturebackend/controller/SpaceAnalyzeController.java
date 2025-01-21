package com.xiaoyu.cloudpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.cloudpicturebackend.annotation.AuthCheck;
import com.xiaoyu.cloudpicturebackend.common.BaseResponse;
import com.xiaoyu.cloudpicturebackend.common.DeleteRequest;
import com.xiaoyu.cloudpicturebackend.common.ResultUtils;
import com.xiaoyu.cloudpicturebackend.constant.UserConstant;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.exception.ThrowUtils;
import com.xiaoyu.cloudpicturebackend.model.dto.space.*;
import com.xiaoyu.cloudpicturebackend.model.dto.space.analyze.*;
import com.xiaoyu.cloudpicturebackend.model.entity.Space;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.enums.SpaceLevelEnum;
import com.xiaoyu.cloudpicturebackend.model.vo.SpaceVO;
import com.xiaoyu.cloudpicturebackend.model.vo.space.analyze.*;
import com.xiaoyu.cloudpicturebackend.service.SpaceAnalyzeService;
import com.xiaoyu.cloudpicturebackend.service.SpaceService;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: SpaceAnalyzeController
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-15 16:48
 * @version: 1.0
 */
@RestController
@RequestMapping("/space/analyze")
@Slf4j
public class SpaceAnalyzeController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 获取空间的使用状态
     *
     * @param spaceUsageAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        if (spaceUsageAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyze);
    }


    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        if (spaceCategoryAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyze);
    }


    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        if (spaceTagAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyze = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyze);
    }

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        if (spaceSizeAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyze);
    }

    /**
     * 获取空间图片用户行为分析
     *
     * @param spaceUserAnalyzeRequest 空间用户行为分析请求
     * @param request                 请求
     * @return  List<SpaceUserAnalyzeResponse>
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        if (spaceUserAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyze = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyze);
    }

    /**
     * 获取空间使用排行分析
     * @param spaceRankAnalyzeRequest 空间使用排行分析请求
     * @param request 请求
     * @return List<Space>
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        if (spaceRankAnalyzeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceRankAnalyze = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceRankAnalyze);
    }
}
