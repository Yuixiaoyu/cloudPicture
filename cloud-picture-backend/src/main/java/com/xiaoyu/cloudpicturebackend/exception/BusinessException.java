package com.xiaoyu.cloudpicturebackend.exception;

import lombok.Getter;

import java.util.stream.Collectors;

/**
 * ClassName: BusinessException
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-07 20:38
 * @version: 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

