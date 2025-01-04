package com.xiaoyu.cloudpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PictureTagCategory
 * Description:
 *      图片标签分类列表试图
 * @Author: fy
 * @create: 2024-12-19 16:11
 * @version: 1.0
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;


    /**
     * 分类列表
     */
    private List<String> categoryList;



}
