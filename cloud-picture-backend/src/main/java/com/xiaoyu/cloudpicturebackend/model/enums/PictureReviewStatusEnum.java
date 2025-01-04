package com.xiaoyu.cloudpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * ClassName: UserRoleEnum
 * Description:
 *  图片审核状态枚举
 *
 * @Author: fy
 * @create: 2024-12-11 23:14
 * @version: 1.0
 */
@Getter
public enum PictureReviewStatusEnum {


    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);


    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureReviewStatusEnum getEnumByValue(int value) {

        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;

    }

}
