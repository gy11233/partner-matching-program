package com.gy11233.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -2710041694231030256L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍密码
     */
    private String password;


}
