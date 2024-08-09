package com.gy11233.model.request;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = 8865348633799617372L;

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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 状态 0 - 公开 1-私有 2-加密
     */
    private Integer status;

    /**
     * 队伍密码
     */
    private String password;

}
