package org.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.dao.AnalysisResultRepository;
import org.demo.dao.HistoryRepository;
import org.demo.entity.AnalysisResult;
import org.demo.entity.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告生成服务
 * 整合各维度评分，生成综合报告
 */
@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成综合报告
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param title 报告标题
     * @return 结构化报告
     */
    @Transactional
    public Map<String, Object> generateReport(String sessionId, Long userId, String title) {
        log.info("开始生成报告 - sessionId: {}, userId: {}", sessionId, userId);

        // 1. 获取该会话的所有历史记录
        List<History> histories = historyRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (histories.isEmpty()) {
            throw new IllegalArgumentException("该会话没有历史记录: " + sessionId);
        }

        // 2. 获取岗位信息
        String jobPosition = histories.get(0).getJobPosition();
        String style = histories.get(0).getStyle();

        // 3. 对每条记录进行详细分析
        List<Map<String, Object>> qaDetails = analyzeHistories(histories, jobPosition, style);

        // 4. 计算各维度平均分
        Map<String, Double> avgScores = calculateAverageScores(qaDetails);

        // 5. 生成亮点和不足分析
        Map<String, Object> analysis = generateStrengthsAndWeaknesses(qaDetails, avgScores);

        // 6. 生成改进建议
        List<Map<String, String>> improvements = generateImprovements(analysis, avgScores);

        // 7. 生成总结评语
        String summary = generateSummary(avgScores, analysis);

        // 7.1 生成亮点和不足分析（增强版）
        Map<String, Object> strengthsWeaknesses = analysisService.analyzeStrengthsAndWeaknesses(qaDetails, avgScores);

        // 8. 构建完整报告
        Map<String, Object> report = buildReport(
                sessionId, userId, title, jobPosition, style,
                histories, qaDetails, avgScores, analysis, improvements, summary, strengthsWeaknesses);

        // 9. 保存报告到数据库
        saveReport(userId, sessionId, title, jobPosition, style, report, avgScores, histories);

        return report;
    }

    /**
     * 分析每条历史记录
     */
    private List<Map<String, Object>> analyzeHistories(List<History> histories,
                                                       String jobPosition, String style) {
        List<Map<String, Object>> qaDetails = new ArrayList<>();

        for (History history : histories) {
            if (history.getQuestion() == null || history.getAnswer() == null) {
                continue;
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("id", history.getId());
            detail.put("question", history.getQuestion());
            detail.put("answer", history.getAnswer());
            detail.put("createdAt", formatDateTime(history.getCreatedAt()));

            // 调用各分析接口获取详细评分
            try {
                // 技术正确性分析
                Map<String, Object> correctness = analysisService.analyzeCorrectness(
                        history.getQuestion(), history.getAnswer(), jobPosition, null);
                detail.put("correctness", correctness);

                // 知识深度分析
                Map<String, Object> depth = analysisService.analyzeDepth(
                        history.getQuestion(), history.getAnswer(), jobPosition);
                detail.put("depth", depth);

                // 逻辑严谨性分析
                String questionType = determineQuestionType(history.getQuestionType());
                Map<String, Object> logic = analysisService.analyzeLogic(
                        history.getQuestion(), history.getAnswer(), jobPosition, questionType);
                detail.put("logic", logic);

                // 语速分析
                if (history.getAnswerDuration() != null && history.getAnswerDuration() > 0) {
                    Map<String, Object> speed = analysisService.analyzeSpeed(
                            history.getAnswer(), history.getAnswerDuration(), history.getSessionId());
                    detail.put("speed", speed);
                }

                // 自信度分析
                Map<String, Object> confidence = analysisService.analyzeConfidence(
                        history.getAnswer(), history.getSessionId());
                detail.put("confidence", confidence);

                // 情绪分析
                Map<String, Object> sentiment = analysisService.analyzeSentiment(
                        history.getAnswer(), history.getSessionId());
                detail.put("sentiment", sentiment);

            } catch (Exception e) {
                log.error("分析历史记录失败: {}", e.getMessage());
            }

            qaDetails.add(detail);
        }

        return qaDetails;
    }

    /**
     * 确定问题类型
     */
    private String determineQuestionType(String questionType) {
        if (questionType == null) return "general";
        switch (questionType) {
            case "技术知识": return "technical";
            case "项目经历": return "project";
            case "场景题": return "scenario";
            case "行为题": return "behavior";
            default: return "general";
        }
    }

    /**
     * 计算各维度平均分
     */
    private Map<String, Double> calculateAverageScores(List<Map<String, Object>> qaDetails) {
        double correctnessSum = 0;
        double depthSum = 0;
        double logicSum = 0;
        double speedSum = 0;
        double confidenceSum = 0;

        int correctnessCount = 0;
        int depthCount = 0;
        int logicCount = 0;
        int speedCount = 0;
        int confidenceCount = 0;

        for (Map<String, Object> qa : qaDetails) {
            // 技术正确性
            if (qa.containsKey("correctness")) {
                Map<String, Object> correctness = (Map<String, Object>) qa.get("correctness");
                if (correctness.containsKey("score")) {
                    correctnessSum += ((Number) correctness.get("score")).doubleValue();
                    correctnessCount++;
                }
            }

            // 知识深度
            if (qa.containsKey("depth")) {
                Map<String, Object> depth = (Map<String, Object>) qa.get("depth");
                if (depth.containsKey("totalScore")) {
                    depthSum += ((Number) depth.get("totalScore")).doubleValue();
                    depthCount++;
                }
            }

            // 逻辑严谨性
            if (qa.containsKey("logic")) {
                Map<String, Object> logic = (Map<String, Object>) qa.get("logic");
                if (logic.containsKey("totalScore")) {
                    logicSum += ((Number) logic.get("totalScore")).doubleValue();
                    logicCount++;
                }
            }

            // 语速
            if (qa.containsKey("speed")) {
                Map<String, Object> speed = (Map<String, Object>) qa.get("speed");
                // 将语速等级转换为分数
                String level = (String) speed.get("level");
                double speedScore = convertSpeedLevelToScore(level);
                speedSum += speedScore;
                speedCount++;
            }

            // 自信度
            if (qa.containsKey("confidence")) {
                Map<String, Object> confidence = (Map<String, Object>) qa.get("confidence");
                if (confidence.containsKey("confidenceScore")) {
                    confidenceSum += ((Number) confidence.get("confidenceScore")).doubleValue();
                    confidenceCount++;
                }
            }
        }

        Map<String, Double> avgScores = new HashMap<>();
        avgScores.put("correctnessAvg", correctnessCount > 0 ?
                Math.round((correctnessSum / correctnessCount) * 100) / 100.0 : 0);
        avgScores.put("depthAvg", depthCount > 0 ?
                Math.round((depthSum / depthCount) * 100) / 100.0 : 0);
        avgScores.put("logicAvg", logicCount > 0 ?
                Math.round((logicSum / logicCount) * 100) / 100.0 : 0);
        avgScores.put("speedAvg", speedCount > 0 ?
                Math.round((speedSum / speedCount) * 100) / 100.0 : 0);
        avgScores.put("confidenceAvg", confidenceCount > 0 ?
                Math.round((confidenceSum / confidenceCount) * 100) / 100.0 : 0);

        return avgScores;
    }

    /**
     * 将语速等级转换为分数
     */
    private double convertSpeedLevelToScore(String level) {
        switch (level) {
            case "适中": return 90;
            case "过快": return 70;
            case "过慢": return 60;
            default: return 75;
        }
    }

    /**
     * 生成亮点和不足分析
     */
    private Map<String, Object> generateStrengthsAndWeaknesses(
            List<Map<String, Object>> qaDetails, Map<String, Double> avgScores) {

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        // 基于平均分判断
        if (avgScores.get("correctnessAvg") >= 80) {
            strengths.add("技术基础知识扎实，回答准确性高");
        } else if (avgScores.get("correctnessAvg") < 60) {
            weaknesses.add("技术基础知识不够扎实，存在较多错误");
        }

        if (avgScores.get("depthAvg") >= 80) {
            strengths.add("知识理解深入，能讲解底层原理");
        } else if (avgScores.get("depthAvg") < 60) {
            weaknesses.add("知识理解停留在表面，缺乏深度");
        }

        if (avgScores.get("logicAvg") >= 80) {
            strengths.add("回答逻辑清晰，善于使用STAR法则");
        } else if (avgScores.get("logicAvg") < 60) {
            weaknesses.add("回答缺乏条理，逻辑不够清晰");
        }

        if (avgScores.get("speedAvg") >= 80) {
            strengths.add("语速适中，表达流畅");
        } else if (avgScores.get("speedAvg") < 60) {
            weaknesses.add("语速过慢/过快，影响表达效果");
        }

        if (avgScores.get("confidenceAvg") >= 80) {
            strengths.add("表达自信，语气肯定");
        } else if (avgScores.get("confidenceAvg") < 60) {
            weaknesses.add("表达缺乏自信，使用过多不确定词汇");
        }

        // 基于具体回答分析
        for (Map<String, Object> qa : qaDetails) {
            if (qa.containsKey("correctness")) {
                Map<String, Object> correctness = (Map<String, Object>) qa.get("correctness");
                if (correctness.containsKey("correctPoints")) {
                    Object points = correctness.get("correctPoints");
                    if (points instanceof List && !((List<?>) points).isEmpty()) {
                        strengths.add("回答中体现了对核心概念的理解");
                    }
                }
            }

            if (qa.containsKey("depth")) {
                Map<String, Object> depth = (Map<String, Object>) qa.get("depth");
                if (depth.containsKey("principles")) {
                    Object principles = depth.get("principles");
                    if (principles instanceof List && !((List<?>) principles).isEmpty()) {
                        strengths.add("能解释技术背后的原理");
                    }
                }
                if (depth.containsKey("examples")) {
                    Object examples = depth.get("examples");
                    if (examples instanceof List && !((List<?>) examples).isEmpty()) {
                        strengths.add("善于用例子说明问题");
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("strengths", strengths.stream().distinct().collect(Collectors.toList()));
        result.put("weaknesses", weaknesses.stream().distinct().collect(Collectors.toList()));

        return result;
    }

    /**
     * 生成改进建议
     */
    private List<Map<String, String>> generateImprovements(
            Map<String, Object> analysis, Map<String, Double> avgScores) {

        List<Map<String, String>> improvements = new ArrayList<>();

        // 基于弱点生成建议
        @SuppressWarnings("unchecked")
        List<String> weaknesses = (List<String>) analysis.get("weaknesses");

        for (String weakness : weaknesses) {
            Map<String, String> item = new HashMap<>();
            item.put("priority", determinePriority(weakness));

            if (weakness.contains("技术基础")) {
                item.put("aspect", "技术知识");
                item.put("suggestion", "建议系统复习核心知识点，多做练习题巩固理解");
            } else if (weakness.contains("深度")) {
                item.put("aspect", "知识深度");
                item.put("suggestion", "学习技术原理和底层实现，阅读源码和官方文档");
            } else if (weakness.contains("逻辑")) {
                item.put("aspect", "逻辑表达");
                item.put("suggestion", "练习使用STAR法则组织回答，先理清思路再表达");
            } else if (weakness.contains("语速")) {
                item.put("aspect", "语速控制");
                item.put("suggestion", "多进行模拟面试练习，注意控制语速在100-180字/分钟");
            } else if (weakness.contains("自信")) {
                item.put("aspect", "自信表达");
                item.put("suggestion", "减少使用'可能'、'也许'等不确定词汇，用肯定语气表达");
            } else {
                item.put("aspect", "综合能力");
                item.put("suggestion", "多参与模拟面试，积累经验");
            }

            improvements.add(item);
        }

        // 基于低分维度生成建议
        if (avgScores.get("correctnessAvg") < 70) {
            improvements.add(createSuggestion(
                    "技术知识", "高", "建议重点复习核心知识点，可参考技术文档和经典教材"));
        }
        if (avgScores.get("depthAvg") < 70) {
            improvements.add(createSuggestion(
                    "知识深度", "高", "深入理解技术原理，阅读源码和设计文档"));
        }
        if (avgScores.get("logicAvg") < 70) {
            improvements.add(createSuggestion(
                    "逻辑表达", "中", "练习使用STAR法则，先列提纲再回答"));
        }
        if (avgScores.get("confidenceAvg") < 70) {
            improvements.add(createSuggestion(
                    "自信表达", "中", "多进行模拟练习，减少不确定词汇的使用"));
        }

        return improvements;
    }

    /**
     * 创建建议项
     */
    private Map<String, String> createSuggestion(String aspect, String priority, String suggestion) {
        Map<String, String> item = new HashMap<>();
        item.put("aspect", aspect);
        item.put("priority", priority);
        item.put("suggestion", suggestion);
        return item;
    }

    /**
     * 确定优先级
     */
    private String determinePriority(String weakness) {
        if (weakness.contains("技术") || weakness.contains("深度")) {
            return "高";
        } else if (weakness.contains("逻辑")) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 生成总结评语
     */
    private String generateSummary(Map<String, Double> avgScores, Map<String, Object> analysis) {
        double totalScore = (avgScores.get("correctnessAvg") +
                avgScores.get("depthAvg") +
                avgScores.get("logicAvg") +
                avgScores.get("confidenceAvg")) / 4;

        String level;
        if (totalScore >= 85) level = "优秀";
        else if (totalScore >= 75) level = "良好";
        else if (totalScore >= 60) level = "中等";
        else level = "待提升";

        @SuppressWarnings("unchecked")
        int strengthCount = ((List<String>) analysis.get("strengths")).size();
        @SuppressWarnings("unchecked")
        int weaknessCount = ((List<String>) analysis.get("weaknesses")).size();

        return String.format(
                "本次面试综合表现%s，总分%.1f分。整体来看，你展现出%d个突出的优点，同时在%d个方面还有提升空间。%s",
                level, totalScore, strengthCount, weaknessCount,
                weaknessCount > 0 ? "建议重点关注改进建议，针对性练习。" : "继续保持，争取更好表现。"
        );
    }

    /**
     * 构建完整报告
     */
    private Map<String, Object> buildReport(
            String sessionId, Long userId, String title, String jobPosition, String style,
            List<History> histories, List<Map<String, Object>> qaDetails,
            Map<String, Double> avgScores, Map<String, Object> analysis,
            List<Map<String, String>> improvements, String summary,
            Map<String, Object> strengthsWeaknesses) {  // 新增参数
        Map<String, Object> report = new HashMap<>();

        // 报告基本信息
        report.put("report_id", UUID.randomUUID().toString().substring(0, 8));
        report.put("session_id", sessionId);
        report.put("user_id", userId);
        report.put("title", title != null ? title : generateTitle(histories));
        report.put("job_position", jobPosition);
        report.put("style", style);
        report.put("generated_at", formatDateTime(LocalDateTime.now()));
        report.put("question_count", histories.size());

        // 各维度平均分
        Map<String, Object> dimensionScores = new HashMap<>();
        dimensionScores.put("technical_correctness", avgScores.get("correctnessAvg"));
        dimensionScores.put("knowledge_depth", avgScores.get("depthAvg"));
        dimensionScores.put("logical_rigor", avgScores.get("logicAvg"));
        dimensionScores.put("speech_speed", avgScores.get("speedAvg"));
        dimensionScores.put("confidence_level", avgScores.get("confidenceAvg"));
        report.put("dimension_scores", dimensionScores);

        // 综合总分
        double totalScore = (avgScores.get("correctnessAvg") +
                avgScores.get("depthAvg") +
                avgScores.get("logicAvg") +
                avgScores.get("confidenceAvg")) / 4;
        report.put("total_score", Math.round(totalScore * 100) / 100.0);

        // 问答详情
        report.put("questions", qaDetails);

        // 亮点和不足
        report.put("strengths", strengthsWeaknesses.get("strengths"));
        report.put("weaknesses", strengthsWeaknesses.get("weaknesses"));
        report.put("strengthDetails", strengthsWeaknesses.get("strengthDetails"));
        report.put("weaknessDetails", strengthsWeaknesses.get("weaknessDetails"));
        report.put("improvementSuggestions", strengthsWeaknesses.get("improvementSuggestions"));

        // 改进建议
        report.put("improvements", improvements);

        // 总结
        report.put("summary", summary);

        return report;
    }

    /**
     * 生成报告标题
     */
    private String generateTitle(List<History> histories) {
        if (histories.isEmpty()) return "面试报告";
        LocalDateTime firstTime = histories.get(0).getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "面试报告_" + firstTime.format(formatter);
    }

    /**
     * 保存报告到数据库
     */
    private void saveReport(Long userId, String sessionId, String title,
                            String jobPosition, String style,
                            Map<String, Object> report, Map<String, Double> avgScores,
                            List<History> histories) {
        try {
            AnalysisResult result = new AnalysisResult();
            result.setUserId(userId);
            result.setSessionId(sessionId);
            result.setTitle(title != null ? title : generateTitleFromSession(sessionId));
            result.setJobPosition(jobPosition);
            result.setInterviewStyle(style);

            result.setCorrectnessAvg(avgScores.get("correctnessAvg"));
            result.setDepthAvg(avgScores.get("depthAvg"));
            result.setLogicAvg(avgScores.get("logicAvg"));
            result.setSpeedAvg(avgScores.get("speedAvg"));
            result.setConfidenceAvg(avgScores.get("confidenceAvg"));

            result.setTotalScore((Double) report.get("total_score"));

            // 存储JSON数据
            result.setDimensionDetails(objectMapper.writeValueAsString(report.get("dimension_scores")));
            result.setQaList(objectMapper.writeValueAsString(report.get("questions")));
            result.setStrengths(objectMapper.writeValueAsString(report.get("strengths")));
            result.setWeaknesses(objectMapper.writeValueAsString(report.get("weaknesses")));
            result.setImprovements(objectMapper.writeValueAsString(report.get("improvements")));
            result.setSummary((String) report.get("summary"));

            if (histories != null && !histories.isEmpty()) {
                result.setStartTime(histories.get(0).getCreatedAt());
            }
            result.setEndTime(LocalDateTime.now());

            analysisResultRepository.save(result);
            log.info("报告保存成功 - reportId: {}", result.getId());

        } catch (Exception e) {
            log.error("保存报告失败: {}", e.getMessage());
        }
    }

    /**
     * 从会话ID生成标题
     */
    private String generateTitleFromSession(String sessionId) {
        return "面试报告_" + sessionId;
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}