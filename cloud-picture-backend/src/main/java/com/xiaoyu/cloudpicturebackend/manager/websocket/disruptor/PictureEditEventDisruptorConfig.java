package com.xiaoyu.cloudpicturebackend.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * ClassName: PictureEditEventDisruptorConfig
 * Description:
 * 图片编辑事件 disruptor 配置类
 *
 * @Author: fy
 * @create: 2025-01-23 19:13
 * @version: 1.0
 */
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("PictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        //定义ringBuffer的大小
        int bufferSize = 1024 * 256;
        //创建Disruptor
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create()
                        .setNamePrefix("PictureEditEventDisruptor")
                        .build()
        );
        //设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        //启动disruptor
        disruptor.start();
        return disruptor;
    }
}
