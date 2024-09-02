package com.gy11233.impl;

import com.gy11233.model.domain.User;
import com.gy11233.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入数据
     */
    @Test
    public void doInsertUsers(){

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> list = new ArrayList<>();
//        final int INSERT_NUM = 10000000;
        final int INSERT_NUM = 100000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("fakeUser");
            user.setUserAccount("fakeUser");
            user.setAvatarUrl("https://i0.hdslb.com/bfs/new_dyn/30575eb3dfd8969467e77c1dfa64cdc7495346562.jpg@1052w_!web-dynamic.avif");
            user.setGender(1);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("123123");
            user.setTags("");
            list.add(user);
        }
        userService.saveBatch(list, 50000);
        stopWatch.start();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    // cpu密集型：分配给核心线程数=cpu-1
    // io密集型：分配给核心的线程数可以大于cpu
    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 批量并发插入数据
     */
    @Test
    public void doConcurrencyInsertUsers(){

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        final int INSERT_NUM = 10000000;
        final int INSERT_NUM = 100000;
        int batch = 10;
        int batch_size = INSERT_NUM / 10;
        // 分成10组
        int j = 0;
        for (int i = 0; i < batch; i++) {
            List<User> list = new ArrayList<>();
            while (true) {
                j ++;
                User user = new User();
                user.setUsername("fakeUser");
                user.setUserAccount("fakeUser");
                user.setAvatarUrl("https://i0.hdslb.com/bfs/new_dyn/30575eb3dfd8969467e77c1dfa64cdc7495346562.jpg@1052w_!web-dynamic.avif");
                user.setGender(1);
                user.setUserPassword("12345678");
                user.setPhone("123");
                user.setEmail("123");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("123123");
                user.setTags("");
                list.add(user);

                if (j % batch_size == 0) {
                    break;
                }
            }
            // 异步执行任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(list, batch_size);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[] {})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}

