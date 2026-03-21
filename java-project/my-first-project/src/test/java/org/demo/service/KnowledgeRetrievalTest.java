package org.demo.service;

import org.demo.dao.KnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识检索效果测试类
 * 验证RAG检索的准确率和召回率
 */
@SpringBootTest
public class KnowledgeRetrievalTest {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private AIService aiService;

    private List<Map<String, Object>> testQueries;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        prepareTestData();

        // 准备测试问题集
        testQueries = prepareTestQueries();
    }

    /**
     * 准备测试数据
     */
    private void prepareTestData() {
        // 清空现有测试数据（谨慎使用）
        // knowledgeRepository.deleteAll();

        // 导入测试知识库
        importTestKnowledge();
    }

    /**
     * 导入测试知识库
     */
    private void importTestKnowledge() {
        List<Map<String, Object>> testDocs = new ArrayList<>();

        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("jobPosition", "Java后端");
        doc1.put("title", "Spring Boot自动配置原理");
        doc1.put("content", "Spring Boot的自动配置是通过@EnableAutoConfiguration注解实现的。它会根据classpath下的依赖自动配置Spring应用。核心原理包括：1. @Conditional条件注解 2. spring.factories文件 3. 自动配置类");
        doc1.put("tags", "Spring Boot,自动配置");
        doc1.put("source", "测试数据");
        doc1.put("importance", 5);
        testDocs.add(doc1);

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("jobPosition", "Java后端");
        doc2.put("title", "JVM垃圾回收机制");
        doc2.put("content", "JVM垃圾回收主要分为Minor GC和Full GC。垃圾回收算法包括：标记-清除、复制算法、标记-整理。常见的垃圾回收器：Serial、Parallel、CMS、G1。");
        doc2.put("tags", "JVM,GC,垃圾回收");
        doc2.put("source", "测试数据");
        doc2.put("importance", 5);
        testDocs.add(doc2);

        Map<String, Object> doc3 = new HashMap<>();
        doc3.put("jobPosition", "Java后端");
        doc3.put("title", "Java并发编程");
        doc3.put("content", "Java并发编程核心包括：1. 线程池ThreadPoolExecutor 2. synchronized和Lock 3. volatile关键字 4. ConcurrentHashMap 5. AQS框架");
        doc3.put("tags", "并发,多线程");
        doc3.put("source", "测试数据");
        doc3.put("importance", 4);
        testDocs.add(doc3);

        Map<String, Object> doc4 = new HashMap<>();
        doc4.put("jobPosition", "Java后端");
        doc4.put("title", "MySQL索引原理");
        doc4.put("content", "MySQL索引主要使用B+树数据结构。索引类型：主键索引、唯一索引、普通索引、全文索引。索引优化原则：最左前缀原则、覆盖索引、索引下推。");
        doc4.put("tags", "MySQL,数据库,索引");
        doc4.put("source", "测试数据");
        doc4.put("importance", 4);
        testDocs.add(doc4);

        Map<String, Object> doc5 = new HashMap<>();
        doc5.put("jobPosition", "Java后端");
        doc5.put("title", "Redis数据结构");
        doc5.put("content", "Redis支持的数据结构：String、Hash、List、Set、ZSet（有序集合）、HyperLogLog、Geo、Bitmap。");
        doc5.put("tags", "Redis,缓存");
        doc5.put("source", "测试数据");
        doc5.put("importance", 3);
        testDocs.add(doc5);

        Map<String, Object> doc6 = new HashMap<>();
        doc6.put("jobPosition", "Java后端");
        doc6.put("title", "Spring Cloud微服务");
        doc6.put("content", "Spring Cloud微服务组件：服务注册发现：Eureka、Nacos 配置中心：Spring Cloud Config 网关：Gateway、Zuul 熔断：Hystrix、Sentinel");
        doc6.put("tags", "微服务,Spring Cloud");
        doc6.put("source", "测试数据");
        doc6.put("importance", 4);
        testDocs.add(doc6);

        Map<String, Object> doc7 = new HashMap<>();
        doc7.put("jobPosition", "Java后端");
        doc7.put("title", "设计模式");
        doc7.put("content", "常见设计模式：创建型：单例、工厂、建造者 结构型：适配器、代理、装饰器 行为型：策略、观察者、模板方法");
        doc7.put("tags", "设计模式");
        doc7.put("source", "测试数据");
        doc7.put("importance", 3);
        testDocs.add(doc7);

        for (Map<String, Object> doc : testDocs) {
            knowledgeService.saveKnowledge(
                    (String) doc.get("jobPosition"),
                    (String) doc.get("title"),
                    (String) doc.get("content"),
                    (String) doc.get("tags"),
                    (String) doc.get("source"),
                    "技术文档",
                    (Integer) doc.get("importance")
            );
        }

        System.out.println("✅ 测试知识库导入完成，共导入 " + testDocs.size() + " 条数据");
    }

    /**
     * 准备测试问题集
     */
    private List<Map<String, Object>> prepareTestQueries() {
        List<Map<String, Object>> queries = new ArrayList<>();

        Map<String, Object> q1 = new HashMap<>();
        q1.put("query", "Spring Boot是怎么实现自动配置的？");
        q1.put("expectedTopic", "Spring Boot");
        q1.put("minRelevantCount", 1);
        q1.put("description", "测试Spring Boot相关检索");
        queries.add(q1);

        Map<String, Object> q2 = new HashMap<>();
        q2.put("query", "JVM垃圾回收有哪几种算法？");
        q2.put("expectedTopic", "JVM");
        q2.put("minRelevantCount", 1);
        q2.put("description", "测试JVM相关检索");
        queries.add(q2);

        Map<String, Object> q3 = new HashMap<>();
        q3.put("query", "Java线程池怎么用？");
        q3.put("expectedTopic", "并发");
        q3.put("minRelevantCount", 1);
        q3.put("description", "测试并发编程检索");
        queries.add(q3);

        Map<String, Object> q4 = new HashMap<>();
        q4.put("query", "MySQL索引优化原则有哪些？");
        q4.put("expectedTopic", "MySQL");
        q4.put("minRelevantCount", 1);
        q4.put("description", "测试数据库检索");
        queries.add(q4);

        Map<String, Object> q5 = new HashMap<>();
        q5.put("query", "Redis支持哪些数据类型？");
        q5.put("expectedTopic", "Redis");
        q5.put("minRelevantCount", 1);
        q5.put("description", "测试Redis检索");
        queries.add(q5);

        return queries;
    }

    // ==================== 测试方法 ====================

    /**
     * 测试1：基础检索功能测试
     */
    @Test
    void testBasicRetrieval() {
        System.out.println("\n========== 测试1：基础检索功能测试 ==========");

        String query = "Spring Boot自动配置";
        Map<String, Object> result = knowledgeService.retrieveKnowledge(query);

        printRetrievalResult(query, result);

        assert result != null;
        assert result.containsKey("results");
        assert !((List<?>) result.get("results")).isEmpty();
    }

    /**
     * 测试2：相似度排序测试
     */
    @Test
    void testSimilarityRanking() {
        System.out.println("\n========== 测试2：相似度排序测试 ==========");

        String query = "Java并发编程和线程池";
        Map<String, Object> result = knowledgeService.retrieveKnowledge(query, "Java后端", 5, 0.2);

        printRetrievalResult(query, result);

        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");

        // 验证是否按相似度降序排列
        if (results.size() >= 2) {
            double firstSim = (double) results.get(0).get("similarity");
            double secondSim = (double) results.get(1).get("similarity");
            assert firstSim >= secondSim : "相似度应该降序排列";
            System.out.println("✅ 相似度排序验证通过");
        }
    }

    /**
     * 测试3：岗位筛选测试
     */
    @Test
    void testJobPositionFilter() {
        System.out.println("\n========== 测试3：岗位筛选测试 ==========");

        String query = "设计模式";

        // 不筛选岗位
        Map<String, Object> resultAll = knowledgeService.retrieveKnowledge(query);

        // 筛选Java后端岗位
        Map<String, Object> resultJava = knowledgeService.retrieveKnowledge(query, "Java后端", 5, 0.2);

        System.out.println("无筛选结果数: " + ((List<?>) resultAll.get("results")).size());
        System.out.println("Java后端筛选结果数: " + ((List<?>) resultJava.get("results")).size());

        // 验证筛选后的结果都包含指定岗位
        List<Map<String, Object>> javaResults = (List<Map<String, Object>>) resultJava.get("results");
        for (Map<String, Object> item : javaResults) {
            assert "Java后端".equals(item.get("jobPosition")) : "结果应该只包含Java后端岗位";
        }
        System.out.println("✅ 岗位筛选验证通过");
    }

    /**
     * 测试4：topK参数测试
     */
    @Test
    void testTopKParameter() {
        System.out.println("\n========== 测试4：topK参数测试 ==========");

        String query = "Java";
        int[] topKValues = {1, 3, 5};

        for (int topK : topKValues) {
            Map<String, Object> result = knowledgeService.retrieveKnowledge(query, null, topK, 0.2);
            List<?> results = (List<?>) result.get("results");
            System.out.println("topK=" + topK + "，实际返回: " + results.size() + " 条");
            assert results.size() <= topK : "返回结果不应超过topK";
        }

        System.out.println("✅ topK参数验证通过");
    }

    /**
     * 测试5：相似度阈值测试
     */
    @Test
    void testMinScoreThreshold() {
        System.out.println("\n========== 测试5：相似度阈值测试 ==========");

        String query = "Spring Boot";
        double[] thresholds = {0.1, 0.3, 0.5};

        for (double threshold : thresholds) {
            Map<String, Object> result = knowledgeService.retrieveKnowledge(query, null, 5, threshold);
            List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");

            System.out.println("阈值=" + threshold + "，返回 " + results.size() + " 条");

            // 验证所有结果都高于阈值
            for (Map<String, Object> item : results) {
                double similarity = (double) item.get("similarity");
                assert similarity >= threshold : "相似度 " + similarity + " 应大于等于阈值 " + threshold;
            }
        }

        System.out.println("✅ 相似度阈值验证通过");
    }

    /**
     * 测试6：批量测试准确率
     */
    @Test
    void testRetrievalAccuracy() {
        System.out.println("\n========== 测试6：批量测试准确率 ==========");

        int totalTests = 0;
        int passedTests = 0;
        List<String> failures = new ArrayList<>();

        for (Map<String, Object> testCase : testQueries) {
            totalTests++;
            String query = (String) testCase.get("query");
            String expectedTopic = (String) testCase.get("expectedTopic");
            int minRelevant = (int) testCase.get("minRelevantCount");
            String desc = (String) testCase.get("description");

            System.out.println("\n测试: " + desc);
            System.out.println("问题: " + query);

            Map<String, Object> result = knowledgeService.retrieveKnowledge(query);
            List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");

            boolean hasRelevant = false;
            for (Map<String, Object> item : results) {
                String title = (String) item.get("title");
                String tags = (String) item.get("tags");

                // 检查是否包含预期主题
                if (title.contains(expectedTopic) ||
                        (tags != null && tags.contains(expectedTopic))) {
                    hasRelevant = true;
                    break;
                }
            }

            if (hasRelevant && results.size() >= minRelevant) {
                passedTests++;
                System.out.println("✅ 通过 - 找到相关结果 " + results.size() + " 条");
                printTopResult(results);
            } else {
                failures.add(desc + " - 未找到相关结果");
                System.out.println("❌ 失败 - 未找到相关结果");
            }
        }

        double accuracy = (double) passedTests / totalTests * 100;
        System.out.println("\n========== 测试结果汇总 ==========");
        System.out.println("总测试数: " + totalTests);
        System.out.println("通过数: " + passedTests);
        System.out.println("准确率: " + String.format("%.2f", accuracy) + "%");

        if (!failures.isEmpty()) {
            System.out.println("\n失败用例:");
            for (String f : failures) {
                System.out.println("  - " + f);
            }
        }

        assert accuracy >= 80.0 : "准确率应达到80%以上，当前为 " + accuracy + "%";
    }

    /**
     * 测试7：空查询测试
     */
    @Test
    void testEmptyQuery() {
        System.out.println("\n========== 测试7：空查询测试 ==========");

        Map<String, Object> result = knowledgeService.retrieveKnowledge("");
        int total = (int) result.get("total");
        System.out.println("空查询返回结果数: " + total);
        assert total == 0 : "空查询应返回0条结果";

        System.out.println("✅ 空查询处理正确");
    }

    /**
     * 测试8：无匹配查询测试
     */
    @Test
    void testNoMatchQuery() {
        System.out.println("\n========== 测试8：无匹配查询测试 ==========");

        String query = "完全不相关的内容abcdefg";
        Map<String, Object> result = knowledgeService.retrieveKnowledge(query, null, 5, 0.5);

        printRetrievalResult(query, result);

        List<?> results = (List<?>) result.get("results");
        assert results.isEmpty() : "无匹配时应返回0条结果";

        System.out.println("✅ 无匹配查询处理正确");
    }

    // ==================== 辅助方法 ====================

    /**
     * 打印检索结果
     */
    private void printRetrievalResult(String query, Map<String, Object> result) {
        System.out.println("查询: " + query);
        System.out.println("总结果数: " + result.get("total"));

        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> item = results.get(i);
            double similarity = (double) item.get("similarity");
            int importance = (int) item.get("importance");
            System.out.println("  " + (i + 1) + ". " + item.get("title") +
                    " (相似度: " + String.format("%.3f", similarity) +
                    ", 重要性: " + importance + ")");
        }
    }

    /**
     * 打印最佳结果
     */
    private void printTopResult(List<Map<String, Object>> results) {
        if (!results.isEmpty()) {
            Map<String, Object> top = results.get(0);
            double similarity = (double) top.get("similarity");
            System.out.println("  最佳匹配: " + top.get("title") +
                    " (相似度: " + String.format("%.3f", similarity) + ")");
        }
    }
}