package org.demo.controller;

import org.demo.entity.User;
import org.demo.dao.UserRepository;
import org.demo.dao.AnalysisResultRepository;
import org.demo.dao.HistoryRepository;
import org.demo.dao.QuestionBankRepository;
import org.demo.entity.AnalysisResult;
import org.demo.entity.History;
import org.demo.entity.QuestionBank;
import org.demo.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.demo.dao.UserWeaknessRepository;
import org.demo.entity.UserWeakness;
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.service.AnalysisService;


/**
 * 对话控制器
 * 处理AI分析相关的接口，支持面试多轮对话
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final AIService aiService;
    private final QuestionBankRepository questionBankRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper;
    @Autowired
    private UserWeaknessRepository userWeaknessRepository;


    // 使用线程安全的Map模拟会话存储（生产环境应使用Redis）
    private final Map<String, String> sessionCurrentQuestions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionJobPositions = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionQuestionIndex = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    // 构造器注入（包含所有依赖）
    @Autowired
    public ChatController(AIService aiService,
                          QuestionBankRepository questionBankRepository,
                          AnalysisResultRepository analysisResultRepository,
                          HistoryRepository historyRepository,
                          UserRepository userRepository,
                          AnalysisService analysisService,
                          ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.questionBankRepository = questionBankRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.analysisService = analysisService;
        this.objectMapper = objectMapper;
    }

    // ==================== 面试对话接口 ====================

    /**
     * 开始新面试
     * POST /api/chat/start
     */
    @PostMapping("/start")
    public Map<String, Object> startInterview(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jobPosition = (String) params.get("jobPosition");
            String sessionId = (String) params.get("session_id");

            if (jobPosition == null || jobPosition.trim().isEmpty()) {
                jobPosition = "Java后端";
            }

            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = "session_" + System.currentTimeMillis();
            }

            // 将前端岗位代码映射到数据库岗位名称
            String dbJobPosition = mapPositionToDb(jobPosition);

            // 获取第一道题（使用映射后的岗位名称）
            String firstQuestion = getRandomQuestion(dbJobPosition);
            if (firstQuestion == null) {
                result.put("code", 404);
                result.put("message", "该岗位暂无题目");
                result.put("data", null);
                return result;
            }

            // 保存会话状态（保存映射后的岗位名称）
            sessionJobPositions.put(sessionId, dbJobPosition);
            sessionCurrentQuestions.put(sessionId, firstQuestion);
            sessionQuestionIndex.put(sessionId, 0);
            sessionHistory.put(sessionId, new ArrayList<>());

            Map<String, Object> data = new HashMap<>();
            data.put("session_id", sessionId);
            data.put("first_question", firstQuestion);
            data.put("job_position", dbJobPosition);
            data.put("message", "面试开始");

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("开始面试失败", e);
            result.put("code", 500);
            result.put("message", "开始面试失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 面试对话接口 - 核心
     * POST /api/chat/message
     */
    @PostMapping("/message")
    public Map<String, Object> chatMessage(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sessionId = (String) params.get("session_id");
            String message = (String) params.get("message");
            String context = (String) params.get("context");  // 新增：接收RAG检索结果
            Integer duration = params.get("duration") != null ?
                    Integer.parseInt(params.get("duration").toString()) : 0;
            // ========== 新增：接收本题回答耗时 ==========
            Integer answerDuration = params.get("answerDuration") != null ?
                    Integer.parseInt(params.get("answerDuration").toString()) : 0;

            if (sessionId == null || sessionId.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "会话ID不能为空");
                result.put("data", null);
                return result;
            }

            if (message == null || message.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "消息不能为空");
                result.put("data", null);
                return result;
            }

            // 获取会话状态
            String jobPosition = sessionJobPositions.get(sessionId);
            String currentQuestion = sessionCurrentQuestions.get(sessionId);

            if (jobPosition == null) {
                jobPosition = "Java后端";
            }
            if (currentQuestion == null) {
                currentQuestion = "请介绍一下你自己";
            }

            // 调用AI分析回答（传入context）
            String aiReply = aiService.analyzeAnswerWithContext(
                    jobPosition, "友好", currentQuestion, message, context
            );

            // ========== 多维度评分 ==========
            Map<String, Object> multiDimScores = new ConcurrentHashMap<>();
            try {
                // 创建 final 副本供 lambda 使用
                final String finalCurrentQuestion = currentQuestion;
                final String finalMessage = message;
                final String finalJobPosition = jobPosition;
                final String finalSessionId = sessionId;
                final int finalAnswerDuration = answerDuration;

                // 使用线程池并行执行
                ExecutorService executor = Executors.newFixedThreadPool(4);
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // 1. 技术正确性评分
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, Object> correctnessRes = analysisService.analyzeCorrectness(
                            finalCurrentQuestion, finalMessage, finalJobPosition, null);
                    multiDimScores.put("correctness", correctnessRes);
                    log.info("技术正确性评分: {}", correctnessRes.get("score"));
                }, executor));

                // 2. 知识深度评分
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, Object> depthRes = analysisService.analyzeDepth(
                            finalCurrentQuestion, finalMessage, finalJobPosition);
                    multiDimScores.put("depth", depthRes);
                    log.info("知识深度评分: {}", depthRes.get("totalScore"));
                }, executor));

                // 3. 逻辑严谨性评分
                futures.add(CompletableFuture.runAsync(() -> {
                    String questionType = determineQuestionTypeByQuestion(finalCurrentQuestion);
                    Map<String, Object> logicRes = analysisService.analyzeLogic(
                            finalCurrentQuestion, finalMessage, finalJobPosition, questionType);
                    multiDimScores.put("logic", logicRes);
                    log.info("逻辑严谨性评分: {}", logicRes.get("totalScore"));
                }, executor));

                // 4. 自信度评分（本地计算，很快）
                Map<String, Object> confidenceRes = analysisService.analyzeConfidence(finalMessage, finalSessionId);
                multiDimScores.put("confidence", confidenceRes);
                log.info("自信度评分: {}", confidenceRes.get("confidenceScore"));

                // 5. 情绪评分
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, Object> sentimentRes = analysisService.analyzeSentiment(finalMessage, finalSessionId);
                    multiDimScores.put("sentiment", sentimentRes);
                    log.info("情绪分析: {}", sentimentRes.get("sentiment"));
                }, executor));

                // 6. 语速评分（本地计算，很快）
                if (finalAnswerDuration > 0) {
                    Map<String, Object> speedRes = analysisService.analyzeSpeed(
                            finalMessage, finalAnswerDuration, finalSessionId);
                    multiDimScores.put("speed", speedRes);
                    log.info("语速评分: {} - {}", speedRes.get("level"), speedRes.get("speed"));
                }

                // 等待所有并行任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                executor.shutdown();

            } catch (Exception e) {
                log.error("多维度评分失败: {}", e.getMessage(), e);
            }
            // ========================================

            // ========== 记录用户薄弱点 ==========
