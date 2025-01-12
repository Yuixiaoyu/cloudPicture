package com.xiaoyu.cloudpicturebackend.api.imageSearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: GetImagePageUrlApi
 * Description:
 *      获取以图搜图页面地址（step 1）
 * @Author: fy
 * @create: 2025-01-12 18:58
 * @version: 1.0
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取以图搜图页面地址
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl){
        /**
         * tn: pc
         * from: pc
         * image_source: PC_UPLOAD_URL
         */
        //1.准备请求参数
        Map<String,Object> formData = new HashMap<>();
        formData.put("image",imageUrl);
        formData.put("tn","pc");
        formData.put("from","pc");
        formData.put("image_source","PC_UPLOAD_URL");
        //获取当前时间戳
        long uptime = System.currentTimeMillis();
        //构造请求地址
        String url = "https://graph.baidu.com/upload?uptime="+uptime;

        try {
            //2，发送请求
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (response.getStatus() != HttpStatus.HTTP_OK){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
            //解析响应
            String body = response.body();
            Map<String,Object> result = JSONUtil.toBean(body, Map.class);
            //3处理响应结果

            if (result == null || !Integer.valueOf(0).equals(result.get("status"))){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if (StrUtil.isBlank(searchResultUrl)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口未返回有效的结果地址");
            }
            return searchResultUrl;
        } catch (Exception e) {
           log.error("调用以图搜图失败",e);
           throw new BusinessException(ErrorCode.OPERATION_ERROR,"搜索失败");
        }
    }

    public static void main(String[] args) {
        //测试意图搜索功能
        String imageUrl ="https://mms1.baidu.com/it/u=919924554,703731821&fm=253&app=138&f=JPEG";
        String imagePageUrl = getImagePageUrl(imageUrl);
        System.out.println("以图搜图的结果："+imagePageUrl);
    }

}
