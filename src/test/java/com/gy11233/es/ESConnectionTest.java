package com.gy11233.es;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.cluster.ClusterOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ESConnectionTest {

    @Resource
    private ElasticsearchRestTemplate elasticTemplate;

    @Test
    public void connectionTest() {
        ClusterOperations cluster = elasticTemplate.cluster();
    }
}
