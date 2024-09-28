package com.gy11233.service.impl;

import com.gy11233.model.domain.ESUser;
import com.gy11233.model.vo.ESUserLocationSearchVO;
import com.gy11233.service.EsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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
    public List<ESUser> queryNearBy(ESUserLocationSearchVO locationSearch) {

        Integer distance = locationSearch.getDistance();
        Double lat = locationSearch.getLat();
        Double lon = locationSearch.getLon();

        // 先构建查询条件
        BoolQueryBuilder defaultQueryBuilder = QueryBuilders.boolQuery();
        // 距离搜索条件
        if (distance != null && lat != null && lon != null) {
            defaultQueryBuilder.filter(QueryBuilders.geoDistanceQuery("location")
                    .distance(distance, DistanceUnit.METERS)
                    .point(lat, lon)
            );
        }


        // 分页条件
        PageRequest pageRequest = PageRequest.of(0, 10);

        // 地理位置排序
        GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("location", lat, lon);

        //组装条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(defaultQueryBuilder)
                .withPageable(pageRequest)
//                .withSort(sortBuilder)
                .build();

        SearchHits<ESUser> searchHits = elasticsearchRestTemplate.search(searchQuery, ESUser.class);
        List<ESUser> userList = new ArrayList<>();
        for (SearchHit<ESUser> searchHit : searchHits) {
            ESUser content = searchHit.getContent();
            userList.add(content);
        }
        return userList;
    }
}
