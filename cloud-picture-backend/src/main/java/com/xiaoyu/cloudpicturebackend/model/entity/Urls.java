package com.xiaoyu.cloudpicturebackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Urls {
    // 必须字段
    private String originalUrl;

    // 必须字段
    private String url;

    // 必须字段
    private String thumbnailUrl;

    // 可选字段
    private String transferUrl;
}

