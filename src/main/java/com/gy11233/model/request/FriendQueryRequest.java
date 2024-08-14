package com.gy11233.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendQueryRequest implements Serializable {
    private static final long serialVersionUID = 3879408752188827943L;
    private String searchParam;
}
