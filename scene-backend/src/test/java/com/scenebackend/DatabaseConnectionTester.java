package com.scenebackend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 独立的数据库连接测试类
 * 可以直接运行main方法测试数据库连接
 */
public class DatabaseConnectionTester {

    // 数据库连接配置 - 从application.properties中复制
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shop?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) {
        System.out.println("开始数据库连接测试...");
        System.out.println("==================================");

        try {
            // 测试1: 加载数据库驱动
            testDriverLoading();

            // 测试2: 建立数据库连接
            testConnection();

            // 测试3: 执行简单查询
            testSimpleQuery();

            // 测试4: 检查数据库表
            testDatabaseTables();

            // 测试5: 综合性能测试
            testPerformance();

            System.out.println("==================================");
            System.out.println("✅ 所有数据库连接测试通过！");

        } catch (Exception e) {
            System.out.println("==================================");
            System.out.println("❌ 数据库连接测试失败！");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试数据库驱动加载
     */
    private static void testDriverLoading() {
        System.out.println("测试1: 加载数据库驱动...");
        try {
            Class.forName(DB_DRIVER);
            System.out.println("✅ 数据库驱动加载成功: " + DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("数据库驱动加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试数据库连接
     */
    private static void testConnection() {
        System.out.println("测试2: 建立数据库连接...");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // 检查连接是否有效
            if (connection.isValid(5)) {
                System.out.println("✅ 数据库连接建立成功");

                // 获取连接信息
                String databaseName = connection.getCatalog();
                String databaseProduct = connection.getMetaData().getDatabaseProductName();
                String databaseVersion = connection.getMetaData().getDatabaseProductVersion();

                System.out.println("   数据库名称: " + databaseName);
                System.out.println("   数据库产品: " + databaseProduct);
                System.out.println("   数据库版本: " + databaseVersion);
                System.out.println("   连接URL: " + DB_URL);
            } else {
                throw new RuntimeException("数据库连接无效");
            }

        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试简单查询
     */
    private static void testSimpleQuery() {
        System.out.println("测试3: 执行简单查询...");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 as test_value, VERSION() as db_version")) {

            if (resultSet.next()) {
                int testValue = resultSet.getInt("test_value");
                String dbVersion = resultSet.getString("db_version");

                System.out.println("✅ 简单查询执行成功");
                System.out.println("   测试值: " + testValue);
                System.out.println("   数据库版本: " + dbVersion);
            } else {
                throw new RuntimeException("查询结果为空");
            }

        } catch (SQLException e) {
            throw new RuntimeException("简单查询执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查数据库表
     */
    private static void testDatabaseTables() {
        System.out.println("测试4: 检查数据库表...");

        String[] requiredTables = {"user", "tag"};

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            for (String tableName : requiredTables) {
                String sql = String.format(
                        "SELECT COUNT(*) as table_count FROM information_schema.tables " +
                                "WHERE table_schema = DATABASE() AND table_name = '%s'",
                        tableName
                );

                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    if (resultSet.next()) {
                        int tableCount = resultSet.getInt("table_count");
                        if (tableCount > 0) {
                            System.out.println("✅ 表 '" + tableName + "' 存在");
                        } else {
                            System.out.println("⚠️  表 '" + tableName + "' 不存在");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("数据库表检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 性能测试
     */
    private static void testPerformance() {
        System.out.println("测试5: 性能测试...");

        long startTime = System.currentTimeMillis();
        int testRounds = 5;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            for (int i = 0; i < testRounds; i++) {
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                    // 简单验证查询结果
                    if (!resultSet.next()) {
                        throw new RuntimeException("第 " + (i+1) + " 轮测试失败");
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double avgTime = (double) duration / testRounds;

            System.out.println("✅ 性能测试完成");
            System.out.println("   总耗时: " + duration + "ms");
            System.out.println("   平均每轮: " + String.format("%.2f", avgTime) + "ms");
            System.out.println("   测试轮数: " + testRounds);

        } catch (SQLException e) {
            throw new RuntimeException("性能测试失败: " + e.getMessage(), e);
        }
    }

    /**
     * 快速连接测试方法（可用于其他类调用）
     */
    public static boolean testQuickConnection() {
        try {
            Class.forName(DB_DRIVER);
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                return connection.isValid(3);
            }
        } catch (Exception e) {
            System.err.println("快速连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取数据库连接（可用于其他类）
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动加载失败", e);
        }
    }
}