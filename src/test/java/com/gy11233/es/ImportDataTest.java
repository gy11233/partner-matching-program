package com.gy11233.es;

import com.gy11233.model.domain.ESUser;
import com.gy11233.model.vo.ESUserLocationSearchVO;
import com.gy11233.service.EsService;
import com.gy11233.utils.RandomCodeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ImportDataTest {
    @Resource
    private ElasticsearchRestTemplate elasticTemplate;

    @Resource
    private EsService esService;
    /**
     * 创建索引
     */
    @Test
    public void testCreateIndex() {
        boolean exists = elasticTemplate.indexOps(ESUser.class).exists();
        // 如果索引已存在,删除索引
        if (exists) {
            elasticTemplate.indexOps(ESUser.class).delete();
            return;
        }
        // 创建索引
        elasticTemplate.indexOps(ESUser.class).create();

        // 创建映射
        Document mapping = elasticTemplate.indexOps(ESUser.class).createMapping();
        elasticTemplate.indexOps(ESUser.class).putMapping(mapping);

        System.out.println("---执行成功---");
    }

    @Test
    public void importEsUserTest() {
        int num = 10;
        int low = 11;

        List<ESUser> list = new ArrayList<>();
        for (int i = low; i < num + low; i++) {

            // 随机生成用户
            ESUser esUser = new ESUser();
            esUser.setId((long) i);
            int age = new Random().nextInt(20) + 5;
            boolean flag = age % 2 > 0;
            esUser.setName(flag ? RandomCodeUtil.getRandomChinese("0") : RandomCodeUtil.getRandomChinese("1"));
            esUser.setTags(flag ? "['java,'男','python']" : "['c++,'女','大一']");
            esUser.setDesc(flag ? "大闹天宫,南天门守卫, 擅长编程, 烹饪" : "天空守卫,擅长编程,睡觉");
            String latRandNumber = RandomCodeUtil.getRandNumberCode(4);
            String lonRandNumber = RandomCodeUtil.getRandNumberCode(4);
            double lat = Double.parseDouble("30.30" + latRandNumber);
            double lon = Double.parseDouble("120.24" + lonRandNumber);
            GeoPoint geoPoint = new GeoPoint(lat, lon);
            esUser.setLocation(geoPoint);
            list.add(esUser);
        }
        elasticTemplate.save(list);
    }

    @Test
    public void searchNearby() {
        ESUserLocationSearchVO esUserLocationSearchVO = new ESUserLocationSearchVO();
        esUserLocationSearchVO.setLat(30.30);
        esUserLocationSearchVO.setLon(120.04);
        esUserLocationSearchVO.setDistance(100000);
        List<ESUser> esUsers = esService.queryNearBy(esUserLocationSearchVO);

        for (ESUser esUser: esUsers) {
            System.out.println("esUser = " + esUser.toString());
        }
    }
}
