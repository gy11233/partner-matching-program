package com.gy11233.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 3531151644058955547L;
    /**
     * id
     */
    private Long teamId;
}
