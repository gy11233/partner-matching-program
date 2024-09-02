package com.gy11233.impl;

import com.gy11233.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AlgorithmUtilsTest {

    @Test
    void test() {
        List<String> list1 = Arrays.asList("java", "大一", "男");
        List<String> list2 = Arrays.asList("java", "大二", "男");
        List<String> list3 = Arrays.asList("python", "大一", "女");
        int i = AlgorithmUtils.minDistance(list1, list2);
        int j = AlgorithmUtils.minDistance(list1, list3);
        System.out.println("i = " + i);
        System.out.println("j = " + j);
    }

}
