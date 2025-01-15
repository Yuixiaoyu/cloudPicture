package com.xiaoyu.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.cloudpicturebackend.exception.BusinessException;
import com.xiaoyu.cloudpicturebackend.exception.ErrorCode;
import com.xiaoyu.cloudpicturebackend.exception.ThrowUtils;
import com.xiaoyu.cloudpicturebackend.manager.CosManager;
import com.xiaoyu.cloudpicturebackend.manager.FileManager;
import com.xiaoyu.cloudpicturebackend.manager.upload.FilePictureUpload;
import com.xiaoyu.cloudpicturebackend.manager.upload.PictureUploadTemplate;
import com.xiaoyu.cloudpicturebackend.manager.upload.UrlPictureUpload;
import com.xiaoyu.cloudpicturebackend.mapper.PictureMapper;
import com.xiaoyu.cloudpicturebackend.model.dto.file.UploadPictureResult;
import com.xiaoyu.cloudpicturebackend.model.dto.picture.*;
import com.xiaoyu.cloudpicturebackend.model.entity.Picture;
import com.xiaoyu.cloudpicturebackend.model.entity.Space;
import com.xiaoyu.cloudpicturebackend.model.entity.Urls;
import com.xiaoyu.cloudpicturebackend.model.entity.User;
import com.xiaoyu.cloudpicturebackend.model.enums.PictureReviewStatusEnum;
import com.xiaoyu.cloudpicturebackend.model.vo.PictureVO;
import com.xiaoyu.cloudpicturebackend.model.vo.UserVO;
import com.xiaoyu.cloudpicturebackend.service.PictureService;
import com.xiaoyu.cloudpicturebackend.service.SpaceService;
import com.xiaoyu.cloudpicturebackend.service.UserService;
import com.xiaoyu.cloudpicturebackend.utils.ColorSimilarUtils;
import com.xiaoyu.cloudpicturebackend.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiaoyu
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-15 19:20:13
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;


    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //校验是否有空间权限,仅空间管理员才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
            }
            //校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
        //判断是上传还是更新
        Long pictureId = null;
        String category = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
            category = pictureUploadRequest.getCategory();
        }
        //如果是更新判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人或管理员可更新图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            /*
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            */
            //校验空间一致性
            //没有spaceId则复用图片的SpaceId，表明这张图片是公共图片，不需要传入spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                //传了spaceid，校验是否和原图片的spaceid是否一致
                if (!spaceId.equals(oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间id不一致");
                }
            }
        }
        //上传图片，得到图片信息
        //按照用户id划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            //公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            //图库
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        //根据inputSource类型来区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造插入数据库的信息
        Picture picture = new Picture();
        picture.setUrls(uploadPictureResult.getUrls());
        picture.setSpaceId(spaceId);
        //支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setCategory(category);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicColor(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor()));
        picture.setUserId(loginUser.getId());
        //补充审核参数
        filePictureParams(picture, loginUser);

        //操作数据库
        //如果pictureId不为空，则补充id，否则新增
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "图片上传失败，数据库异常");
            //如果是公共图库则直接返回
            if (finalSpaceId == null) {
                return true;
            }
            //更新空间使用额度
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, finalSpaceId)
                    .setSql("totalSize = totalSize +" + picture.getPicSize())
                    .setSql("totalCount = totalCount + 1")
                    .update();
            ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新空间使用额度失败");
            return picture;
        });
        return PictureVO.objToVo(picture);
    }


    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startDateTime = pictureQueryRequest.getStartDateTime();
        Date endDateTime = pictureQueryRequest.getEndDateTime();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            // and(name like "%xxx%" or introduction like "%xxx%")
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        //      >=开始时间
        queryWrapper.ge(ObjUtil.isNotEmpty(startDateTime), "editTime", startDateTime);
        //      < 结束时间
        queryWrapper.lt(ObjUtil.isNotEmpty(endDateTime), "editTime", endDateTime);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                // tags like "\"java\"" and tags lick "\"java\""
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        //对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //获取到用户id
        Long id = picture.getUserId();
        //判断用户是否存在
        if (id != null && id > 0) {
            //用户存在则将用户的信息添加到picture中的userVO属性中
            User user = userService.getById(id);
            UserVO userVo = userService.getUserVo(user);
            pictureVO.setUser(userVo);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId).collect(Collectors.toSet());
        //查询用户信息，并将用户id作为key，value是具有相同id的User列表
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVo(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 校验图片
     *
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = Optional.of(picture).map(Picture::getUrls).map(Urls::getUrl).orElse("");
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureReviewRequest), ErrorCode.PARAMS_ERROR);

        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();

        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        //校验审核状态是否重复
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }


        //数据库操作
        Picture picture = BeanUtil.copyProperties(pictureReviewRequest, Picture.class);
        picture.setUserId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);


    }


    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void filePictureParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            //管理员自动审核通过
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动过审");
        } else {
            //非管理员用户，无论是编辑还是创建默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //参数校验
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String category = pictureUploadByBatchRequest.getCategory();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多抓取30张图片");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = pictureUploadByBatchRequest.getSearchText();
        }
        //抓取地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败：" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        //解析内容
        Elements div = document.getElementsByClass("dgControl");
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        //
        Elements imgHref = div.select("a.iusc");
        //遍历元素依次处理上传图片
        int uploadCount = 0;
        for (Element element : imgHref) {
            long start = System.currentTimeMillis();
            String json = element.attr("m");
            JSONObject jsonObject = JSONUtil.parseObj(json);
            String urlString = jsonObject.getStr("murl");
            // 使用正则表达式替换掉查询参数部分
            String fileUrl = urlString.replaceAll("\\?.*$", "");
            long end = System.currentTimeMillis();
            log.info("解析" + (uploadCount + 1) + "用时:" + (end - start));
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：" + fileUrl);
                continue;
            }
            //处理图片地址，防止转义或者对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            //上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setCategory(category);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                long startUpload = System.currentTimeMillis();
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                long endUpload = System.currentTimeMillis();
                log.info("图片上传成功：id={},总计用时：{}", pictureVO.getId(), (endUpload - startUpload));
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败" + e.getMessage());
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 计算url出现的次数
     * sql
     * SELECT COUNT(*)
     * FROM picture
     * WHERE JSON_CONTAINS(urls, '"xxxx"', '$.url');
     *
     * @param pictureUrl
     * @return
     */
    public Long getOneUrlCount(String pictureUrl) {
        // 构造 JSON 路径
        String jsonPath = "$." + "url";
        String queryUrl = "\"" + pictureUrl + "\"";
        return this.lambdaQuery()
                .apply("JSON_CONTAINS(urls, {0}, {1})", queryUrl, jsonPath)
                .count();
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        //判断当前图片是否还有其他引用
        String url = oldPicture.getUrls().getUrl();
        long count = this.getOneUrlCount(url);
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        Urls urls = oldPicture.getUrls();
        if (urls != null) {
            List<String> delKeys = Stream.of(
                            urls.getOriginalUrl(),
                            urls.getUrl(),
                            urls.getThumbnailUrl(),
                            urls.getTransferUrl()
                    )
                    // 过滤非空字段
                    .filter(StrUtil::isNotBlank)
                    // 转换为删除键
                    .map(this::getPictureKey)
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(delKeys)) {
                cosManager.deleteObjects(delKeys);
            }
        }
    }

    private String getPictureKey(String url) {
        //todo 暂no
        try {
            URL urlObj = new URL(url);
            return urlObj.toString();
        } catch (Exception e) {
            // 处理异常，例如 URL 格式不正确
            log.error("图片异常,", e);
            return null;
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        //用户未登陆
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        checkPictureAuth(loginUser, oldPicture);
        //开启事务
        transactionTemplate.execute(status -> {
            //操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            //释放空间使用额度
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, oldPicture.getSpaceId())
                    .setSql("totalSize = totalSize -" + oldPicture.getPicSize())
                    .setSql("totalCount = totalCount - 1")
                    .update();
            ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新空间使用额度失败");
            return oldPicture;
        });

        //异步清理文件
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        this.checkPictureAuth(loginUser, oldPicture);
        //补充审核参数
        this.filePictureParams(picture, loginUser);
        //操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (spaceId == null) {
            //公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            Space space = spaceService.getById(spaceId);
            //私有空间，仅空间管理员可操作
            if (!space.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        //查询该空间下的所有图片（必须要有主色调）
        List<Picture> pictureList = this.lambdaQuery().eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        //如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)){
            return new ArrayList<>();
        }
        //将颜色字符串转换为主色调
        Color targetColor = Color.decode(picColor);
        //计算相似度排序
        List<Picture> sortedPictureList = pictureList.stream()
                //从小到大排序，越相似排名越靠前
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    //没有主色调，设置double最大值排到最后面
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    //计算相似度
                    //越大越相似，所以要取一个负号
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                .limit(12)
                .collect(Collectors.toList());
        //返回结果
        return sortedPictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        //1.获取和校验参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();

        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList),ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceId == null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NO_AUTH_ERROR);
        //2，校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间访问权限");
        }
        //3.查询指定图片，（仅选择需要的字段）
        List<Picture> pictureList = this.lambdaQuery().select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()){
            return;
        }
        // 4，更新分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)){
                picture.setCategory(category);
            }
            if (ObjUtil.isNotEmpty(tags)){
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        //批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList,nameRule);
        //5.批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片批量编辑失败");
    }

    /**
     * nameRul 格式 ==>  图片｛序号｝
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if(StrUtil.isBlank(nameRule)||CollUtil.isEmpty(pictureList)){
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replace("{序号}",String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"名称解析错误");
        }
    }

}




