package com.xiaoyu.cloudpicturebackend.api.imageSearch;

import com.xiaoyu.cloudpicturebackend.api.imageSearch.model.ImageSearchResult;
import com.xiaoyu.cloudpicturebackend.api.imageSearch.sub.GetImageFirstUrlApi;
import com.xiaoyu.cloudpicturebackend.api.imageSearch.sub.GetImageListApi;
import com.xiaoyu.cloudpicturebackend.api.imageSearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * ClassName: ImageSearchApiFacade
 * Description:
 *
 * @Author: fy
 * @create: 2025-01-12 19:45
 * @version: 1.0
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl){
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        //String url = "https://mms1.baidu.com/it/u=919924554,703731821&fm=253&app=138&f=JPEG";
        String url = "https://cloud-picture-1317444877.cos.ap-chengdu.myqcloud.com/space/1877961633318854658/2025-01-12_v3kr76yb.webp";
        List<ImageSearchResult> imageSearchResults = searchImage(url);
        System.out.println(imageSearchResults);
    }


}
