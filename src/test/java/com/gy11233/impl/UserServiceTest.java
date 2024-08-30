package com.gy11233.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.gy11233.model.domain.User;
import com.gy11233.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Resource
    UserService userService;

    /**
     * 为所有无标签用户添加默认标签
     */
    @Test
    public void emptyTagTest(){
        String[] tagsArray = {"大一", "单身"};
        Gson gson = new Gson();
        String json = gson.toJson(tagsArray);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> list = userService.list(queryWrapper);
        for (User user : list) {
            if (StringUtils.isBlank(user.getTags()) || StringUtils.isBlank(user.getTags())){
                User tempUser = new User();
                tempUser.setId(user.getId());
                tempUser.setTags(json);
                userService.updateById(tempUser);
            }
        }
    }
}
