package com.gy11233.model.vo;

import lombok.Data;

/**
 * 功能描述: 搜索附近的人
 *
 */
@Data
public class ESUserLocationSearchVO {
 
    // 纬度 [3.86， 53.55]
    private Double lat;
 
    // 经度 [73.66, 135.05]
    private Double lon;
 
    // 搜索范围(单位米)
    private Integer distance;

}