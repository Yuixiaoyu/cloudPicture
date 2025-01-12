package com.xiaoyu.cloudpicturebackend.api.imageSearch.model;

import lombok.Data;

/**
 * ClassName: ImageSearchResult
 * Description:
 *  意图搜图结果
 * @Author: fy
 * @create: 2025-01-12 18:55
 * @version: 1.0
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;

}
