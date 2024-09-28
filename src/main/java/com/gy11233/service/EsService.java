package com.gy11233.service;

import com.gy11233.model.domain.ESUser;
import com.gy11233.model.vo.ESUserLocationSearchVO;

import java.util.List;

public interface EsService {
    List<ESUser> queryNearBy(ESUserLocationSearchVO locationSearch);
}
