package com.gy11233.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy11233.model.domain.Friend;
import com.gy11233.model.request.FriendQueryRequest;
import com.gy11233.model.vo.UserVO;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface FriendService extends IService<Friend> {

    boolean addFriend(long userId, long friendId);

    List<UserVO> listFriends(Long userId, HttpServletRequest request);
    List<UserVO> searchFriends(FriendQueryRequest friendQueryRequest, long userId);
}
