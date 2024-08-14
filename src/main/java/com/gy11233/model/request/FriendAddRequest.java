package com.gy11233.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendAddRequest implements Serializable {
    private static final long serialVersionUID = -836364474157971203L;
    private Long friendId;
}
