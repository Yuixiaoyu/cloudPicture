package com.xiaoyu.cloudpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: PictureUploadRequest
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-15 19:26
 * @version: 1.0
 */
@Data
public class PictureUploadRequest implements Serializable {


    private static final long serialVersionUID = -6673255481811165898L;


    /**
     * 图片id（用于修改）
     */
    private Long id;


    /**
     * 文件地址
     */
    private  String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片类别
     */
    private String category;


}
