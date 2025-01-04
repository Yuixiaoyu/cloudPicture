package com.xiaoyu.cloudpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PictureUploadRequest
 * Description:
 *  批量导入图片请求
 * @Author: fy
 * @create: 2024-12-15 19:26
 * @version: 1.0
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {


    private static final long serialVersionUID = -6673255481811165898L;


    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 图片名称前缀
     */
    private String namePrefix;


    /**
     * 抓取数量
     */
    private  Integer count=10;


    /**
     * 图片类别
     */
    private String category;


}
