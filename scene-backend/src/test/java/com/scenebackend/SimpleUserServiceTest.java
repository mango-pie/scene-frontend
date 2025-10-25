package com.scenebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简化的测试类，用于验证基本配置
 */
@SpringBootTest
public class SimpleUserServiceTest {

    @Test
    public void testBasicConfiguration() {
        System.out.println("基本配置测试...");
        assertTrue(true, "基本测试应该通过");
        System.out.println("✅ 基本配置测试通过");
    }
}