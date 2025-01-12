package com.xiaoyu.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
        //有些服务器会拒绝java程序访问，导致Server response error with status code: [403]
        //因此不能直接使用，要设置请求头信息来伪装成浏览器
        HttpUtil.downloadFile(fileUrl, file);
    }
    @Deprecated
    protected void oldProcessFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        // 创建请求对象
        HttpRequest request = HttpUtil.createGet(fileUrl);
        // 设置User-Agent，伪装成浏览器
        // 设置User-Agent，伪装成最新版本的Chrome浏览器
        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        // 设置其他请求头
        request.header("Referer", "http://www.example.com");
        request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        request.header("Accept-Encoding", "gzip, deflate, br");

        //// 获取Cookies（如果需要）
        //String cookies = "cf_clearance=WCRQXSP8H.iQ_rMWKBgqXZh6UgqGfGR0Vy8xqbcag04-1736663574-1.2.1.1-yBKOGVPEdFc1USl.KWmjw4POj_KzHGwnhL1xhk28scNzMhosTjYxqNCVDOC.NoUN_cwDmnQ9FiumuSj2Qy9CQ3yHKaU9R.tW25D6SxSgyloRObXVXxcgauZQyjzeBOf3iK06IY4o8kH3EoNLuFzHcUwOxmCX5XxRDJz7WUEiI1jM_dSy9A166ptT54siLXF_LiKl9UOvwdn3V7ASlrtzpPiyWhNj5YrtSCNJhGb1LfJ0vpPug0KcguW5xNAHuohLmlx4pc3ky7puHAaMSeW56wOVlbq0lvQeZJUClSO5J7og7pYQYBrxcwuIF2f1olRGCHYCAXhiKsyeoSUgNhPiEA";
        //request.header("Cookie", cookies);

        // 发送请求，获取响应
        HttpResponse response = request.execute();
        // 检查响应状态码
        if (response.isOk()) {
            // 获取响应的输入流
            InputStream inputStream = response.bodyStream();
            // 创建文件输出流
            FileOutputStream outputStream = new FileOutputStream(file);

            // 读取输入流内容并写入输出流
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            // 关闭流
            inputStream.close();
            outputStream.close();
        } else {
            // 处理错误
            throw new Exception("Server response error with status code: [" + response.getStatus() + "]");
        }
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
                    ThrowUtils.throwIf(length>6*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过6MB");
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
