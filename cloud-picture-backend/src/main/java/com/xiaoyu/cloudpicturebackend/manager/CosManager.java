package com.xiaoyu.cloudpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.xiaoyu.cloudpicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName: CosManager
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-15 16:45
 * @version: 1.0
 */
@Component
public class CosManager {

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    private final List<String> ALLOW_FILE_TYPE = Arrays.asList("png", "jpg", "jpeg");

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 上传对象（附带图片信息）
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putPicObject(String key,File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //对图片进行处理（获取基本信息）
        PicOperations picOperations = new PicOperations();
        //1表示返回原图信息
        picOperations.setIsPicInfo(1);
        //图片处理规则列表
        List<PicOperations.Rule> rules = new ArrayList<>();
        //1.图片压缩，转成webp格式
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        //2.缩略图处理,仅对>20k的图片大小进行处理
        if (file.length()>2*1024){
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            //拼接缩略图的路径
            String thumbnailKey = FileUtil.mainName(key)+"_thumbnail."+FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
            rules.add(thumbnailRule);
        }
        // 图片格式转换 如果不是 png/jpg/jpeg 进行转化成 jpg 格式，方便后面百度图搜图接口的使用
        if (!ALLOW_FILE_TYPE.contains(FileUtil.getSuffix(key))) {
            PicOperations.Rule transferRule = new PicOperations.Rule();
            transferRule.setBucket(cosClientConfig.getBucket());
            // 转换成 png 格式
            transferRule.setRule("imageMogr2/format/png");
            // 存储的 key 的名字  xxx_transfer.png
            String transferKey = FileUtil.mainName(key) + "_transfer" + ".png";
            transferRule.setFileId(transferKey);
            rules.add(transferRule);
        }
        picOperations.setRules(rules);
        //构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取url域名
     * @return
     */
    public String getBaseUrl() {
        return cosClientConfig.getHost();
    }


    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
         cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }

    /**
     * 删除对象
     *
     * @param keys List 集合
     */
    public void deleteObjects(List<String> keys) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
        deleteObjectsRequest.setKeys(keyVersions);
        keys.forEach(key -> keyVersions.add(new DeleteObjectsRequest.KeyVersion(key)));
        deleteObjectsRequest.setKeys(keyVersions);
        cosClient.deleteObjects(deleteObjectsRequest);
    }



}
