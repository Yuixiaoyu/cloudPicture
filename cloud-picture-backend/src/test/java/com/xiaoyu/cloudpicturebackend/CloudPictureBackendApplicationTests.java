package com.xiaoyu.cloudpicturebackend;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class CloudPictureBackendApplicationTests {

    @Test
    void contextLoads() {
        String urlString = "https://pic1.zhimg.com/v2-2c8cb13d158d32708beced95e715e012_r.jpg";
        // 使用正则表达式替换掉查询参数部分
        String baseUrl = urlString.replaceAll("\\?.*$", "");
        System.out.println("URL without params: " + baseUrl);
    }

    @Test
    void testDate() {
        System.out.println(new Date().toString());
    }

}
