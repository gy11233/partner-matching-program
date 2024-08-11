package com.gy11233.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -4248295980049241636L;
    /**
     * id
     */
    private Long id;
}
