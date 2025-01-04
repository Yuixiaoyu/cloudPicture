package com.xiaoyu.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.cloudpicturebackend.constant.UserConstant;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.mapper.UserMapper;
import com.xiaoyu.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.user.UserRegisterRequest;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.enums.UserRoleEnum;
import com.xiaoyu.cloudpicturebackend.model.vo.LoginUserVo;
import com.xiaoyu.cloudpicturebackend.model.vo.UserVO;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaoyu
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-11 23:06:52
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册dto
     * @return 返回用户id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {

        //校验参数
        if (ObjUtil.isEmpty(userRegisterRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }

        //检查数据库中是否存在该数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());

        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前账户已存在");
        }
        //对密码进行加密操作
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.UESR.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库异常");
        }
        //主键回填
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户或密码错误");
        }
        //对用户的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);

        //查询数据库找到对应数据
        User user = this.baseMapper.selectOne(queryWrapper);
        if (ObjUtil.isEmpty(user)) {
            log.info("user login failed,userAccount cannot match UserPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return getLoginUserVo(user);
    }

    /**
     * 获取脱敏用户
     *
     * @param user 用户信息
     * @return 返回脱敏后的登录用户信息
     */
    @Override
    public LoginUserVo getLoginUserVo(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVo.class);
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVo)
                .collect(Collectors.toList());
    }

    /**
     * 获取脱敏用户的信息
     * @param user 用户信息
     * @return
     */
    @Override
    public UserVO getUserVo(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断是否登录
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //追求性能直接返回，否则再次查一遍数据库，确保数据一致性
        user = baseMapper.selectById(user.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {

        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    /**
     * 对密码加密
     *
     * @param userPassword 待加密的密码
     * @return 返回加密之后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        //混淆密码，加盐
        final String SALT = "xiaoyu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null){
            return false;
        }
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




