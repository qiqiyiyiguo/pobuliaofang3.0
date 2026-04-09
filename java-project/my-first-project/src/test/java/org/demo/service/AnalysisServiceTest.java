package org.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @Test
    void testAnalyzeCorrectness() {
        System.out.println("\n========== 技术正确性评分测试 ==========");
        String question = "请介绍一下Java内存区域";
        String answer = "Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器";

        Map<String, Object> result = analysisService.analyzeCorrectness(question, answer, "Java后端", null);

        System.out.println("分数: " + result.get("score"));
        System.out.println("等级: " + result.get("level"));
        System.out.println("反馈: " + result.get("feedback"));
    }

    @Test
    void testAnalyzeDepth() {
        System.out.println("\n========== 知识深度评分测试 ==========");
        String question = "请介绍一下Java内存区域";
        String answer = "Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器。堆是线程共享的，存储对象实例；栈是线程私有的，存储局部变量和方法调用。JVM通过垃圾回收机制自动管理堆内存。";

        Map<String, Object> result = analysisService.analyzeDepth(question, answer, "Java后端");

        System.out.println("总分: " + result.get("totalScore"));
        System.out.println("等级: " + result.get("level"));
        System.out.println("维度得分: " + result.get("dimensions"));
        System.out.println("反馈: " + result.get("feedback"));
    }

    @Test
    void testAnalyzeLogic() {
        System.out.println("\n========== 逻辑严谨性评分测试 ==========");

        String question = "请分享一个你解决过的技术难题";
        String answer = "在电商项目中，我们遇到了高并发下库存超卖的问题。\n" +
                "【情境】项目是一个秒杀系统，高峰期QPS达到5000，出现了库存超卖现象。\n" +
                "【任务】我的任务是解决超卖问题，同时保证系统性能和用户体验。\n" +
                "【行动】我首先分析了问题的原因，发现是数据库行锁竞争激烈。然后我提出了使用Redis分布式锁的方案，在减库存前先获取锁。具体实现是使用Redisson框架，设置合理的超时时间，并实现了可重入锁机制。\n" +
                "【结果】实施后，超卖问题完全解决，系统并发能力提升了3倍，接口响应时间从200ms降低到50ms。";

        Map<String, Object> result = analysisService.analyzeLogic(question, answer, "Java后端", "project");

        System.out.println("总分: " + result.get("totalScore"));
        System.out.println("等级: " + result.get("level"));
        System.out.println("维度得分: " + result.get("dimensions"));
        System.out.println("STAR分析: " + result.get("starAnalysis"));
        System.out.println("优点: " + result.get("strengths"));
        System.out.println("不足: " + result.get("weaknesses"));
        System.out.println("建议: " + result.get("suggestions"));
        System.out.println("反馈: " + result.get("feedback"));
    }

    @Test
    void testAnalyzeMatch() {
        System.out.println("\n========== 岗位匹配度分析测试 ==========");

        String resume = "3年Java开发经验，熟练掌握Spring Boot、MyBatis、MySQL，有分布式系统开发经验，熟悉Redis、消息队列（RocketMQ），参与过电商项目开发，熟悉高并发场景下的缓存设计和性能优化。本科计算机专业毕业。";
        String jobDescription = "岗位要求：5年以上Java开发经验，精通Spring Cloud微服务架构，熟悉分布式系统设计，有高并发项目经验，熟悉Redis、Kafka等中间件，熟悉MySQL数据库优化，有团队管理经验者优先。学历要求：本科及以上。";

        Map<String, Object> result = analysisService.analyzeMatch(resume, jobDescription, "Java高级工程师", "张三");

        System.out.println("总分: " + result.get("totalScore"));
        System.out.println("等级: " + result.get("level"));
        System.out.println("维度得分: " + result.get("dimensions"));
        System.out.println("匹配技能: " + result.get("matchedSkills"));
        System.out.println("缺失技能: " + result.get("missingSkills"));
        System.out.println("优势: " + result.get("strengths"));
        System.out.println("不足: " + result.get("weaknesses"));
        System.out.println("建议: " + result.get("recommendations"));
        System.out.println("合适度: " + result.get("suitableLevel"));
        System.out.println("总结: " + result.get("summary"));
    }

    @Test
    void testAnalyzeSpeed() {
        System.out.println("\n========== 语速分析测试 ==========");

        // 测试用例：不同语速的场景
        Object[][] testCases = {
                {"Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器", 30},  // 快
                {"Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器", 45},  // 适中
                {"Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器", 80}   // 慢
        };

        for (Object[] testCase : testCases) {
            String message = (String) testCase[0];
            int duration = (int) testCase[1];

            Map<String, Object> result = analysisService.analyzeSpeed(message, duration, "TEST_SESSION");

            int wordCount = message.length();
            double speed = (wordCount / (double) duration) * 60;

            System.out.println("消息: " + message);
            System.out.println("字数: " + wordCount + "字, 用时: " + duration + "秒");
            System.out.println("语速: " + String.format("%.1f", speed) + "字/分钟");
            System.out.println("等级: " + result.get("level"));
            System.out.println("描述: " + result.get("description"));
            System.out.println("建议: " + result.get("suggestion"));
            System.out.println("---");
        }
    }

    @Test
    void testAnalyzeSentiment() {
        System.out.println("\n========== 文本情绪分析测试 ==========");

        // 测试不同情绪的文本
        String[] messages = {
                "这个问题太棒了！我正好准备过，可以详细讲讲我的理解，我觉得这个知识点特别有意思",  // 积极
                "嗯...这个问题我有点不太确定，可能不太会，让我想想该怎么回答",  // 中性（犹豫）
                "太难了，我完全不会，压力好大，不想继续了，这面试太打击人了"  // 消极
        };

        for (String message : messages) {
            Map<String, Object> result = analysisService.analyzeSentiment(message, "TEST_SESSION");

            System.out.println("文本: " + message);
            System.out.println("情绪: " + result.get("sentiment"));
            System.out.println("强度: " + result.get("intensity"));
            System.out.println("置信度: " + result.get("confidence"));

            // 安全地处理keywords数组
            Object keywordsObj = result.get("keywords");
            if (keywordsObj instanceof String[]) {
                String[] keywords = (String[]) keywordsObj;
                System.out.println("关键词: " + String.join(", ", keywords));
            } else {
                System.out.println("关键词: []");
            }

            System.out.println("心理状态: " + result.get("mentalState"));
            System.out.println("建议: " + result.get("suggestion"));
            System.out.println("---");
        }
    }

    @Test
    void testAnalyzeConfidence() {
        System.out.println("\n========== 自信度分析测试 ==========");

        // 测试不同自信度的文本
        String[] messages = {
                // 高自信
                "这个问题我肯定知道！Java内存区域包括方法区、堆、虚拟机栈、本地方法栈和程序计数器，我完全理解它们的作用。",

                // 中等偏高
                "Java内存区域主要有方法区、堆、虚拟机栈等，我比较熟悉这些概念。",

                // 中等
                "Java内存区域好像包括方法区、堆这些吧，可能还有虚拟机栈？",

                // 中等偏低
                "嗯...这个问题我不太确定，可能是方法区和堆？让我想想...",

                // 低自信
                "呃...这个...我不太会，可能...也许...我猜是内存相关的..."
        };

        for (String message : messages) {
            Map<String, Object> result = analysisService.analyzeConfidence(message, "TEST_SESSION");

            System.out.println("文本: " + message);
            System.out.println("自信度分数: " + result.get("confidenceScore"));
            System.out.println("等级: " + result.get("level"));
            System.out.println("描述: " + result.get("description"));
            System.out.println("建议: " + result.get("suggestion"));
            System.out.println("低自信词: " + result.get("lowConfidenceWords"));
            System.out.println("犹豫词: " + result.get("hesitationWords"));
            System.out.println("---");
        }
    }
}