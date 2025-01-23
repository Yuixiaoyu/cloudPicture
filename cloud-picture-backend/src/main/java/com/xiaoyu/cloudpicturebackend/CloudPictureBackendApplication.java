package com.xiaoyu.cloudpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = ShardingSphereAutoConfiguration.class)
@MapperScan("com.xiaoyu.cloudpicturebackend.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true) //代理
public class CloudPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudPictureBackendApplication.class, args);
    }

}
