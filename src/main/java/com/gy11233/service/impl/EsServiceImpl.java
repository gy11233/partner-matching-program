package com.gy11233.service.impl;

import com.gy11233.common.ErrorCode;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.ESUser;
import com.gy11233.model.domain.User;
import com.gy11233.model.vo.ESUserLocationSearchVO;
import com.gy11233.model.vo.UserNearbyVO;
import com.gy11233.service.EsService;
import com.gy11233.utils.DistanceUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EsServiceImpl implements EsService {

    @Resource
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    /**
     * 搜索附近的人
     */
    @Override
    public List<UserNearbyVO> queryNearBy(ESUserLocationSearchVO locationSearch) {

        Integer distance = locationSearch.getDistance();
        Double lat = locationSearch.getLat();
        Double lon = locationSearch.getLon();
        if (distance == null || lat == null || lon == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "距离搜索参数有误");
        }
        // 先构建查询条件
        BoolQueryBuilder defaultQueryBuilder = QueryBuilders.boolQuery();
        // 距离搜索条件
        defaultQueryBuilder.filter(QueryBuilders.geoDistanceQuery("location")
                .distance(distance, DistanceUnit.METERS)
                .point(lat, lon)
        );


        // 分页条件
        PageRequest pageRequest = PageRequest.of(0, 10);

        // 地理位置
        GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("location", lat, lon)
                .geoDistance(GeoDistance.ARC)  // 距离计算方式， 具体详情知识补充部分。
                .order(SortOrder.ASC);
//        // 按照距离排序
//        GeoDistanceSortBuilder distanceSortBuilder = SortBuilders.geoDistanceSort("location", String.valueOf(new GeoPoint(lat, lon)))
//                .unit(DistanceUnit.KILOMETERS)
//                .geoDistance(GeoDistance.ARC)  // 距离计算方式， 具体详情知识补充部分。
//                .order(SortOrder.ASC);

        //组装条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(defaultQueryBuilder)
                .withPageable(pageRequest)
                .withSort(sortBuilder)
                .build();

        SearchHits<ESUser> searchHits = elasticsearchRestTemplate.search(searchQuery, ESUser.class);
        List<UserNearbyVO> userList = new ArrayList<>();
        for (SearchHit<ESUser> searchHit : searchHits) {
            ESUser content = searchHit.getContent();
            UserNearbyVO userNearbyVO = new UserNearbyVO();
            userNearbyVO.setId(content.getId());
            userNearbyVO.setLat(content.getLocation().getLat());
            userNearbyVO.setLon(content.getLocation().getLon());
            userNearbyVO.setDistance((Double) searchHit.getSortValues().get(0));
//            System.out.println(DistanceUtil.calculateDistance(lat, lon, userNearbyVO.getLat(), userNearbyVO.getLon()));
            userList.add(userNearbyVO);
        }
        return userList;
    }

    @Override
    public Boolean saveUser(User user) {

        ESUser esUser = new ESUser();
        esUser.setId(user.getId());
        esUser.setTags(user.getTags());
        esUser.setName(user.getUsername());
        esUser.setDesc(user.getProfile());
        esUser.setLocation(new GeoPoint(user.getDimension(), user.getLongitude()));
        try {
            elasticsearchRestTemplate.save(esUser);
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "es数据插入异常");
        }
        return true;
    }
}
