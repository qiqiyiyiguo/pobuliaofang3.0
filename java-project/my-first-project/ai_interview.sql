-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: ai_interview
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `analysis_result`
--

DROP TABLE IF EXISTS `analysis_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analysis_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `confidence_avg` double DEFAULT NULL,
  `correctness_avg` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `depth_avg` double DEFAULT NULL,
  `dimension_details` json DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `improvements` json DEFAULT NULL,
  `interview_style` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `job_position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logic_avg` double DEFAULT NULL,
  `qa_list` json DEFAULT NULL,
  `sentiment_stats` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `session_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `speed_avg` double DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `strengths` json DEFAULT NULL,
  `summary` text COLLATE utf8mb4_unicode_ci,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_score` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `weaknesses` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `analysis_result`
--

LOCK TABLES `analysis_result` WRITE;
/*!40000 ALTER TABLE `analysis_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `analysis_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_reply` text COLLATE utf8mb4_unicode_ci,
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer_duration` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `difficulty` int DEFAULT NULL,
  `extra_data` json DEFAULT NULL,
  `job_position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `question` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `score` double DEFAULT NULL,
  `session_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `style` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview_report`
--

DROP TABLE IF EXISTS `interview_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) DEFAULT NULL COMMENT '报告标题',
  `job_position` varchar(50) DEFAULT NULL COMMENT '面试岗位',
  `total_score` decimal(5,2) DEFAULT NULL COMMENT '总分',
  `strengths` text COMMENT '优势分析',
  `weaknesses` text COMMENT '不足分析',
  `suggestions` text COMMENT '改进建议',
  `report_data` json DEFAULT NULL COMMENT '原始报告数据',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面试报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_report`
--

