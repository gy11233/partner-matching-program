package com.gy11233;

import com.gy11233.model.domain.User;
import com.gy11233.service.UserService;
import com.gy11233.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * 启动类测试
 *
 */
@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserService userService;

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String newPassword = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(newPassword);
    }


    @Test
    void contextLoads() {

    }

    @Test
    void testSearchUserByTags(){
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> users = userService.searchUserByTags(tagNameList);
        System.out.println(users);
    }

}

