package com.xiaoyu.cloudpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserRegisterRequest
 * Description:
 *          用户注册请求
 * @Author: fy
 * @create: 2024-12-11 23:21
 * @version: 1.0
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 7126708207432167622L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;


}
