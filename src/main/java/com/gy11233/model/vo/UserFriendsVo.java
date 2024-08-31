package com.gy11233.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserFriendsVo implements Serializable {

    private static final long serialVersionUID = -7235246619835901985L;
    /**
     * id
     */
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 标签 json
     */
    private String tags;

    /**
     * 用户距离
     */
    private Double distance;

    /**
     * 是否是好友
     */
    private Integer isFriends; // 0不是好友 1 是好友 2 未登录
}
