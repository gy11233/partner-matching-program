package com.gy11233.model.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Data
@Document(indexName = "es_user", createIndex = true)
public class ESUser {

    /**
     * 用户id
     */
    @Id
    private Long id;

    /**
     * 用户名称
     */
    @Field(type = FieldType.Text)
    private String name;
    /**
     * 用户标签
     */
    @Field(type = FieldType.Keyword)
    private String tags;

    /**
     * 用户简介
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String desc;

    /**
     * 位置信息
     */
    @GeoPointField
    private GeoPoint location;
    
}