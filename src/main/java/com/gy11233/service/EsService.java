package com.gy11233.service;

import com.gy11233.model.domain.ESUser;
import com.gy11233.model.domain.User;
import com.gy11233.model.vo.ESUserLocationSearchVO;
import com.gy11233.model.vo.UserNearbyVO;

import java.util.List;

public interface EsService {
    List<UserNearbyVO> queryNearBy(ESUserLocationSearchVO locationSearch);

    Boolean saveUser(User user);
}