LOCK TABLES `interview_report` WRITE;
/*!40000 ALTER TABLE `interview_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `knowledge`
--

DROP TABLE IF EXISTS `knowledge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `embedding` json DEFAULT NULL,
  `embedding_dim` int DEFAULT NULL,
  `importance` int DEFAULT NULL,
  `job_position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `knowledge_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `reference_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` int DEFAULT NULL,
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `usage_count` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge`
--

LOCK TABLES `knowledge` WRITE;
/*!40000 ALTER TABLE `knowledge` DISABLE KEYS */;
/*!40000 ALTER TABLE `knowledge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_bank`
--

DROP TABLE IF EXISTS `question_bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `analysis` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `difficulty` int DEFAULT NULL,
  `job_position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reference_answer` text COLLATE utf8mb4_unicode_ci,
  `status` int DEFAULT NULL,
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type_id` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_bank`
--

LOCK TABLES `question_bank` WRITE;
/*!40000 ALTER TABLE `question_bank` DISABLE KEYS */;
INSERT INTO `question_bank` VALUES (1,NULL,NULL,'简述 Java 的垃圾回收算法有哪些？',3,'Java','Java 垃圾回收机制',NULL,NULL,'JVM,GC',1,NULL,NULL),(2,NULL,NULL,'请介绍一下 Java 内存区域的划分',2,'Java','Java 内存区域',NULL,NULL,'JVM,内存',1,NULL,NULL),(3,NULL,NULL,'请分享一个你做过的最有挑战性的项目',3,'Java','电商项目经验',NULL,NULL,'项目,电商',2,NULL,NULL),(4,NULL,NULL,'如何设计一个秒杀系统？',4,'Java','高并发场景题',NULL,NULL,'高并发,秒杀',3,NULL,NULL),(5,NULL,NULL,'当你和同事有技术分歧时怎么处理？',2,'通用','团队冲突',NULL,NULL,'沟通,团队',4,NULL,NULL),(6,NULL,NULL,'请解释封装、继承、多态的含义及使用场景。',1,'Java','Java 的三大特性是什么？',NULL,NULL,'基础,面向对象',1,NULL,NULL),(7,NULL,NULL,'请详细说明这三个关键字的区别和使用场景。',2,'Java','final、finally、finalize 的区别？',NULL,NULL,'基础,关键字',1,NULL,NULL),(8,NULL,NULL,'请说明三者的特点、使用场景和性能差异。',2,'Java','Java 中的 String、StringBuilder、StringBuffer 有什么区别？',NULL,NULL,'基础,String',1,NULL,NULL),(9,NULL,NULL,'请从线程安全、性能、null值等方面说明。',2,'Java','HashMap 和 Hashtable 的区别？',NULL,NULL,'集合,Map',1,NULL,NULL),(10,NULL,NULL,'请解释 ConcurrentHashMap 的底层实现和并发控制机制。',3,'Java','ConcurrentHashMap 的实现原理？',NULL,NULL,'集合,并发',1,NULL,NULL),(11,NULL,NULL,'请从底层数据结构、插入删除性能、随机访问等方面说明。',2,'Java','ArrayList 和 LinkedList 的区别？',NULL,NULL,'集合,List',1,NULL,NULL),(12,NULL,NULL,'请解释泛型的概念、类型擦除和使用场景。',2,'Java','什么是泛型？有什么好处？',NULL,NULL,'基础,泛型',1,NULL,NULL),(13,NULL,NULL,'请解释 JMM 的组成、作用及与 JVM 内存区域的关系。',3,'Java','Java 内存模型（JMM）是什么？',NULL,NULL,'JVM,内存',1,NULL,NULL),(14,NULL,NULL,'请说明标记-清除、复制、标记-整理等算法的原理和优缺点。',3,'Java','JVM 垃圾回收机制有哪些算法？',NULL,NULL,'JVM,GC',1,NULL,NULL),(15,NULL,NULL,'请说明引用计数法和可达性分析算法的原理。',3,'Java','如何判断对象可以回收？',NULL,NULL,'JVM,GC',1,NULL,NULL),(16,NULL,NULL,'请介绍 Serial、Parallel、CMS、G1 等垃圾回收器的特点。',3,'Java','常见的垃圾回收器有哪些？',NULL,NULL,'JVM,GC',1,NULL,NULL),(17,NULL,NULL,'请解释类加载器的双亲委派模型及作用。',3,'Java','Java 中的 ClassLoader 是什么？',NULL,NULL,'JVM,类加载',1,NULL,NULL),(18,NULL,NULL,'请说明线程的创建、就绪、运行、阻塞、死亡状态及转换过程。',2,'Java','线程有哪几种状态？如何转换？',NULL,NULL,'多线程',1,NULL,NULL),(19,NULL,NULL,'请从锁的获取方式、可重入性、性能、使用便利性等方面对比。',3,'Java','synchronized 和 Lock 的区别？',NULL,NULL,'多线程,并发',1,NULL,NULL),(20,NULL,NULL,'请解释 volatile 的可见性和禁止指令重排序特性。',3,'Java','volatile 关键字的作用？',NULL,NULL,'多线程,并发',1,NULL,NULL),(21,NULL,NULL,'请说明 corePoolSize、maxPoolSize、queueCapacity 等参数的作用。',3,'Java','线程池有哪些参数？如何配置？',NULL,NULL,'多线程,线程池',1,NULL,NULL),(22,NULL,NULL,'请解释 IoC 和 AOP 的概念及其实现原理。',2,'Java','Spring 框架的核心是什么？',NULL,NULL,'Spring,框架',1,NULL,NULL),(23,NULL,NULL,'请描述 Bean 从实例化到销毁的完整过程。',3,'Java','Spring Bean 的生命周期？',NULL,NULL,'Spring',1,NULL,NULL),(24,NULL,NULL,'请说明 REQUIRED、REQUIRES_NEW、NESTED 等传播行为。',3,'Java','Spring 事务的传播机制有哪些？',NULL,NULL,'Spring,事务',1,NULL,NULL),(25,NULL,NULL,'请说明两种占位符的区别及 SQL 注入问题。',2,'Java','MyBatis 中 #{} 和 ${} 的区别？',NULL,NULL,'MyBatis',1,NULL,NULL),(26,NULL,NULL,'请说明 B+Tree 索引的特点和优势。',2,'Java','MySQL 索引的数据结构是什么？',NULL,NULL,'数据库,MySQL',1,NULL,NULL),(27,NULL,NULL,'请解释覆盖索引的概念及其性能优化作用。',2,'Java','什么是索引覆盖？有什么好处？',NULL,NULL,'数据库,优化',1,NULL,NULL),(28,NULL,NULL,'请说明读未提交、读已提交、可重复读、串行化的特点。',2,'Java','MySQL 的四种隔离级别是什么？',NULL,NULL,'数据库,事务',1,NULL,NULL),(29,NULL,NULL,'请说明 String、Hash、List、Set、ZSet 的特点和使用场景。',2,'Java','Redis 支持哪些数据类型？',NULL,NULL,'Redis,缓存',1,NULL,NULL),(30,NULL,NULL,'请说明 RDB 和 AOF 的原理及优缺点。',2,'Java','Redis 的持久化方式有哪些？',NULL,NULL,'Redis',1,NULL,NULL),(31,NULL,NULL,'请说明基于 Redis 或 Zookeeper 的分布式锁实现原理。',3,'Java','什么是分布式锁？如何实现？',NULL,NULL,'分布式,锁',1,NULL,NULL),(32,NULL,NULL,'请说明微服务的拆分原则、优缺点及挑战。',3,'Java','微服务架构的优点和缺点？',NULL,NULL,'微服务',1,NULL,NULL),(33,NULL,NULL,'请说明 Eureka、Ribbon、Feign、Hystrix、Gateway 的作用。',3,'Java','Spring Cloud 有哪些核心组件？',NULL,NULL,'SpringCloud',1,NULL,NULL),(34,NULL,NULL,'请解释一致性、可用性、分区容错性的含义和取舍。',3,'Java','什么是 CAP 定理？',NULL,NULL,'分布式,理论',1,NULL,NULL),(35,NULL,NULL,'请说明资源命名、HTTP 方法、状态码等设计规范。',2,'Java','RESTful API 设计原则有哪些？',NULL,NULL,'API',1,NULL,NULL),(36,NULL,NULL,'请详细说明项目背景、技术选型、你的职责和取得的成果。',2,'Java','请介绍一个你最满意的项目。',NULL,NULL,'项目,经验',2,NULL,NULL),(37,NULL,NULL,'请分享一个具体的技术问题及解决过程。',2,'Java','项目中遇到过哪些技术难题？如何解决的？',NULL,NULL,'项目,问题',2,NULL,NULL),(38,NULL,NULL,'请说明代码规范、Code Review、单元测试等实践。',2,'Java','如何保证项目代码质量？',NULL,NULL,'项目,质量',2,NULL,NULL),(39,NULL,NULL,'请举例说明你做过哪些性能优化，效果如何。',3,'Java','项目中如何做性能优化？',NULL,NULL,'项目,优化',2,NULL,NULL),(40,NULL,NULL,'请说明选型时的考虑因素和决策过程。',2,'Java','如何对项目进行技术选型？',NULL,NULL,'项目,技术',2,NULL,NULL),(41,NULL,NULL,'请说明需求变更的处理流程和沟通方式。',2,'Java','项目中如何管理需求变更？',NULL,NULL,'项目,管理',2,NULL,NULL),(42,NULL,NULL,'请说明失败原因和从中得到的教训。',2,'Java','请分享一个项目失败或踩坑的经历。',NULL,NULL,'项目,经验',2,NULL,NULL),(43,NULL,NULL,'请说明团队协作中的沟通方式和进度管理方法。',2,'Java','如何协调团队开发进度？',NULL,NULL,'项目,团队',2,NULL,NULL),(44,NULL,NULL,'请从高并发、超卖、限流、降级等方面说明。',3,'Java','设计一个秒杀系统，需要考虑哪些问题？',NULL,NULL,'场景,秒杀',3,NULL,NULL),(45,NULL,NULL,'请说明算法选择、数据库设计、缓存策略等。',2,'Java','如何设计一个短链接生成服务？',NULL,NULL,'场景,设计',3,NULL,NULL),(46,NULL,NULL,'请说明 Redis ZSet 的使用和实时更新策略。',2,'Java','如何设计一个排行榜系统？',NULL,NULL,'场景,Redis',3,NULL,NULL),(47,NULL,NULL,'请说明幂等性设计和防重方案。',2,'Java','如何处理用户重复提交订单？',NULL,NULL,'场景,幂等',3,NULL,NULL),(48,NULL,NULL,'请说明长连接、消息队列、离线消息存储等方案。',3,'Java','如何设计一个消息推送系统？',NULL,NULL,'场景,消息',3,NULL,NULL),(49,NULL,NULL,'请说明令牌桶、漏桶算法及具体实现。',2,'Java','如何实现接口限流？',NULL,NULL,'场景,限流',3,NULL,NULL),(50,NULL,NULL,'请说明分片键的选择和分布式事务处理。',3,'Java','数据库分库分表怎么设计？',NULL,NULL,'场景,分库分表',3,NULL,NULL),(51,NULL,NULL,'请说明缓存更新策略和延迟双删方案。',3,'Java','如何保证缓存和数据库的一致性？',NULL,NULL,'场景,缓存',3,NULL,NULL),(52,NULL,NULL,'请分享你的工作态度和平衡方法。',1,'Java','你如何看待加班？',NULL,NULL,'行为,态度',4,NULL,NULL),(53,NULL,NULL,'请说明你的沟通方式和解决思路。',2,'Java','和同事发生技术分歧时如何处理？',NULL,NULL,'行为,沟通',4,NULL,NULL),(54,NULL,NULL,'请说明你的应对策略和沟通方式。',2,'Java','遇到一个无法按时完成的任务怎么办？',NULL,NULL,'行为,压力',4,NULL,NULL),(55,NULL,NULL,'请说明短期目标和长期发展方向。',1,'Java','你对自己的职业规划是什么？',NULL,NULL,'行为,规划',4,NULL,NULL),(56,NULL,NULL,'请分享你的学习方法、渠道和实践经验。',1,'Java','你如何保持技术学习？',NULL,NULL,'行为,学习',4,NULL,NULL),(57,NULL,NULL,'请说明你的指导方法和经验传授方式。',2,'Java','如果让你带新人，你会怎么做？',NULL,NULL,'行为,指导',4,NULL,NULL),(58,NULL,NULL,'请说明可变性、性能、使用场景等方面的差异。',1,'Python','Python 中列表和元组的区别？',NULL,NULL,'基础,数据结构',1,NULL,NULL),(59,NULL,NULL,'请解释装饰器的概念、应用场景及实现方式。',2,'Python','Python 的装饰器是什么？如何实现？',NULL,NULL,'语法,装饰器',1,NULL,NULL),(60,NULL,NULL,'请说明两者的定义、使用方式及内存效率。',2,'Python','Python 生成器和迭代器的区别？',NULL,NULL,'高级,迭代器',1,NULL,NULL),(61,NULL,NULL,'请说明 copy 和 deepcopy 的区别及使用场景。',2,'Python','Python 中的深拷贝和浅拷贝有什么区别？',NULL,NULL,'基础,拷贝',1,NULL,NULL),(62,NULL,NULL,'请解释全局解释器锁的作用及对多线程的影响。',3,'Python','Python 的 GIL 是什么？有什么影响？',NULL,NULL,'高级,多线程',1,NULL,NULL),(63,NULL,NULL,'请说明引用计数和分代回收的原理。',2,'Python','Python 的垃圾回收机制是怎样的？',NULL,NULL,'内存,GC',1,NULL,NULL),(64,NULL,NULL,'请说明使用多进程、异步IO、C扩展等方法。',3,'Python','如何优化 Python 代码性能？',NULL,NULL,'优化',1,NULL,NULL),(65,NULL,NULL,'请说明可变参数的使用场景和注意事项。',2,'Python','Python 中 *args 和 **kwargs 的用法？',NULL,NULL,'语法,参数',1,NULL,NULL),(66,NULL,NULL,'请解释闭包的概念及使用场景。',2,'Python','Python 的闭包是什么？',NULL,NULL,'高级,闭包',1,NULL,NULL),(67,NULL,NULL,'请说明 @classmethod 和 @staticmethod 的区别。',2,'Python','Python 中的类方法和静态方法有什么区别？',NULL,NULL,'面向对象',1,NULL,NULL),(68,NULL,NULL,'请说明 os、sys、re、datetime、json 等模块的用途。',1,'Python','列举 Python 常用的内置模块。',NULL,NULL,'模块',1,NULL,NULL),(69,NULL,NULL,'请说明 try-except-finally 的使用和自定义异常。',1,'Python','Python 的异常处理机制是怎样的？',NULL,NULL,'基础,异常',1,NULL,NULL),(70,NULL,NULL,'请说明列表推导式的语法和应用场景。',1,'Python','什么是列表推导式？请举例说明。',NULL,NULL,'语法,推导式',1,NULL,NULL),(71,NULL,NULL,'请说明使用模块、装饰器、__new__ 等方法。',2,'Python','Python 中如何实现单例模式？',NULL,NULL,'设计模式',1,NULL,NULL),(72,NULL,NULL,'请说明上下文管理器的原理和使用。',2,'Python','Python 的 with 语句是什么？',NULL,NULL,'语法',1,NULL,NULL),(73,NULL,NULL,'请说明 threading 模块的使用和注意事项。',2,'Python','Python 中如何实现多线程？',NULL,NULL,'多线程',1,NULL,NULL),(74,NULL,NULL,'请说明 multiprocessing 模块的使用。',2,'Python','Python 中如何实现多进程？',NULL,NULL,'多进程',1,NULL,NULL),(75,NULL,NULL,'请说明 asyncio 模块和 async/await 语法。',3,'Python','Python 中如何实现异步编程？',NULL,NULL,'异步',1,NULL,NULL),(76,NULL,NULL,'请说明数据科学中这两个库的作用。',2,'Python','NumPy 和 Pandas 的主要用途是什么？',NULL,NULL,'数据分析',1,NULL,NULL),(77,NULL,NULL,'请说明冒泡、快速、归并等排序算法的复杂度。',2,'Python','常见的排序算法有哪些？请说明时间复杂度。',NULL,NULL,'算法',1,NULL,NULL),(78,NULL,NULL,'请说明快慢指针的原理和实现。',2,'Python','如何判断一个链表是否有环？',NULL,NULL,'算法,链表',1,NULL,NULL),(79,NULL,NULL,'请说明递归和层序遍历两种方法。',2,'Python','二叉树的最大深度如何计算？',NULL,NULL,'算法,二叉树',1,NULL,NULL),(80,NULL,NULL,'请说明双指针方法的实现。',1,'Python','如何判断一个字符串是否是回文串？',NULL,NULL,'算法,字符串',1,NULL,NULL),(81,NULL,NULL,'请说明使用 OrderedDict 或哈希表加双向链表的实现。',3,'Python','如何实现一个 LRU 缓存？',NULL,NULL,'算法,缓存',1,NULL,NULL),(82,NULL,NULL,'请说明使用快速选择算法或堆排序的方法。',3,'Python','求一个数组中的第 K 大元素。',NULL,NULL,'算法,排序',1,NULL,NULL),(83,NULL,NULL,'请说明摩尔投票算法的原理。',2,'Python','如何找出数组中出现次数超过一半的元素？',NULL,NULL,'算法,数组',1,NULL,NULL),(84,NULL,NULL,'请说明迭代法和递归法的实现。',2,'Python','如何反转一个单链表？',NULL,NULL,'算法,链表',1,NULL,NULL),(85,NULL,NULL,'请说明中序遍历或递归验证的方法。',2,'Python','如何验证一个二叉树是否是二叉搜索树？',NULL,NULL,'算法,二叉树',1,NULL,NULL),(86,NULL,NULL,'请说明双重检查锁或使用模块级变量的方法。',3,'Python','如何实现一个线程安全的单例模式？',NULL,NULL,'算法,设计模式',1,NULL,NULL),(87,NULL,NULL,'请说明数据处理、分析和可视化的完整过程。',2,'Python','请介绍一个你参与的数据分析项目。',NULL,NULL,'项目,数据分析',2,NULL,NULL),(88,NULL,NULL,'请说明数据预处理、特征工程、模型选择和评估的流程。',3,'Python','请分享一个机器学习项目经验。',NULL,NULL,'项目,机器学习',2,NULL,NULL),(89,NULL,NULL,'请举例说明你做过哪些性能优化措施。',2,'Python','项目中如何优化算法性能？',NULL,NULL,'项目,优化',2,NULL,NULL),(90,NULL,NULL,'请说明反爬处理、数据解析和存储的方法。',2,'Python','请分享一个爬虫项目的经验。',NULL,NULL,'项目,爬虫',2,NULL,NULL),(91,NULL,NULL,'请说明使用生成器、分块处理等方法。',2,'Python','项目中如何处理大数据量？',NULL,NULL,'项目,大数据',2,NULL,NULL),(92,NULL,NULL,'请说明协同过滤、内容过滤等推荐算法的选择。',3,'Python','如何设计一个推荐系统？',NULL,NULL,'场景,推荐',3,NULL,NULL),(93,NULL,NULL,'请说明请求控制、解析、去重、存储等设计。',2,'Python','如何实现一个网页爬虫？',NULL,NULL,'场景,爬虫',3,NULL,NULL),(94,NULL,NULL,'请说明在 Spark 或分布式计算中的处理方案。',3,'Python','如何处理数据倾斜问题？',NULL,NULL,'场景,数据',3,NULL,NULL),(95,NULL,NULL,'请说明 Kafka、Spark Streaming、Flink 等技术选型。',3,'Python','如何设计一个实时数据处理系统？',NULL,NULL,'场景,实时',3,NULL,NULL),(96,NULL,NULL,'请说明轮询、最小连接数等算法的实现。',2,'Python','如何实现一个简单的负载均衡器？',NULL,NULL,'场景,负载均衡',3,NULL,NULL),(97,NULL,NULL,'请说明你的处理方式和经验。',2,'Python','你如何看待技术债务？',NULL,NULL,'行为,技术',4,NULL,NULL),(98,NULL,NULL,'请分享你的学习方法论。',1,'Python','你如何快速学习一门新技术？',NULL,NULL,'行为,学习',4,NULL,NULL),(99,NULL,NULL,'请举例说明你的沟通方式。',2,'Python','如何向非技术人员解释技术概念？',NULL,NULL,'行为,沟通',4,NULL,NULL),(100,NULL,NULL,'请说明你的排查思路和解决方法。',2,'Python','遇到一个从未遇到过的问题怎么办？',NULL,NULL,'行为,问题解决',4,NULL,NULL),(101,NULL,NULL,'请说明你的取舍原则和实践经验。',2,'Python','你如何平衡代码质量和交付速度？',NULL,NULL,'行为,平衡',4,NULL,NULL);
/*!40000 ALTER TABLE `question_bank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_type`
--

DROP TABLE IF EXISTS `question_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_type`
--

LOCK TABLES `question_type` WRITE;
/*!40000 ALTER TABLE `question_type` DISABLE KEYS */;
INSERT INTO `question_type` VALUES (1,NULL,NULL,NULL,NULL,'技术知识',1,NULL,NULL),(2,NULL,NULL,NULL,NULL,'项目经历',2,NULL,NULL),(3,NULL,NULL,NULL,NULL,'场景题',3,NULL,NULL),(4,NULL,NULL,NULL,NULL,'行为题',4,NULL,NULL);
/*!40000 ALTER TABLE `question_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-22 17:27:57
