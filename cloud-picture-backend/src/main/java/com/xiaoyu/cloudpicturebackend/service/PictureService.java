package com.xiaoyu.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.cloudpicturebackend.model.dto.picture.PictureQueryRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.picture.PictureReviewRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.xiaoyu.cloudpicturebackend.model.dto.picture.PictureUploadRequest;
import com.xiaoyu.cloudpicturebackend.model.entity.Picture;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.vo.LoginUserVo;
import com.xiaoyu.cloudpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 张飞宇
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-15 19:20:13
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     * @param inputSource 输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 获取查询对象
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条）
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);


    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest ,User loginUser);


    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
    void filePictureParams(Picture picture, User loginUser);


    /**
     * 批量抓取和创建图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


    /**
     * 清理图片文件
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

}
