package com.xiaoyu.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaoyu.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.user.UserRegisterRequest;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.cloudpicturebackend.model.vo.LoginUserVo;
import com.xiaoyu.cloudpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author xiaoyu
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-12-11 23:06:52
 */
public interface UserService extends IService<User> {

    /**
     * 注册用户
     *
     * @param userRegisterRequest 用户注册接口dto
     * @return 返回用户id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取脱敏后的登录用户信息
     *
     * @param user 用户信息
     * @return 返回脱敏后的用户信息
     */
    LoginUserVo getLoginUserVO(User user);


    /**
     * 获取脱敏后的用户信息
     *
     * @param userList 用户列表
     * @return 返回脱敏后的用户信息
     */
    List<UserVO> getUserVOList(List<User> userList);


    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 返回脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return 脱敏后的用户列表
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);



    /**
     * 获取加密后的密码
     *
     * @param userPassword 待加密 密码
     * @return 返回加密后的密码
     */
    String getEncryptPassword(String userPassword);


    /**
     * 判断当前用户是否是管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);


}
