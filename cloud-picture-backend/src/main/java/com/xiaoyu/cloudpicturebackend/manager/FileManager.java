package com.xiaoyu.cloudpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.xiaoyu.cloudpicturebackend.common.ResultUtils;
import com.xiaoyu.cloudpicturebackend.config.CosClientConfig;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.exception.ThrowUtils;
import com.xiaoyu.cloudpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件服务
 * @Deprecated 已废弃，改为使用upload包中的模版方法优化
 */
@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(8);
        String originalFilename = multipartFile.getOriginalFilename();
        //上传的文件名称为当前上传时间+uuid.文件后缀（防止文件名冲突）
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFilename));
        //uploadPathPrefix是每个用户的文件夹目录
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回
        File file = null;
        try {
            //上传文件,本地路径
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPicObject(uploadPath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            //计算宽高
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
            //封装返回结果
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回可访问的路径
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //临时文件清理
            deleteTemplateFile(file);
        }

    }

    /**
     * 清理临时文件
     *
     * @param file
     */
    public static void deleteTemplateFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error,filepath ={}", file.getAbsolutePath());
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        //校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        //fileSize > 3 * ONE_M 文件大小不能超过3MB
        ThrowUtils.throwIf(fileSize > 3 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过3MB");

        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        //允许上传的文件后缀列表
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "webp", "jpg", "png");

        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型不支持");

    }
}
