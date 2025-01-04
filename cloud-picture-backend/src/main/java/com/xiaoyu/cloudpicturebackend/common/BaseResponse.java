package com.xiaoyu.cloudpicturebackend.common;

import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: BaseResponse
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-07 20:48
 * @version: 1.0
 */

@Data
public class BaseResponse<T> {

    private int code;

    private T data;

    private String message;


    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code,data,"");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
