package com.xiaoyu.cloudpicturebackend.utils;

import java.awt.*;

/**
 * 工具类：计算颜色相似度
 */
public class ColorTransformUtils {

    private ColorTransformUtils() {
        // 工具类不需要实例化
    }

    public static String getStandardColor(String color) {
        // 去掉前缀 "0x"
        String hexColor = color.substring(2);
        // 检查长度
        if (hexColor.length() == 5) {
            // 如果长度为5位，在末尾补一个0
            hexColor += "0";
        }
        // 重新添加前缀 "0x"
        return "0x" + hexColor;
    }

    public static void main(String[] args) {
        String color = "0x27321";
        String standardColor = getStandardColor(color);
        System.out.println(standardColor);
    }

}