// 从多维度评分中获取总分
            double questionScore = 60; // 默认分
            if (multiDimScores.containsKey("correctness")) {
                Map<String, Object> correctness = (Map<String, Object>) multiDimScores.get("correctness");
                if (correctness.containsKey("score")) {
                    Object scoreObj = correctness.get("score");
                    if (scoreObj instanceof Number) {
                        questionScore = ((Number) scoreObj).doubleValue();
                    }
                }
            }

// 获取用户ID
            Long userId = extractUserIdFromToken(null);
            if (userId == null) {
                userId = 1L;
            }

            String questionTypeForRecord = getQuestionTypeForRecord(currentQuestion);
            updateUserWeakness(userId, jobPosition, questionTypeForRecord, questionScore);
// ====================================
            // 获取下一题
            String nextQuestion = getRandomQuestion(jobPosition, userId);

            // 保存问答记录（包含多维度评分）
            Map<String, String> record = new HashMap<>();
            record.put("question", currentQuestion);
            record.put("answer", message);
            record.put("aiReply", aiReply);
            record.put("timestamp", LocalDateTime.now().toString());
            // 将多维度评分转为JSON字符串保存
            try {
                String scoresJson = objectMapper.writeValueAsString(multiDimScores);
                record.put("multiDimScores", scoresJson);
            } catch (Exception e) {
                log.error("保存多维度评分失败: {}", e.getMessage());
            }

            List<Map<String, String>> history = sessionHistory.getOrDefault(sessionId, new ArrayList<>());
            history.add(record);
            sessionHistory.put(sessionId, history);

            // 更新当前问题
            sessionCurrentQuestions.put(sessionId, nextQuestion);

            // 更新题目索引
            int currentIndex = sessionQuestionIndex.getOrDefault(sessionId, 0);
            sessionQuestionIndex.put(sessionId, currentIndex + 1);

            // 检查是否结束（5题后结束）
            boolean isEnd = (currentIndex + 1) >= 5;
            if (isEnd) {
                nextQuestion = "面试结束，感谢您的参与！我们会尽快反馈结果。";
                aiReply = "本次面试已结束。综合来看，您在技术理解和表达方面表现良好。";
            }

            Map<String, Object> data = new HashMap<>();
            data.put("session_id", sessionId);
            data.put("reply", aiReply);
            data.put("next_question", nextQuestion);
            data.put("is_end", isEnd);
            data.put("question_index", currentIndex + 1);
            data.put("created_at", LocalDateTime.now().toString());

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("对话处理失败", e);
            result.put("code", 500);
            result.put("message", "对话处理失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 结束面试并评分
     * POST /api/chat/end
     */
    @PostMapping("/end")
    public Map<String, Object> endInterview(@RequestBody Map<String, Object> params,
                                            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sessionId = (String) params.get("session_id");

            // 从 token 获取用户 ID
            Long userId = null;
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.replace("Bearer ", "").trim();
                Optional<User> userOpt = userRepository.findByToken(token);
                if (userOpt.isPresent()) {
                    userId = userOpt.get().getId();
                }
            }

            if (userId == null) {
                userId = 1L;
                log.warn("未找到用户，使用默认用户ID: {}", userId);
            }

            List<Map<String, String>> history = sessionHistory.getOrDefault(sessionId, new ArrayList<>());
            String jobPosition = sessionJobPositions.get(sessionId);

            // ========== 使用 AI 真实评分，替代随机数 ==========
            double[] realScores = calculateRealScoresWithAI(history);
            double averageScore = realScores[0];
            double avgTech = realScores[1];
            double avgCommunication = realScores[2];
            double avgLogic = realScores[3];

            // 保存到 AnalysisResult 表
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setUserId(userId);
            analysisResult.setSessionId(sessionId);
            analysisResult.setJobPosition(jobPosition);
            analysisResult.setTotalScore(Math.round(averageScore * 10) / 10.0);
            analysisResult.setCorrectnessAvg(Math.round(avgTech * 10) / 10.0);
            analysisResult.setConfidenceAvg(Math.round(avgCommunication * 10) / 10.0);
            analysisResult.setLogicAvg(Math.round(avgLogic * 10) / 10.0);
            analysisResult.setStrengths("[\"技术基础扎实\", \"表达清晰\"]");
            analysisResult.setWeaknesses("[\"深入分析不足\", \"举例不够具体\"]");
            analysisResult.setImprovements("[\"多练习项目经验描述\", \"准备具体案例\"]");
            analysisResult.setSummary("整体表现良好，建议加强项目细节的讲解能力。");
            analysisResult.setCreatedAt(LocalDateTime.now());
            analysisResult.setUpdatedAt(LocalDateTime.now());

            analysisResultRepository.save(analysisResult);

            // 保存每条问答记录到 History 表（使用真实评分）
            for (Map<String, String> record : history) {
                History historyRecord = new History();
                historyRecord.setUserId(userId);
                historyRecord.setSessionId(sessionId);
                historyRecord.setJobPosition(jobPosition);
                historyRecord.setQuestion(record.get("question"));
                historyRecord.setAnswer(record.get("answer"));
                historyRecord.setAiReply(record.get("aiReply"));
                // 使用真实评分
                double realScore = extractScoreFromAIResponse(record.get("aiReply"), record.get("answer"));
                historyRecord.setScore(realScore);
                historyRecord.setCreatedAt(LocalDateTime.now());
                historyRecord.setUpdatedAt(LocalDateTime.now());
                historyRepository.save(historyRecord);
            }

            // 清理会话数据
            sessionCurrentQuestions.remove(sessionId);
            sessionJobPositions.remove(sessionId);
            sessionQuestionIndex.remove(sessionId);
            sessionHistory.remove(sessionId);

            Map<String, Object> data = new HashMap<>();
            data.put("session_id", sessionId);
            data.put("total_questions", history.size());
            data.put("total_score", Math.round(averageScore * 10) / 10.0);
            data.put("tech_score", Math.round(avgTech * 10) / 10.0);
            data.put("communication_score", Math.round(avgCommunication * 10) / 10.0);
            data.put("logic_score", Math.round(avgLogic * 10) / 10.0);
            data.put("report_id", analysisResult.getId());
            data.put("created_at", LocalDateTime.now().toString());

            result.put("code", 200);
            result.put("message", "面试结束");
            result.put("data", data);

        } catch (Exception e) {
            log.error("结束面试失败", e);
            result.put("code", 500);
            result.put("message", "结束面试失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    // ==================== 真实评分辅助方法 ====================

    /**
     * 使用 AI 评价计算真实分数
     */
    private double[] calculateRealScoresWithAI(List<Map<String, String>> history) {
        if (history == null || history.isEmpty()) {
            return new double[]{0, 0, 0, 0};
        }

        double totalScore = 0;
        double techScore = 0;
        double communicationScore = 0;
        double logicScore = 0;
        int techCount = 0, commCount = 0, logicCount = 0;

        for (Map<String, String> record : history) {
            String question = record.get("question");
            String answer = record.get("answer");
            String aiReply = record.get("aiReply");

            // 计算基础分数
            double baseScore = extractScoreFromAIResponse(aiReply, answer);

            // 根据问题类型分配维度权重
            if (isTechnicalQuestion(question)) {
                // 技术类问题：技术维度权重最高
                techScore += baseScore * 1.0;
                logicScore += baseScore * 0.6;
                communicationScore += baseScore * 0.4;
                techCount++;
                logicCount++;
                commCount++;
            } else if (isBehaviorQuestion(question)) {
                // 行为类问题：沟通维度权重最高
                communicationScore += baseScore * 1.0;
                logicScore += baseScore * 0.7;
                techScore += baseScore * 0.3;
                commCount++;
                logicCount++;
                techCount++;
            } else if (isLogicQuestion(question)) {
                // 逻辑/设计类问题：逻辑维度权重最高
                logicScore += baseScore * 1.0;
                techScore += baseScore * 0.5;
                communicationScore += baseScore * 0.5;
                logicCount++;
                techCount++;
                commCount++;
            } else {
                // 默认：各维度均等
                techScore += baseScore;
                communicationScore += baseScore;
                logicScore += baseScore;
                techCount++;
                commCount++;
                logicCount++;
            }

            totalScore += baseScore;
        }

        int questionCount = history.size();
        double avgTotal = questionCount > 0 ? totalScore / questionCount : 0;
        double avgTech = techCount > 0 ? techScore / techCount : 0;
        double avgComm = commCount > 0 ? communicationScore / commCount : 0;
        double avgLogic = logicCount > 0 ? logicScore / logicCount : 0;

        return new double[]{avgTotal, avgTech, avgComm, avgLogic};
    }

    /**
     * 判断是否是技术类问题
     */
    private boolean isTechnicalQuestion(String question) {
        if (question == null) return false;
        String lowerQ = question.toLowerCase();
        return lowerQ.contains("java") || lowerQ.contains("内存") || lowerQ.contains("线程") ||
                lowerQ.contains("string") || lowerQ.contains("hash") || lowerQ.contains("list") ||
                lowerQ.contains("set") || lowerQ.contains("zset") || lowerQ.contains("redis") ||
                lowerQ.contains("jmm") || lowerQ.contains("jvm") || lowerQ.contains("垃圾回收") ||
                lowerQ.contains("spring") || lowerQ.contains("mysql") || lowerQ.contains("索引");
    }

    /**
     * 判断是否是行为/态度类问题
     */
    private boolean isBehaviorQuestion(String question) {
        if (question == null) return false;
        String lowerQ = question.toLowerCase();
        return lowerQ.contains("态度") || lowerQ.contains("平衡") || lowerQ.contains("压力") ||
                lowerQ.contains("团队") || lowerQ.contains("沟通") || lowerQ.contains("协作") ||
                lowerQ.contains("负责") || lowerQ.contains("项目经历") || lowerQ.contains("介绍") ||
                lowerQ.contains("分享");
    }

    /**
     * 判断是否是逻辑/设计类问题
     */
    private boolean isLogicQuestion(String question) {
        if (question == null) return false;
        String lowerQ = question.toLowerCase();
        return lowerQ.contains("设计") || lowerQ.contains("系统") || lowerQ.contains("架构") ||
                lowerQ.contains("如何") || lowerQ.contains("场景") || lowerQ.contains("策略") ||
                lowerQ.contains("方案") || lowerQ.contains("优化") || lowerQ.contains("解决");
    }

    /**
     * 从 AI 回复中提取分数
     */
    private double extractScoreFromAIResponse(String aiReply, String answer) {
        if (aiReply == null || aiReply.isEmpty()) {
            return 60;
        }

        String lowerReply = aiReply.toLowerCase();
        int answerLength = answer == null ? 0 : answer.length();
        double score = 60;

        // 特别处理"不知道"、"不清楚"等回答
        if (answer != null) {
            if (answer.contains("不知道") || answer.contains("没想法") ||
                    answer.contains("不了解") || answer.contains("不清楚")) {
                score = 25;  // 不知道给低分
            } else if (answer.contains("太难了") || answer.contains("不会") || answer.contains("不懂")) {
                score = 30;  // 太难了给稍低分
            } else if (answer.length() < 5) {
                score = 35;  // 回答太短
            }
        }

        // 回答长度加分
        if (answerLength > 200) score += 15;
        else if (answerLength > 100) score += 10;
        else if (answerLength > 50) score += 5;
        else if (answerLength < 10) score -= 10;

        // 根据 AI 评价关键词加分
        if (lowerReply.contains("详细") || lowerReply.contains("清晰") || lowerReply.contains("准确")) score += 15;
        if (lowerReply.contains("具体") || lowerReply.contains("完整") || lowerReply.contains("深入")) score += 10;
        if (lowerReply.contains("正确") || lowerReply.contains("合理") || lowerReply.contains("有道理")) score += 5;
        if (lowerReply.contains("诚实") || lowerReply.contains("坦诚")) score += 3;  // 诚实加分少一些

        // 根据 AI 评价关键词扣分
        if (lowerReply.contains("简略") || lowerReply.contains("简短") || lowerReply.contains("不足")) score -= 15;
        if (lowerReply.contains("错误") || lowerReply.contains("不正确") || lowerReply.contains("偏差")) score -= 20;
        if (lowerReply.contains("缺乏") || lowerReply.contains("未涉及") || lowerReply.contains("没有")) score -= 15;
        if (lowerReply.contains("不知道") || lowerReply.contains("没想法") || lowerReply.contains("不了解")) score -= 10;
        if (lowerReply.contains("空") || lowerReply.contains("未提供") || lowerReply.contains("无")) score -= 20;
        if (lowerReply.contains("简略") && lowerReply.contains("缺乏")) score -= 5;

        return Math.max(0, Math.min(100, score));
    }



    /**
     * 从 Authorization Header 中提取用户 ID
     */
    private Long extractUserIdFromToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Authorization header 为空");
            return null;
        }

        String token = authorization.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            log.warn("Token 为空");
            return null;
        }

        Optional<User> userOpt = userRepository.findByToken(token);
        if (userOpt.isPresent()) {
            return userOpt.get().getId();
        }

        log.warn("未找到 token 对应的用户: {}", token);
        return null;
    }

    /**
     * 获取面试历史
     */
    @GetMapping("/history")
    public Map<String, Object> getHistory(@RequestParam("session_id") String sessionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, String>> history = sessionHistory.getOrDefault(sessionId, new ArrayList<>());
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", history);
        } catch (Exception e) {
            log.error("获取历史失败", e);
            result.put("code", 500);
            result.put("message", "获取历史失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    // ==================== AI分析接口 ====================

    @PostMapping("/analyze")
    public Map<String, Object> analyzeAnswer(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String question = params.get("question") != null ? params.get("question").toString() : null;
            String answer = params.get("answer") != null ? params.get("answer").toString() : null;
            String jobPosition = params.get("jobPosition") != null ? params.get("jobPosition").toString() : null;
            String style = params.get("style") != null ? params.get("style").toString() : null;

            if (question == null || question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题内容不能为空");
                result.put("data", null);
                return result;
            }

            if (answer == null || answer.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "回答内容不能为空");
                result.put("data", null);
                return result;
            }

            if (jobPosition == null || jobPosition.trim().isEmpty()) jobPosition = "通用";
            if (style == null || style.trim().isEmpty()) style = "友好";

            String evaluation = aiService.analyzeAnswer(jobPosition, style, question, answer);

            Map<String, Object> data = new HashMap<>();
            data.put("evaluation", evaluation);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            log.error("AI分析服务异常", e);
            result.put("code", 500);
            result.put("message", "AI分析服务异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    @PostMapping("/ai/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String message = params.get("message") != null ? params.get("message").toString() : null;

            if (message == null || message.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "消息内容不能为空");
                result.put("data", null);
                return result;
            }

            String reply = aiService.chat(message);

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("对话服务异常", e);
            result.put("code", 500);
            result.put("message", "对话服务异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    private String getRandomQuestion(String dbJobPosition, Long userId) {
        try {
            List<QuestionBank> questions = questionBankRepository.findByJobPosition(dbJobPosition);
            if (questions == null || questions.isEmpty()) {
                log.warn("未找到岗位 {} 的题目，使用默认题目", dbJobPosition);
                return "请介绍一下你的技术背景和项目经验。";
            }

            // 如果有用户ID，尝试动态出题
            if (userId != null && userId > 0) {
                List<String> weaknessTypes = getUserWeaknessTypes(userId, dbJobPosition);
                if (!weaknessTypes.isEmpty()) {
                    // 优先从薄弱类型中选题
                    for (String weakType : weaknessTypes) {
                        List<QuestionBank> weakQuestions = questions.stream()
                                .filter(q -> weakType.equals(getQuestionTypeForRecord(q.getDescription())))
                                .collect(Collectors.toList());
                        if (!weakQuestions.isEmpty()) {
                            Random random = new Random();
                            QuestionBank question = weakQuestions.get(random.nextInt(weakQuestions.size()));
                            log.info("动态出题 - 用户薄弱类型: {}, 题目: {}", weakType, question.getDescription());
                            return question.getDescription();
                        }
                    }
                }
            }

            // 默认随机选择
            Random random = new Random();
            QuestionBank question = questions.get(random.nextInt(questions.size()));
            return question.getDescription();

        } catch (Exception e) {
            log.error("获取随机题目失败", e);
            return "请介绍一下你的技术背景和项目经验。";
        }
    }

    // 保留原有方法兼容性
    private String getRandomQuestion(String dbJobPosition) {
        return getRandomQuestion(dbJobPosition, null);
    }

    private String mapPositionToDb(String position) {
        switch (position) {
            case "frontend": return "Web";
            case "backend": return "Java";
            case "fullstack": return "Java";
            case "data": return "Python";
            case "product": return "Python";
            case "algo": return "Python";
            case "test": return "Test";
            default: return position;
        }
    }

    // ==================== 批量分析接口 ====================

    @PostMapping("/analyze/batch")
    public Map<String, Object> analyzeBatch(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jobPosition = params.get("jobPosition") != null ? params.get("jobPosition").toString() : null;
            String style = params.get("style") != null ? params.get("style").toString() : null;

            List<Map<String, String>> qaList = extractQaList(params);

            if (qaList.isEmpty()) {
                result.put("code", 400);
                result.put("message", "问答列表不能为空");
                result.put("data", null);
                return result;
            }

            if (jobPosition == null || jobPosition.trim().isEmpty()) jobPosition = "通用";
            if (style == null || style.trim().isEmpty()) style = "友好";

            List<Map<String, Object>> evaluations = new ArrayList<>();
            for (Map<String, String> qa : qaList) {
                String question = qa.get("question");
                String answer = qa.get("answer");

                if (question != null && answer != null) {
                    String evaluation = aiService.analyzeAnswer(jobPosition, style, question, answer);

                    Map<String, Object> item = new HashMap<>();
                    item.put("question", question);
                    item.put("answer", answer);
                    item.put("evaluation", evaluation);
                    evaluations.add(item);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("total", evaluations.size());
            data.put("list", evaluations);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("批量分析异常", e);
            result.put("code", 500);
            result.put("message", "批量分析异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    private List<Map<String, String>> extractQaList(Map<String, Object> params) {
        List<Map<String, String>> qaList = new ArrayList<>();
        Object questionsObj = params.get("questions");

        if (questionsObj instanceof List) {
            for (Object item : (List<?>) questionsObj) {
                if (item instanceof Map) {
                    Map<String, String> qa = new HashMap<>();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;

                    Object q = itemMap.get("question");
                    Object a = itemMap.get("answer");

                    if (q != null) qa.put("question", q.toString());
                    if (a != null) qa.put("answer", a.toString());
                    qaList.add(qa);
                }
            }
        }
        return qaList;
    }

    /**
     * 根据问题内容判断问题类型
     */
    private String determineQuestionTypeByQuestion(String question) {
        if (question == null) return "general";
        String lowerQ = question.toLowerCase();
        if (lowerQ.contains("项目") || lowerQ.contains("经历") || lowerQ.contains("做过") || lowerQ.contains("负责")) {
            return "project";
        } else if (lowerQ.contains("场景") || lowerQ.contains("遇到") || lowerQ.contains("如果") || lowerQ.contains("假设")) {
            return "scenario";
        } else if (lowerQ.contains("行为") || lowerQ.contains("态度") || lowerQ.contains("团队") || lowerQ.contains("沟通")) {
            return "behavior";
        } else {
            return "general";
        }
    }

    private void updateUserWeakness(Long userId, String jobPosition, String questionType, double score) {
        try {
            Optional<UserWeakness> existing = userWeaknessRepository.findByUserIdAndJobPositionAndQuestionType(
                    userId, jobPosition, questionType);

            UserWeakness weakness;
            if (existing.isPresent()) {
                weakness = existing.get();
                int newTotal = weakness.getTotalCount() + 1;
                double newAvg = (weakness.getAvgScore() * weakness.getTotalCount() + score) / newTotal;
                weakness.setAvgScore(newAvg);
                weakness.setTotalCount(newTotal);
                if (score < 60) {
                    weakness.setWrongCount(weakness.getWrongCount() + 1);
                }
            } else {
                weakness = new UserWeakness();
                weakness.setUserId(userId);
                weakness.setJobPosition(jobPosition);
                weakness.setQuestionType(questionType);
                weakness.setTotalCount(1);
                weakness.setAvgScore(score);
                weakness.setWrongCount(score < 60 ? 1 : 0);
            }
            userWeaknessRepository.save(weakness);
            log.info("更新用户薄弱点 - 用户: {}, 岗位: {}, 类型: {}, 平均分: {}",
                    userId, jobPosition, questionType, weakness.getAvgScore());
        } catch (Exception e) {
            log.error("更新薄弱点失败: {}", e.getMessage());
        }
    }

    /**
     * 获取用户薄弱点类型（按薄弱程度排序）
     */
    private List<String> getUserWeaknessTypes(Long userId, String jobPosition) {
        List<UserWeakness> weaknesses = userWeaknessRepository.findByUserIdAndJobPositionOrderByWeaknessScoreDesc(
                userId, jobPosition);
        return weaknesses.stream()
                .map(UserWeakness::getQuestionType)
                .collect(Collectors.toList());
    }

    /**
     * 根据问题内容判断题目类型（用于记录）
     */
    private String getQuestionTypeForRecord(String question) {
        if (question == null) return "技术知识";
        String lowerQ = question.toLowerCase();
        if (lowerQ.contains("项目") || lowerQ.contains("经历") || lowerQ.contains("做过") || lowerQ.contains("负责")) {
            return "项目经历";
        } else if (lowerQ.contains("场景") || lowerQ.contains("遇到") || lowerQ.contains("如果") || lowerQ.contains("假设")) {
            return "场景题";
        } else if (lowerQ.contains("行为") || lowerQ.contains("态度") || lowerQ.contains("团队") || lowerQ.contains("沟通")) {
            return "行为题";
        } else {
            return "技术知识";
        }
    }

}