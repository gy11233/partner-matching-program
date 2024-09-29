package com.gy11233.model.vo;

import lombok.Data;

/**
 * 搜索附近的人返回的结果包装类
 */
@Data
public class UserNearbyVO {

    /**
     * 用户id
     */
    private Long id;

    // 纬度
    private Double lat;

    // 经度
    private Double lon;

    // 两人间的距离 m
    private Double distance;

}
