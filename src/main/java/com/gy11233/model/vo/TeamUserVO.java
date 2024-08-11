package com.gy11233.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍和用户信息封装类
 */
@Data
public class TeamUserVO implements Serializable {
    private static final long serialVersionUID = -5121272425339577342L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 状态 0 - 公开 1-私有 2-加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO createUser;

    /**
     * 加入用户数量
     */
    private long hasJoinNum;
    /**
     * 用户是否加入队伍
     */
    private boolean hasJoin;

}
