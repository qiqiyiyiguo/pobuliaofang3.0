package org.demo.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.dao.KnowledgeRepository;
import org.demo.entity.Knowledge;
import org.demo.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库初始化器
 * 项目启动时自动导入知识库数据（仅当数据库为空时）
 */
@Component
public class KnowledgeInitRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeInitRunner.class);

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        // 如果已经有数据，跳过初始化
        if (knowledgeRepository.count() > 0) {
            log.info("知识库已有 {} 条数据，跳过初始化", knowledgeRepository.count());
            return;
        }

        log.info("开始初始化知识库...");

        List<Knowledge> knowledgeList = new ArrayList<>();

        // ==================== 1. 后端开发（Java）岗位知识 ====================
        knowledgeList.add(createKnowledge(
                "backend",
                "Java后端面试核心知识点",
                "## Java基础\n- JVM内存模型：堆、栈、方法区\n- 垃圾回收机制：可达性分析、GC算法\n- 多线程：synchronized、Lock、volatile\n- 集合框架：ArrayList、HashMap、ConcurrentHashMap\n\n## Spring框架\n- IOC和AOP原理\n- Spring Boot自动配置\n- Spring事务传播行为\n\n## 数据库\n- MySQL索引优化\n- SQL执行计划分析\n- 事务隔离级别",
                "Java,JVM,Spring,MySQL",
                "锐捷网络技术文档",
                5
        ));

        knowledgeList.add(createKnowledge(
                "backend",
                "Spring Boot面试常见问题",
                "## 常见问题及参考答案\n\n**Q: Spring Boot自动配置原理？**\nA: @SpringBootApplication包含@EnableAutoConfiguration，通过SpringFactoriesLoader加载META-INF/spring.factories中的配置类。\n\n**Q: 如何自定义starter？**\nA: 创建自动配置类，使用@Configuration和@Conditional注解，在spring.factories中注册。\n\n**Q: Spring Boot与Spring Cloud的关系？**\nA: Spring Boot是快速开发框架，Spring Cloud基于Spring Boot构建微服务生态。",
                "Spring Boot,自动配置,微服务",
                "锐捷网络技术文档",
                5
        ));

        knowledgeList.add(createKnowledge(
                "backend",
                "Redis面试知识点",
                "## Redis核心知识点\n\n### 数据类型\n- String：缓存、计数器\n- Hash：对象存储\n- List：消息队列\n- Set：去重、标签\n- ZSet：排行榜\n\n### 持久化\n- RDB：快照，适合备份\n- AOF：日志，数据更安全\n\n### 缓存问题\n- 穿透：缓存空值/布隆过滤器\n- 击穿：互斥锁/热点数据永不过期\n- 雪崩：随机过期时间/熔断降级",
                "Redis,缓存,持久化",
                "锐捷网络技术文档",
                5
        ));

        knowledgeList.add(createKnowledge(
                "backend",
                "MySQL优化面试指南",
                "## MySQL优化方向\n\n### 索引优化\n- 最左前缀原则\n- 覆盖索引减少回表\n- 避免索引失效（函数、类型转换、%开头的LIKE）\n\n### SQL优化\n- 使用EXPLAIN分析执行计划\n- 避免SELECT *\n- 分页优化：使用子查询或延迟关联\n\n### 架构优化\n- 读写分离\n- 分库分表",
                "MySQL,索引,SQL优化",
                "锐捷网络技术文档",
                4
        ));

        knowledgeList.add(createKnowledge(
                "backend",
                "消息队列面试题",
                "## 消息队列核心问题\n\n### 为什么使用MQ？\n- 异步处理：提高响应速度\n- 削峰填谷：应对流量高峰\n- 解耦：降低系统耦合度\n\n### 常见问题及解决方案\n- 消息丢失：持久化+确认机制\n- 重复消费：幂等设计\n- 消息积压：增加消费者/临时扩容",
                "消息队列,RabbitMQ,Kafka",
                "锐捷网络技术文档",
                4
        ));

        // ==================== 2. 前端开发（Web）岗位知识 ====================
        knowledgeList.add(createKnowledge(
                "frontend",
                "Vue3面试核心知识点",
                "## Vue3核心特性\n\n### 响应式原理\n- 使用Proxy代替Object.defineProperty\n- 可以监听数组变化和属性新增删除\n\n### Composition API\n- setup函数\n- ref和reactive\n- computed和watch\n- 生命周期钩子\n\n### 性能优化\n- 虚拟DOM和Diff算法\n- 静态提升\n- 事件监听缓存",
                "Vue3,响应式,Composition API",
                "锐捷网络技术文档",
                5
        ));

        knowledgeList.add(createKnowledge(
                "frontend",
                "JavaScript面试高频题",
                "## JavaScript核心\n\n### 闭包\n- 定义：函数和其周围状态的引用捆绑\n- 用途：封装私有变量、函数工厂\n- 注意：内存泄漏\n\n### 原型链\n- __proto__和prototype\n- 继承的实现方式\n\n### 事件循环\n- 宏任务：setTimeout、setInterval\n- 微任务：Promise.then、MutationObserver",
                "JavaScript,闭包,原型链,事件循环",
                "锐捷网络技术文档",
                5
        ));

        knowledgeList.add(createKnowledge(
                "frontend",
                "CSS面试知识点",
                "## CSS核心\n\n### 布局方式\n- Flex：一维布局\n- Grid：二维布局\n- 定位：relative/absolute/fixed\n\n### 响应式设计\n- 媒体查询\n- 视口单位\n- 弹性布局\n\n### 性能优化\n- 减少重绘重排\n- CSS动画代替JS动画",
                "CSS,Flex,Grid,响应式",
                "锐捷网络技术文档",
                4
        ));

        knowledgeList.add(createKnowledge(
                "frontend",
                "Webpack面试题",
                "## Webpack核心概念\n\n### 核心流程\n- Entry：入口\n- Output：输出\n- Loader：处理非JS文件\n- Plugin：扩展功能\n\n### 优化配置\n- 代码分割\n- Tree Shaking\n- 缓存优化",
                "Webpack,构建工具,前端工程化",
                "锐捷网络技术文档",
                4
        ));

        // ==================== 3. 锐捷企业文化 ====================
        knowledgeList.add(createKnowledge(
                "general",
                "锐捷网络企业介绍",
                "## 锐捷网络简介\n\n锐捷网络是行业领先的ICT基础设施及行业解决方案提供商，主营业务为网络设备、网络安全产品及云桌面解决方案的研发、设计和销售。\n\n### 核心业务\n- 网络设备：交换机、路由器、无线产品\n- 网络安全：防火墙、安全网关\n- 云桌面：云课堂、云办公\n\n### 企业成就\n- 中国网络设备三大供应商之一\n- 服务1000多家金融机构\n- 100%的双一流高校\n- 60%的全国百强医院\n- 超200家中国500强企业\n\n### 股票信息\n- 锐捷网络：301165\n- 星网锐捷：002396",
                "锐捷网络,企业文化,ICT",
                "锐捷网络官网",
                5
        ));

        knowledgeList.add(createKnowledge(
                "general",
                "锐捷网络核心价值观",
                "## 锐捷网络核心价值观\n\n### 贴近用户\n- 坚持深入用户场景\n- 与行业头部客户建立深度合作关系\n\n### 持续创新\n- 拥有8大研发中心\n- 8000余员工\n- 业务范围覆盖90多个国家和地区\n\n### 使命愿景\n- 助力各行业客户数字化转型\n- 夯实数字经济坚实底座\n- 勇立数字时代潮头",
                "锐捷网络,企业文化,价值观",
                "锐捷网络官网",
                5
        ));

        knowledgeList.add(createKnowledge(
                "general",
                "锐捷网络教育行业布局",
                "## 锐捷网络教育行业\n\n锐捷网络作为深耕教育行业数字化解决方案的先行者，在云课堂业务的长期实践中，始终关注从学习到就业的全链条人才培养。\n\n### 核心关注\n- 高校就业准备环节\n- 学生面试挑战\n- 缺乏真实互动场景\n- 缺乏即时、客观的反馈\n\n### 解决方案\n- AI模拟面试与能力提升平台\n- 将企业级面试场景与技术评估模型深度结合\n- 研发能够模拟真实技术面试、提供多维度智能反馈的AI教练",
                "锐捷网络,教育行业,AI面试",
                "锐捷网络官网",
                5
        ));

        // ==================== 4. 其他通用知识 ====================
        knowledgeList.add(createKnowledge(
                "general",
                "STAR法则面试技巧",
                "## STAR法则详解\n\nSTAR法则是面试中回答行为类问题的黄金框架：\n\n### S - Situation（情境）\n- 描述项目的背景和挑战\n- 说明当时的客观条件\n\n### T - Task（任务）\n- 明确你的职责和目标\n- 说清楚需要解决什么问题\n\n### A - Action（行动）\n- 具体采取了哪些措施\n- 体现你的思考过程和执行力\n\n### R - Result（结果）\n- 取得了什么样的成果\n- 尽量量化（提升了X%，节省了Y小时）\n\n**示例**：\nS：电商项目大促期间库存超卖严重\nT：需要解决高并发下库存准确性问题\nA：使用Redis分布式锁+数据库乐观锁双重保障\nR：超卖率降低99.9%，系统并发能力提升3倍",
                "STAR法则,面试技巧,行为面试",
                "面试经验总结",
                5
        ));

        knowledgeList.add(createKnowledge(
                "general",
                "如何回答技术面试题",
                "## 技术面试回答框架\n\n### 1. 确认问题\n- 复述问题确保理解正确\n- 追问边界条件\n\n### 2. 由浅入深\n- 先给出核心答案\n- 再补充细节和原理\n- 最后扩展到相关知识点\n\n### 3. 举例说明\n- 结合实际项目经验\n- 说明遇到过什么问题\n- 展示解决问题的思路\n\n### 4. 承认不足\n- 遇到不会的问题\n- 说明你了解的相关方向\n- 表示愿意学习的态度\n\n### 常见错误\n- 回答太简短，缺乏深度\n- 没有举例，只背概念\n- 不懂装懂，暴露硬伤",
                "面试技巧,技术面试,回答框架",
                "面试经验总结",
                4
        ));

        // 批量保存并生成向量
        for (Knowledge k : knowledgeList) {
            try {
                // 生成向量
                List<Float> embedding = aiService.generateEmbedding(k.getContent());
                if (embedding != null && !embedding.isEmpty()) {
                    k.setEmbedding(vectorToJson(embedding));
                    k.setEmbeddingDim(embedding.size());
                }
                knowledgeRepository.save(k);
                log.info("导入知识: {}", k.getTitle());
            } catch (Exception e) {
                log.error("导入知识失败: {}", k.getTitle(), e);
            }
        }

        log.info("知识库初始化完成，共导入 {} 条知识", knowledgeList.size());
    }

    private Knowledge createKnowledge(String jobPosition, String title, String content,
                                      String tags, String source, int importance) {
        Knowledge k = new Knowledge();
        k.setJobPosition(jobPosition);
        k.setTitle(title);
        k.setContent(content);
        k.setTags(tags);
        k.setSource(source);
        k.setKnowledgeType("技术文档");
        k.setImportance(importance);
        k.setStatus(1);
        return k;
    }

    private String vectorToJson(List<Float> vector) {
        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < vector.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(vector.get(i));
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}