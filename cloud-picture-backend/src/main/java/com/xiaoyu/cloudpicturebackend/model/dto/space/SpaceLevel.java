package com.xiaoyu.cloudpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: SpaceLevel
 * Description:
 * 空间级别
 *
 * @Author: fy
 * @create: 2025-01-11 13:29
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 空间对应的值
     */
    private int value;

    /**
     * 空间的中文名
     */
    private String text;

    /**
     * 最大条数
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;


}
