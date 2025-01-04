package com.xiaoyu.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName: UrlPictureUploadImpl
 * Description:
 *  url图片上传
 * @Author: fy
 * @create: 2024-12-28 14:36
 * @version: 1.0
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        //下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        URL url = null;
        try {
            url =new URL(fileUrl);
            fileUrl = url.getFile().substring(url.getFile().lastIndexOf('/'));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //无法获取完整文件名
        //return FileUtil.mainName(fileUrl);
        return fileUrl;
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        //校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        //校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址不正确");
        }
        //校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP协议和HTTPS协议的文件地址");

        //发送 HEAD 请求，验证文件是否存在 获取元信息
        HttpResponse httpResponse = null;
        try {
            httpResponse= HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //为正常返回，无需执行其他判断（有些图片地址不支持HEAD请求但是不能一口否认这个图片不存在）
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            //文件存在，校验文件类型
            String contentType = httpResponse.header("Content-Type");
            //不为空才校验合法
            if (StrUtil.isNotBlank(contentType)){
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp","image/gif");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR,"文件类型错误");
            }
            //文件存在，校验文件大小
            try {
                String contentLength = httpResponse.header("Content-Length");
                if (StrUtil.isNotBlank(contentLength)){
                    long length = Long.parseLong(contentLength);
                    final long ONE_M = 1024 * 1024;
                    ThrowUtils.throwIf(length>3*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过3MB");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式异常");
            }

        } finally {
            //手动释放资源
            if (httpResponse != null){
                httpResponse.close();
            }
        }



    }
}
