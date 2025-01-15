package com.xiaoyu.cloudpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.xiaoyu.cloudpicturebackend.config.CosClientConfig;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.manager.CosManager;
import com.xiaoyu.cloudpicturebackend.model.dto.file.UploadPictureResult;
import com.xiaoyu.cloudpicturebackend.model.entity.Urls;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 图片上传模版
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片接口
     *
     * @param obj url 或者  multipartFile
     * @return 封装的 VO
     */
    public UploadPictureResult uploadPicture(Object obj, String uploadPathPrefix) {
        validPicture(obj);
        String templatePath = System.getProperty("user.dir") + File.separator + getOriginalFilename(obj);
        File tempFile = new File(templatePath);
        try {
            processFile(obj, tempFile);
        } catch (Exception e) {
            log.error("文件校验失败",e);
        }
        // 上传文件
        return uploadPicture(tempFile, uploadPathPrefix);
    }

    public UploadPictureResult uploadPicture(File file, String uploadPathPrefix) {
        String imagePath = generateImageUploadPath(file, uploadPathPrefix);
        try {
            return analyzeCosReturn(new AnalyzeCosParams(
                    cosManager.putPicObject(imagePath, file),
                    FileUtil.mainName(file),
                    imagePath
            ));
        } catch (Exception e) {
            log.error("Error uploading picture: {}", ExceptionUtil.getRootCauseMessage(e), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            try {
                FileUtil.del(file);
            } catch (IORuntimeException e) {
                log.error("Error deleting temp file: {}", file.getAbsolutePath(), e);
            }
        }
    }
    private String generateImageUploadPath(File file, String uploadPathPrefix) {
        String originalFilename = FileUtil.getName(file);
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), RandomUtil.randomString(16),
                originalFilename);
        // 最后结果 public/1867564572229492994/2024-12-21_REArPZjceu7DkRp3.Konachan.jpg
        return String.format("%s/%s", uploadPathPrefix, uploadFilename);
    }
    /**
     * 获取文件后缀，默认转成小写进行判断
     *
     * @param fileName 文件名
     * @return 文件后缀
     */
    protected String extractFileSuffix(String fileName) {
        return Optional.of(fileName)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf('.') + 1))
                .map(String::toLowerCase)
                .orElse("");
    }

    private UploadPictureResult analyzeCosReturn(AnalyzeCosParams params) {
        PutObjectResult putObjectResult = params.getPutObjectResult();
        CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
        ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();
        List<CIObject> objectList = ciUploadResult.getProcessResults().getObjectList();
        if (CollUtil.isNotEmpty(objectList)) {
            return getUploadPictureResult(params, objectList,imageInfo);
        }
        return buildResult(params, imageInfo);
    }

    private UploadPictureResult getUploadPictureResult(AnalyzeCosParams params, List<CIObject> objectList,ImageInfo imageInfo) {
        CIObject compressedCiObject = objectList.get(0);
        String baseUrl = cosManager.getBaseUrl();
        // 设置 url
        Urls urls = new Urls();
        // 公共字段
        urls.setOriginalUrl(String.format("%s/%s", baseUrl, params.getImagePath()));
        urls.setUrl(String.format("%s/%s", baseUrl, compressedCiObject.getKey()));
        if (objectList.size() > 1) {
            // 压缩图 webp 格式
            urls.setThumbnailUrl(String.format("%s/%s", baseUrl, objectList.get(1).getKey()));
        } else {
            // 没有压缩图片的时候，缩略图默认等于压缩图
            urls.setThumbnailUrl(String.format("%s/%s", baseUrl, compressedCiObject.getKey()));
        }
        if (objectList.size() > 2) {
            // 涉及到转化成 png 逻辑，转换后的 url
            urls.setTransferUrl(String.format("%s/%s", baseUrl, objectList.get(2).getKey()));
        }
        UploadPictureResult uploadPictureResult = buildResult(params.getImageName(), compressedCiObject);
        uploadPictureResult.setUrls(urls);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     * @param originalFilename 原始文件名
     * @param compressCiObject 压缩后的对象
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename,CIObject compressCiObject) {
        //计算宽高
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //设置压缩后的原图地址
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());

        //返回可访问的路径
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     * @param params
     * @param imageInfo 对象存储返回结果
     * @return
     */
    private UploadPictureResult buildResult(AnalyzeCosParams params, ImageInfo imageInfo) {
        Urls urls = new Urls();
        urls.setUrl(String.format("%s/%s",cosClientConfig.getHost(),params.getImagePath()));
        //计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrls(urls);
        uploadPictureResult.setPicName(params.getImageName());
        uploadPictureResult.setPicSize((long)imageInfo.getQuality());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        //返回可访问的路径
        return uploadPictureResult;
    }

    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource
     */
    protected abstract void processFile(Object inputSource,File file)throws Exception;

    /**
     * 获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验输入源（本地或url）
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 不用成员变量因为多线程时会出问题
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AnalyzeCosParams {
        private PutObjectResult putObjectResult;
        private String imageName;
        private String imagePath;
    }
}
