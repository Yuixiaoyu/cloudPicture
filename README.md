# cloudPicture
基于Springboot+Vue+COS+WebSocket的云图床

项目分为公共图库，私人空间以及团队空间，支持用户创建私人空间和团队空间，用户可以将图片上传到公共图库、私人空间或团队空间，项目采用sa-Token的kit模式实现多账号体系的***RBAC权限管控***，同时还使用到**ShardingSphere** 自定义分表算法实现团队空间的**动态分表**，运用webSocket+事件驱动设计实现多人协同编辑图片功能，为了防止编辑操作丢失采用**Disruptor 无锁队列** 实现了 WebSocket 消息的异步化处理。

![image-20250104153527510](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/6778e4eae9726.png)


### 技术选型：

<hr>

#### 后端

* Springboot
* MySQL数据库+Mybatis-Plus框架
* Redis缓存+Caffeine本地缓存
* jsoup抓取数据
* COS+数据万象服务
* 阿里云百炼AI绘画大模型
* Sa-Token RBAC 权限控制
* ShardingSphere 动态分库分表
* WebSocket 实时协作
* Disruptor 无锁队列提升并发性能

#### 前端

* Vue3框架
* Vite构建工具
* Ant Design Vue组件库
* Pinia全局状态管理
* ESLint+TypeScript

---

### 项目功能模块

* #### 项目首页

![项目首页](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image1.png)

* #### 团队空间

![团队空间](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image2.png)

* #### 图片上传

![图片上传](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image3.png)

* #### 图片编辑

![图片编辑](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image4.png)

* #### AI扩图

![AI扩图](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image5.gif)

* #### 图库分析

![图库分析](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image6.png)

![图库分析](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image7.png)

* #### 以图搜图

![图库分析](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image8.png)

* #### 图片分享

![图库分析](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/image9.png)

* #### 实时协同编辑

<video src="https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/video.mp4" controls="controls" width="500" height="300"></video>

### 项目架构

![项目架构](https://github.com/Yuixiaoyu/cloudPicture/blob/main/img/framework.png)
