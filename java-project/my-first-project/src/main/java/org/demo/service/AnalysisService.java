package org.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 内容分析服务
 * 提供技术正确性、知识深度、逻辑严谨性等评分
 */
@Slf4j
@Service
public class AnalysisService {

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 技术正确性评分
     * @param question 问题
     * @param answer 用户回答
     * @param jobPosition 岗位（可选）
     * @param referenceAnswer 参考答案（可选）
     * @return 评分结果（含分数和依据）
     */
    public Map<String, Object> analyzeCorrectness(String question, String answer,
                                                  String jobPosition, String referenceAnswer) {
        log.info("开始技术正确性评分 - 问题: {}", question);

        // 构建提示词
        String prompt = buildCorrectnessPrompt(question, answer, jobPosition, referenceAnswer);

        // 调用AI分析
        String aiResponse = aiService.chat(prompt);
        log.debug("AI响应: {}", aiResponse);

        // 解析AI响应
        return parseCorrectnessResponse(aiResponse);
    }

    /**
     * 构建技术正确性评分的提示词
     */
    private String buildCorrectnessPrompt(String question, String answer,
                                          String jobPosition, String referenceAnswer) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的技术面试官，请对以下回答进行技术正确性评分。\n\n");

        if (jobPosition != null && !jobPosition.isEmpty()) {
            prompt.append("岗位：").append(jobPosition).append("\n");
        }

        prompt.append("问题：").append(question).append("\n");
        prompt.append("回答：").append(answer).append("\n");

        if (referenceAnswer != null && !referenceAnswer.isEmpty()) {
            prompt.append("参考答案：").append(referenceAnswer).append("\n");
        }

        prompt.append("\n请从技术准确性的角度评分，考虑以下方面：\n");
        prompt.append("1. 核心概念是否正确\n");
        prompt.append("2. 技术细节是否准确\n");
        prompt.append("3. 是否存在技术错误\n");
        prompt.append("4. 是否遗漏关键点\n\n");

        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 85,  // 0-100分\n");
        prompt.append("  \"level\": \"优秀/良好/中等/较差\",  // 根据分数自动判断\n");
        prompt.append("  \"correctPoints\": [\"正确点1\", \"正确点2\"],\n");
        prompt.append("  \"errorPoints\": [\"错误点1\", \"错误点2\"],\n");
        prompt.append("  \"missingPoints\": [\"遗漏点1\", \"遗漏点2\"],\n");
        prompt.append("  \"feedback\": \"总体评价和建议\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 解析AI返回的技术正确性评分结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseCorrectnessResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试从响应中提取JSON
            String jsonStr = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取分数
            Number scoreNum = (Number) parsed.getOrDefault("score", 0);
            int score = scoreNum.intValue();

            // 确定等级
            String level = determineLevel(score);

            result.put("score", score);
            result.put("level", level);
            result.put("correctPoints", parsed.getOrDefault("correctPoints", new String[0]));
            result.put("errorPoints", parsed.getOrDefault("errorPoints", new String[0]));
            result.put("missingPoints", parsed.getOrDefault("missingPoints", new String[0]));
            result.put("feedback", parsed.getOrDefault("feedback", ""));

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());

            // 解析失败时返回默认值
            result.put("score", 0);
            result.put("level", "未知");
            result.put("correctPoints", new String[0]);
            result.put("errorPoints", new String[0]);
            result.put("missingPoints", new String[0]);
            result.put("feedback", "评分解析失败，请重试");
        }

        return result;
    }

    /**
     * 从AI响应中提取JSON部分
     */
    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }

        return response;
    }

    /**
     * 根据分数确定等级
     */
    private String determineLevel(int score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "中等";
        return "较差";
    }

    /**
     * 知识深度评分
     * @param question 问题
     * @param answer 用户回答
     * @param jobPosition 岗位（可选）
     * @return 深度评分结果
     */
    public Map<String, Object> analyzeDepth(String question, String answer, String jobPosition) {
        log.info("开始知识深度评分 - 问题: {}", question);

        // 构建提示词
        String prompt = buildDepthPrompt(question, answer, jobPosition);

        // 调用AI分析
        String aiResponse = aiService.chat(prompt);
        log.debug("AI响应: {}", aiResponse);

        // 解析AI响应
        return parseDepthResponse(aiResponse);
    }

    /**
     * 构建知识深度评分的提示词
     */
    private String buildDepthPrompt(String question, String answer, String jobPosition) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的技术面试官，请对以下回答进行知识深度评分。\n\n");
        prompt.append("知识深度考察的是回答是否涉及底层原理、是否举例说明、是否有深入的理解。\n\n");

        if (jobPosition != null && !jobPosition.isEmpty()) {
            prompt.append("岗位：").append(jobPosition).append("\n");
        }

        prompt.append("问题：").append(question).append("\n");
        prompt.append("回答：").append(answer).append("\n\n");

        prompt.append("请从以下维度评分（每项0-100分）：\n");
        prompt.append("1. 原理深度：是否解释了背后的原理、机制\n");
        prompt.append("2. 举例说明：是否给出了具体的例子\n");
        prompt.append("3. 细节把握：是否提到了关键细节\n");
        prompt.append("4. 扩展理解：是否有自己的理解和延伸\n\n");

        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"totalScore\": 85,  // 总分0-100\n");
        prompt.append("  \"level\": \"深入/中等/浅显\",\n");
        prompt.append("  \"dimensions\": {\n");
        prompt.append("    \"principleDepth\": 90,  // 原理深度\n");
        prompt.append("    \"exampleQuality\": 85,  // 举例说明\n");
        prompt.append("    \"detailMastery\": 80,   // 细节把握\n");
        prompt.append("    \"extensionAbility\": 70  // 扩展理解\n");
        prompt.append("  },\n");
        prompt.append("  \"principles\": [\"涉及的原理1\", \"涉及的原理2\"],\n");
        prompt.append("  \"examples\": [\"举例1\", \"举例2\"],\n");
        prompt.append("  \"suggestions\": [\"建议1\", \"建议2\"],\n");
        prompt.append("  \"feedback\": \"总体评价和改进建议\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 解析AI返回的知识深度评分结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDepthResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jsonStr = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取总分
            Number totalScoreNum = (Number) parsed.getOrDefault("totalScore", 0);
            int totalScore = totalScoreNum.intValue();

            // 确定深度等级
            String level = determineDepthLevel(totalScore);

            result.put("totalScore", totalScore);
            result.put("level", level);
            result.put("dimensions", parsed.getOrDefault("dimensions", new HashMap<>()));
            result.put("principles", parsed.getOrDefault("principles", new String[0]));
            result.put("examples", parsed.getOrDefault("examples", new String[0]));
            result.put("suggestions", parsed.getOrDefault("suggestions", new String[0]));
            result.put("feedback", parsed.getOrDefault("feedback", ""));

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());

            result.put("totalScore", 0);
            result.put("level", "未知");
            result.put("dimensions", new HashMap<>());
            result.put("principles", new String[0]);
            result.put("examples", new String[0]);
            result.put("suggestions", new String[0]);
            result.put("feedback", "深度评分解析失败，请重试");
        }

        return result;
    }

    /**
     * 根据深度分数确定等级
     */
    private String determineDepthLevel(int score) {
        if (score >= 80) return "深入";
        if (score >= 60) return "中等";
        return "浅显";
    }

    /**
     * 逻辑严谨性评分
     * @param question 问题
     * @param answer 用户回答
     * @param jobPosition 岗位（可选）
     * @param questionType 问题类型（project/behavior/scenario）
     * @return 逻辑评分结果
     */
    public Map<String, Object> analyzeLogic(String question, String answer,
                                            String jobPosition, String questionType) {
        log.info("开始逻辑严谨性评分 - 问题: {}, 类型: {}", question, questionType);

        // 构建提示词
        String prompt = buildLogicPrompt(question, answer, jobPosition, questionType);

        // 调用AI分析
        String aiResponse = aiService.chat(prompt);
        log.debug("AI响应: {}", aiResponse);

        // 解析AI响应
        return parseLogicResponse(aiResponse);
    }

    /**
     * 构建逻辑严谨性评分的提示词
     */
    private String buildLogicPrompt(String question, String answer,
                                    String jobPosition, String questionType) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的面试官，请对以下回答进行逻辑严谨性评分。\n\n");

        if ("project".equals(questionType) || "behavior".equals(questionType)) {
            prompt.append("请使用STAR法则（Situation-情境, Task-任务, Action-行动, Result-结果）来评估回答的逻辑性。\n\n");
        }

        if (jobPosition != null && !jobPosition.isEmpty()) {
            prompt.append("岗位：").append(jobPosition).append("\n");
        }

        prompt.append("问题类型：").append(getQuestionTypeName(questionType)).append("\n");
        prompt.append("问题：").append(question).append("\n");
        prompt.append("回答：").append(answer).append("\n\n");

        prompt.append("请从以下维度评分（每项0-100分）：\n");
        prompt.append("1. 结构清晰度：回答是否有清晰的开头、主体和结尾\n");
        prompt.append("2. 因果关系：是否清楚地解释了原因和结果\n");
        prompt.append("3. 时间顺序：描述是否按合理的时间顺序展开\n");
        prompt.append("4. 重点突出：是否突出了关键信息\n");

        if ("project".equals(questionType) || "behavior".equals(questionType)) {
            prompt.append("5. STAR完整性：是否完整包含了情境、任务、行动、结果四个要素\n");
        }

        prompt.append("\n请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"totalScore\": 85,  // 总分0-100\n");
        prompt.append("  \"level\": \"优秀/良好/中等/较差\",\n");
        prompt.append("  \"dimensions\": {\n");
        prompt.append("    \"structure\": 90,     // 结构清晰度\n");
        prompt.append("    \"causality\": 85,     // 因果关系\n");
        prompt.append("    \"timeOrder\": 88,     // 时间顺序\n");
        prompt.append("    \"focus\": 80,         // 重点突出\n");
        if ("project".equals(questionType) || "behavior".equals(questionType)) {
            prompt.append("    \"starCompleteness\": 75  // STAR完整性\n");
        }
        prompt.append("  },\n");

        if ("project".equals(questionType) || "behavior".equals(questionType)) {
            prompt.append("  \"starAnalysis\": {\n");
            prompt.append("    \"situation\": \"描述的情境是否清晰\",\n");
            prompt.append("    \"task\": \"描述的任务是否明确\",\n");
            prompt.append("    \"action\": \"采取的行动是否具体\",\n");
            prompt.append("    \"result\": \"取得的结果是否量化\"\n");
            prompt.append("  },\n");
        }

        prompt.append("  \"strengths\": [\"优点1\", \"优点2\"],\n");
        prompt.append("  \"weaknesses\": [\"不足1\", \"不足2\"],\n");
        prompt.append("  \"suggestions\": [\"建议1\", \"建议2\"],\n");
        prompt.append("  \"feedback\": \"总体评价和改进建议\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 获取问题类型名称
     */
    private String getQuestionTypeName(String questionType) {
        if (questionType == null) return "通用";
        switch (questionType) {
            case "project": return "项目经历";
            case "behavior": return "行为面试";
            case "scenario": return "场景题";
            default: return "通用";
        }
    }

    /**
     * 解析AI返回的逻辑严谨性评分结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseLogicResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jsonStr = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取总分
            Number totalScoreNum = (Number) parsed.getOrDefault("totalScore", 0);
            int totalScore = totalScoreNum.intValue();

            // 确定等级
            String level = determineLogicLevel(totalScore);

            result.put("totalScore", totalScore);
            result.put("level", level);
            result.put("dimensions", parsed.getOrDefault("dimensions", new HashMap<>()));

            if (parsed.containsKey("starAnalysis")) {
                result.put("starAnalysis", parsed.get("starAnalysis"));
            }

            result.put("strengths", parsed.getOrDefault("strengths", new String[0]));
            result.put("weaknesses", parsed.getOrDefault("weaknesses", new String[0]));
            result.put("suggestions", parsed.getOrDefault("suggestions", new String[0]));
            result.put("feedback", parsed.getOrDefault("feedback", ""));

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());

            result.put("totalScore", 0);
            result.put("level", "未知");
            result.put("dimensions", new HashMap<>());
            result.put("strengths", new String[0]);
            result.put("weaknesses", new String[0]);
            result.put("suggestions", new String[0]);
            result.put("feedback", "逻辑评分解析失败，请重试");
        }

        return result;
    }

    /**
     * 根据逻辑分数确定等级
     */
    private String determineLogicLevel(int score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 60) return "中等";
        return "较差";
    }

    /**
     * 岗位匹配度分析
     * @param resume 简历内容
     * @param jobDescription 岗位描述
     * @param jobPosition 岗位名称
     * @param candidateName 候选人姓名（可选）
     * @return 匹配度分析结果
     */
    public Map<String, Object> analyzeMatch(String resume, String jobDescription,
                                            String jobPosition, String candidateName) {
        log.info("开始岗位匹配度分析 - 岗位: {}", jobPosition);

        // 构建提示词
        String prompt = buildMatchPrompt(resume, jobDescription, jobPosition, candidateName);

        // 调用AI分析
        String aiResponse = aiService.chat(prompt);
        log.debug("AI响应: {}", aiResponse);

        // 解析AI响应
        return parseMatchResponse(aiResponse);
    }

    /**
     * 构建岗位匹配度分析的提示词
     */
    private String buildMatchPrompt(String resume, String jobDescription,
                                    String jobPosition, String candidateName) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的HR和技术面试官，请对以下简历与岗位描述进行匹配度分析。\n\n");

        if (candidateName != null && !candidateName.isEmpty()) {
            prompt.append("候选人：").append(candidateName).append("\n");
        }

        prompt.append("岗位名称：").append(jobPosition != null ? jobPosition : "未知").append("\n\n");

        prompt.append("【岗位描述】\n").append(jobDescription).append("\n\n");
        prompt.append("【候选人简历】\n").append(resume).append("\n\n");

        prompt.append("请从以下维度进行匹配度评分（每项0-100分）：\n");
        prompt.append("1. 工作经验匹配：工作年限、职级等是否匹配\n");
        prompt.append("2. 技术栈匹配：所需技术栈是否掌握\n");
        prompt.append("3. 业务领域匹配：行业经验、业务理解是否匹配\n");
        prompt.append("4. 软技能匹配：沟通能力、团队协作等是否匹配\n");
        prompt.append("5. 学历背景匹配：教育背景是否符合要求\n\n");

        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"totalScore\": 85,  // 总分0-100\n");
        prompt.append("  \"level\": \"高度匹配/基本匹配/部分匹配/不匹配\",\n");
        prompt.append("  \"dimensions\": {\n");
        prompt.append("    \"experience\": 90,      // 工作经验\n");
        prompt.append("    \"techStack\": 85,       // 技术栈\n");
        prompt.append("    \"businessDomain\": 70,  // 业务领域\n");
        prompt.append("    \"softSkills\": 80,      // 软技能\n");
        prompt.append("    \"education\": 95        // 学历背景\n");
        prompt.append("  },\n");
        prompt.append("  \"matchedSkills\": [\"匹配的技能1\", \"匹配的技能2\"],\n");
        prompt.append("  \"missingSkills\": [\"缺失的技能1\", \"缺失的技能2\"],\n");
        prompt.append("  \"strengths\": [\"优势1\", \"优势2\"],\n");
        prompt.append("  \"weaknesses\": [\"不足1\", \"不足2\"],\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"aspect\": \"技术栈\",\n");
        prompt.append("      \"suggestion\": \"建议学习...\",\n");
        prompt.append("      \"priority\": \"高/中/低\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"suitableLevel\": \"非常合适/合适/待定/不合适\",\n");
        prompt.append("  \"summary\": \"总体评价和建议\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }
    /**
     * 解析AI返回的岗位匹配度分析结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMatchResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试从响应中提取JSON
            String jsonStr = extractJson(aiResponse);

            // 如果提取不到JSON，尝试解析文本格式
            if (jsonStr.equals("{}") || !jsonStr.startsWith("{")) {
                return parseMatchTextResponse(aiResponse);
            }

            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取总分
            Number totalScoreNum = (Number) parsed.getOrDefault("totalScore", 0);
            int totalScore = totalScoreNum.intValue();

            // 确定匹配等级
            String level = determineMatchLevel(totalScore);

            result.put("totalScore", totalScore);
            result.put("level", level);
            result.put("dimensions", parsed.getOrDefault("dimensions", new HashMap<>()));

            // 处理数组类型
            result.put("matchedSkills", convertToList(parsed.get("matchedSkills")));
            result.put("missingSkills", convertToList(parsed.get("missingSkills")));
            result.put("strengths", convertToList(parsed.get("strengths")));
            result.put("weaknesses", convertToList(parsed.get("weaknesses")));

            result.put("recommendations", parsed.getOrDefault("recommendations", new ArrayList<>()));
            result.put("suitableLevel", parsed.getOrDefault("suitableLevel", "待定"));
            result.put("summary", parsed.getOrDefault("summary", ""));

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());

            // 尝试解析文本格式
            return parseMatchTextResponse(aiResponse);
        }

        return result;
    }

    /**
     * 解析文本格式的匹配度分析
     */
    private Map<String, Object> parseMatchTextResponse(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 提取总分
            Pattern scorePattern = Pattern.compile("总分(\\d+)");
            Matcher scoreMatcher = scorePattern.matcher(text);
            int totalScore = scoreMatcher.find() ? Integer.parseInt(scoreMatcher.group(1)) : 0;
            result.put("totalScore", totalScore);
            result.put("level", determineMatchLevel(totalScore));

            // 提取维度得分
            Map<String, Double> dimensions = new HashMap<>();
            Pattern dimPattern = Pattern.compile("(工作经验|技术栈|业务领域|软技能|学历背景)(\\d+)");
            Matcher dimMatcher = dimPattern.matcher(text);
            while (dimMatcher.find()) {
                String dimName = dimMatcher.group(1);
                double score = Double.parseDouble(dimMatcher.group(2));
                dimensions.put(getDimKey(dimName), score);
            }
            result.put("dimensions", dimensions);

            // 提取匹配技能
            result.put("matchedSkills", extractList(text, "匹配的技能：(.*)"));

            // 提取缺失技能
            result.put("missingSkills", extractList(text, "缺失的技能：(.*)"));

            // 提取优势
            result.put("strengths", extractList(text, "优势：(.*)"));

            // 提取不足
            result.put("weaknesses", extractList(text, "不足：(.*)"));

            // 提取建议
            List<Map<String, String>> recommendations = new ArrayList<>();
            Pattern recPattern = Pattern.compile("-\\s*(技术栈|管理能力)：\\s*(.*?)，优先级(高|中|低)");
            Matcher recMatcher = recPattern.matcher(text);
            while (recMatcher.find()) {
                Map<String, String> rec = new HashMap<>();
                rec.put("aspect", recMatcher.group(1));
                rec.put("suggestion", recMatcher.group(2));
                rec.put("priority", recMatcher.group(3));
                recommendations.add(rec);
            }
            result.put("recommendations", recommendations);

            // 提取合适度
            Pattern suitablePattern = Pattern.compile("适合程度：([^。]+)");
            Matcher suitableMatcher = suitablePattern.matcher(text);
            result.put("suitableLevel", suitableMatcher.find() ? suitableMatcher.group(1) : "待定");

            // 提取总结
            Pattern summaryPattern = Pattern.compile("总体评价和建议：([^。]*)");
            Matcher summaryMatcher = summaryPattern.matcher(text);
            result.put("summary", summaryMatcher.find() ? summaryMatcher.group(1) : "");

        } catch (Exception e) {
            log.error("解析文本格式失败: {}", e.getMessage());
            result.put("totalScore", 0);
            result.put("level", "未知");
            result.put("dimensions", new HashMap<>());
            result.put("matchedSkills", new ArrayList<>());
            result.put("missingSkills", new ArrayList<>());
            result.put("strengths", new ArrayList<>());
            result.put("weaknesses", new ArrayList<>());
            result.put("recommendations", new ArrayList<>());
            result.put("suitableLevel", "待定");
            result.put("summary", "匹配度分析解析失败，请重试");
        }

        return result;
    }

    /**
     * 提取列表
     */
    private List<String> extractList(String text, String pattern) {
        List<String> list = new ArrayList<>();
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String content = m.group(1);
            String[] items = content.split("[,，、]");
            for (String item : items) {
                list.add(item.trim());
            }
        }
        return list;
    }

    /**
     * 获取维度键名
     */
    private String getDimKey(String dimName) {
        switch (dimName) {
            case "工作经验": return "experience";
            case "技术栈": return "techStack";
            case "业务领域": return "businessDomain";
            case "软技能": return "softSkills";
            case "学历背景": return "education";
            default: return dimName;
        }
    }

    /**
     * 将对象转换为List<String>
     */
    private List<String> convertToList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj == null) {
            return list;
        }
        if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                list.add(item.toString());
            }
        } else if (obj instanceof String[]) {
            for (String s : (String[]) obj) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * 根据匹配分数确定等级
     */
    private String determineMatchLevel(int score) {
        if (score >= 85) return "高度匹配";
        if (score >= 70) return "基本匹配";
        if (score >= 50) return "部分匹配";
        return "不匹配";
    }

    /**
     * 语速分析
     * @param message 用户消息
     * @param duration 回答时长（秒）
     * @param sessionId 会话ID
     * @return 语速分析结果
     */
    public Map<String, Object> analyzeSpeed(String message, int duration, String sessionId) {
        log.info("开始语速分析 - 消息长度: {}字, 用时: {}秒", message.length(), duration);

        // 计算语速（字/分钟）
        int wordCount = message.length();
        double speed = (wordCount / (double) duration) * 60;

        // 分析语速等级
        return determineSpeedLevel(speed, sessionId);
    }

    /**
     * 根据语速值确定等级
     */
    private Map<String, Object> determineSpeedLevel(double speed, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        String level;
        String description;
        String suggestion;

        if (speed > 180) {
            level = "过快";
            description = "语速过快（>180字/分钟），可能显得紧张";
            suggestion = "尝试放慢语速，在关键点适当停顿，让面试官更好地理解你的思路";
        } else if (speed < 100) {
            level = "过慢";
            description = "语速过慢（<100字/分钟），可能显得不自信";
            suggestion = "可以适当加快语速，避免给人犹豫不决的印象，同时保持表达清晰";
        } else {
            level = "适中";
            description = "语速适中（100-180字/分钟），表达清晰自然";
            suggestion = "语速适中，继续保持";
        }

        result.put("speed", Math.round(speed * 10) / 10.0);
        result.put("level", level);
        result.put("description", description);
        result.put("suggestion", suggestion);
        result.put("session_id", sessionId);

        return result;
    }

    /**
     * 批量语速分析（用于历史记录）
     */
    public List<Map<String, Object>> batchAnalyzeSpeed(List<Map<String, Object>> messages) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> msg : messages) {
            try {
                String message = (String) msg.get("message");
                Integer duration = (Integer) msg.get("duration");
                String sessionId = (String) msg.get("session_id");

                if (message != null && duration != null) {
                    Map<String, Object> speedResult = analyzeSpeed(message, duration, sessionId);
                    results.add(speedResult);
                }
            } catch (Exception e) {
                log.error("批量语速分析失败: {}", e.getMessage());
            }
        }

        return results;
    }

    /**
     * 文本情绪分析
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return 情绪分析结果
     */
    public Map<String, Object> analyzeSentiment(String message, String sessionId) {
        log.info("开始情绪分析 - 消息长度: {}", message.length());

        // 构建提示词
        String prompt = buildSentimentPrompt(message);

        // 调用AI分析
        String aiResponse = aiService.chat(prompt);
        log.debug("AI响应: {}", aiResponse);

        // 解析AI响应
        return parseSentimentResponse(aiResponse, sessionId);
    }

    /**
     * 构建情绪分析提示词
     */
    private String buildSentimentPrompt(String message) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的心理分析师，请分析以下文本的情绪倾向。\n\n");
        prompt.append("文本内容：").append(message).append("\n\n");

        prompt.append("请从以下维度分析：\n");
        prompt.append("1. 情绪倾向：积极/中性/消极\n");
        prompt.append("2. 情绪强度：高/中/低\n");
        prompt.append("3. 关键情绪词：找出表达情绪的关键词\n");
        prompt.append("4. 可能的心理状态：自信/紧张/犹豫/兴奋/沮丧/平静等\n\n");

        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"sentiment\": \"积极/中性/消极\",\n");
        prompt.append("  \"intensity\": \"高/中/低\",\n");
        prompt.append("  \"confidence\": 0.95,  // 分析置信度0-1\n");
        prompt.append("  \"keywords\": [\"关键词1\", \"关键词2\"],\n");
        prompt.append("  \"mentalState\": \"自信/紧张/犹豫/兴奋/沮丧/平静等\",\n");
        prompt.append("  \"suggestion\": \"给用户的建议\",\n");
        prompt.append("  \"details\": \"详细分析说明\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 解析情绪分析结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSentimentResponse(String aiResponse, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jsonStr = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取情绪
            String sentiment = (String) parsed.getOrDefault("sentiment", "中性");
            String intensity = (String) parsed.getOrDefault("intensity", "中");
            Double confidence = parsed.get("confidence") != null ?
                    ((Number) parsed.get("confidence")).doubleValue() : 0.8;

            // 处理关键词数组
            Object keywordsObj = parsed.getOrDefault("keywords", new String[0]);
            String[] keywords;
            if (keywordsObj instanceof List) {
                List<?> list = (List<?>) keywordsObj;
                keywords = list.stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
            } else {
                keywords = new String[0];
            }

            String mentalState = (String) parsed.getOrDefault("mentalState", "平静");
            String suggestion = (String) parsed.getOrDefault("suggestion", "");
            String details = (String) parsed.getOrDefault("details", "");

            result.put("sentiment", sentiment);
            result.put("intensity", intensity);
            result.put("confidence", confidence);
            result.put("keywords", keywords);
            result.put("mentalState", mentalState);
            result.put("suggestion", suggestion);
            result.put("details", details);
            result.put("session_id", sessionId);

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());

            result.put("sentiment", "中性");
            result.put("intensity", "中");
            result.put("confidence", 0.5);
            result.put("keywords", new String[0]);
            result.put("mentalState", "未知");
            result.put("suggestion", "情绪分析暂时不可用");
            result.put("details", "分析失败，请重试");
            result.put("session_id", sessionId);
        }

        return result;
    }

    /**
     * 自信度分析（基于关键词）
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return 自信度分析结果
     */
    public Map<String, Object> analyzeConfidence(String message, String sessionId) {
        log.info("开始自信度分析 - 消息长度: {}", message.length());

        // 方法一：基于规则的关键词匹配（简单快速）
        Map<String, Object> ruleResult = analyzeConfidenceByRules(message);

        // 方法二：调用AI进行更深入的分析（可选）
        // Map<String, Object> aiResult = analyzeConfidenceByAI(message);

        ruleResult.put("session_id", sessionId);
        return ruleResult;
    }

    /**
     * 基于规则的关键词匹配自信度分析
     */
    private Map<String, Object> analyzeConfidenceByRules(String message) {
        Map<String, Object> result = new HashMap<>();

        // 将消息转换为小写以便匹配
        String lowerMsg = message.toLowerCase();

        // 定义关键词库
        String[] highConfidenceKeywords = {
                "肯定", "一定", "绝对", "毫无疑问", "我确定", "我保证",
                "当然", "确实", "没错", "正是", "完全正确", "我肯定",
                "clearly", "definitely", "absolutely", "certainly", "of course"
        };

        String[] lowConfidenceKeywords = {
                "可能", "也许", "或许", "大概", "好像", "似乎", "不一定",
                "我不确定", "我不太确定", "我猜", "可能是", "maybe", "perhaps",
                "probably", "not sure", "guess", "不确定", "不太确定", "有点不太确定"
        };

        String[] hesitationWords = {
                "嗯...", "呃...", "这个...", "那个...", "就是...", "怎么说呢",
                "让我想想", "我考虑一下", "uh...", "um...", "well..."
        };

        // 统计关键词出现次数
        int highCount = countKeywords(lowerMsg, highConfidenceKeywords);
        int lowCount = countKeywords(lowerMsg, lowConfidenceKeywords);
        int hesitationCount = countKeywords(lowerMsg, hesitationWords);

        // 计算自信度分数 (0-100)
        int confidenceScore = calculateConfidenceScore(highCount, lowCount, hesitationCount, message.length());

        // 确定自信度等级
        String level;
        String description;
        String suggestion;

        if (confidenceScore >= 80) {
            level = "高";
            description = "表达非常自信，语气肯定，没有犹豫词";
            suggestion = "继续保持自信的态度，同时确保内容准确";
        } else if (confidenceScore >= 60) {
            level = "中等偏高";
            description = "整体自信，偶尔有轻微犹豫";
            suggestion = "可以适当减少不确定的词语，让表达更肯定";
        } else if (confidenceScore >= 40) {
            level = "中等";
            description = "有一定自信，但存在较多不确定表达";
            suggestion = "建议用更肯定的语气，减少'可能'、'也许'等词语";
        } else if (confidenceScore >= 20) {
            level = "中等偏低";
            description = "明显缺乏自信，频繁使用不确定词汇";
            suggestion = "多练习回答，增加对知识的掌握程度，避免犹豫词";
        } else {
            level = "低";
            description = "非常不自信，充满犹豫和不确定";
            suggestion = "建议先巩固基础知识，多进行模拟练习";
        }

        // 找出具体的不确定词
        List<String> foundLowKeywords = findKeywords(message, lowConfidenceKeywords);
        List<String> foundHesitations = findKeywords(message, hesitationWords);

        result.put("confidenceScore", confidenceScore);
        result.put("level", level);
        result.put("description", description);
        result.put("suggestion", suggestion);
        result.put("lowConfidenceWords", foundLowKeywords);
        result.put("hesitationWords", foundHesitations);
        result.put("highConfidenceWords", findKeywords(message, highConfidenceKeywords));

        return result;
    }

    /**
     * 统计关键词出现次数
     */
    private int countKeywords(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 查找文本中包含的关键词
     */
    private List<String> findKeywords(String text, String[] keywords) {
        List<String> found = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                found.add(keyword);
            }
        }
        return found;
    }

    /**
     * 计算自信度分数
     */
    private int calculateConfidenceScore(int highCount, int lowCount, int hesitationCount, int textLength) {
        // 基础分
        int baseScore = 70;

        // 高自信词加分
        baseScore += highCount * 5;

        // 低自信词扣分
        baseScore -= lowCount * 8;

        // 犹豫词扣分
        baseScore -= hesitationCount * 10;

        // 根据文本长度调整（太短的文本可能信息不足）
        if (textLength < 20) {
            baseScore -= 10;
        }

        // 限制在0-100之间
        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * 基于AI的自信度分析（更深入）
     */
    private Map<String, Object> analyzeConfidenceByAI(String message) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的面试教练，请分析以下回答的自信程度。\n\n");
        prompt.append("回答内容：").append(message).append("\n\n");

        prompt.append("请从以下维度分析：\n");
        prompt.append("1. 自信度等级：高/中等偏高/中等/中等偏低/低\n");
        prompt.append("2. 语气特征：肯定/犹豫/不确定/含糊\n");
        prompt.append("3. 关键词分析：找出体现自信或犹豫的词语\n");
        prompt.append("4. 改进建议\n\n");

        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"confidenceScore\": 75,  // 0-100分\n");
        prompt.append("  \"level\": \"中等\",\n");
        prompt.append("  \"description\": \"分析说明\",\n");
        prompt.append("  \"suggestion\": \"改进建议\",\n");
        prompt.append("  \"keywords\": [\"可能\", \"我觉得\"]\n");
        prompt.append("}\n");

        String aiResponse = aiService.chat(prompt.toString());
        return parseConfidenceAIResponse(aiResponse);
    }

    /**
     * 解析AI自信度分析结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfidenceAIResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jsonStr = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            result.put("confidenceScore", parsed.getOrDefault("confidenceScore", 50));
            result.put("level", parsed.getOrDefault("level", "中等"));
            result.put("description", parsed.getOrDefault("description", ""));
            result.put("suggestion", parsed.getOrDefault("suggestion", ""));
            result.put("keywords", parsed.getOrDefault("keywords", new ArrayList<>()));

        } catch (Exception e) {
            log.error("解析AI自信度分析失败: {}", e.getMessage());
            result.put("confidenceScore", 50);
            result.put("level", "未知");
            result.put("description", "分析失败");
            result.put("suggestion", "请重试");
            result.put("keywords", new ArrayList<>());
        }

        return result;
    }

    /**
     * 亮点与不足分析
     * @param qaDetails 问答详情列表
     * @param avgScores 各维度平均分
     * @return 亮点和不足分析结果
     */
    public Map<String, Object> analyzeStrengthsAndWeaknesses(
            List<Map<String, Object>> qaDetails,
            Map<String, Double> avgScores) {

        log.info("开始亮点与不足分析 - 问答数量: {}", qaDetails.size());

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<Map<String, Object>> strengthDetails = new ArrayList<>();
        List<Map<String, Object>> weaknessDetails = new ArrayList<>();

        // 1. 基于各维度平均分分析
        analyzeByAverageScores(avgScores, strengths, weaknesses, strengthDetails, weaknessDetails);

        // 2. 基于具体问答内容分析
        analyzeByQuestionDetails(qaDetails, strengths, weaknesses, strengthDetails, weaknessDetails);

        // 3. 生成改进建议
        List<Map<String, String>> improvementSuggestions = generateImprovementSuggestions(weaknessDetails);

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("strengths", strengths);
        result.put("weaknesses", weaknesses);
        result.put("strengthDetails", strengthDetails);
        result.put("weaknessDetails", weaknessDetails);
        result.put("improvementSuggestions", improvementSuggestions);
        result.put("summary", generateSummary(strengths.size(), weaknesses.size()));

        return result;
    }

    /**
     * 基于平均分分析
     */
    private void analyzeByAverageScores(
            Map<String, Double> avgScores,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        // 技术正确性分析
        analyzeDimension("技术正确性",
                avgScores.get("correctnessAvg"),
                "技术知识扎实，回答准确性高",
                "技术知识不够扎实，存在概念性错误",
                "核心概念理解清晰",
                "对关键概念理解模糊",
                strengths, weaknesses, strengthDetails, weaknessDetails);

        // 知识深度分析
        analyzeDimension("知识深度",
                avgScores.get("depthAvg"),
                "理解深入，能讲解底层原理",
                "理解停留在表面，缺乏深度",
                "能解释技术原理和机制",
                "未涉及底层原理，停留在使用层面",
                strengths, weaknesses, strengthDetails, weaknessDetails);

        // 逻辑严谨性分析
        analyzeDimension("逻辑严谨性",
                avgScores.get("logicAvg"),
                "回答逻辑清晰，善于使用STAR法则",
                "回答缺乏条理，逻辑不够清晰",
                "回答结构完整，因果关系明确",
                "回答跳跃，缺乏连贯性",
                strengths, weaknesses, strengthDetails, weaknessDetails);

        // 语速分析
        analyzeDimension("语速控制",
                avgScores.get("speedAvg"),
                "语速适中，表达流畅",
                "语速过慢/过快，影响表达效果",
                "语速控制得当，停顿自然",
                "语速不均衡，停顿不当",
                strengths, weaknesses, strengthDetails, weaknessDetails);

        // 自信度分析
        analyzeDimension("自信表达",
                avgScores.get("confidenceAvg"),
                "表达自信，语气肯定",
                "表达缺乏自信，使用过多不确定词汇",
                "语气坚定，用词肯定",
                "频繁使用'可能'、'也许'等不确定词",
                strengths, weaknesses, strengthDetails, weaknessDetails);
    }

    /**
     * 分析单个维度
     */
    private void analyzeDimension(
            String dimensionName,
            double score,
            String highScoreDesc,
            String lowScoreDesc,
            String highDetail,
            String lowDetail,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        if (score >= 80) {
            strengths.add(dimensionName + "：" + highScoreDesc);

            Map<String, Object> detail = new HashMap<>();
            detail.put("dimension", dimensionName);
            detail.put("score", score);
            detail.put("description", highDetail);
            detail.put("suggestion", "继续保持");
            strengthDetails.add(detail);

        } else if (score < 60) {
            weaknesses.add(dimensionName + "：" + lowScoreDesc);

            Map<String, Object> detail = new HashMap<>();
            detail.put("dimension", dimensionName);
            detail.put("score", score);
            detail.put("description", lowDetail);
            detail.put("suggestion", getSuggestionByDimension(dimensionName));
            weaknessDetails.add(detail);
        }
    }

    /**
     * 基于问答内容分析
     */
    private void analyzeByQuestionDetails(
            List<Map<String, Object>> qaDetails,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        for (Map<String, Object> qa : qaDetails) {
            // 分析技术正确性中的亮点
            if (qa.containsKey("correctness")) {
                Map<String, Object> correctness = (Map<String, Object>) qa.get("correctness");
                analyzeCorrectnessPoints(qa, correctness, strengths, weaknesses, strengthDetails, weaknessDetails);
            }

            // 分析知识深度中的亮点
            if (qa.containsKey("depth")) {
                Map<String, Object> depth = (Map<String, Object>) qa.get("depth");
                analyzeDepthPoints(qa, depth, strengths, weaknesses, strengthDetails, weaknessDetails);
            }

            // 分析逻辑严谨性中的亮点
            if (qa.containsKey("logic")) {
                Map<String, Object> logic = (Map<String, Object>) qa.get("logic");
                analyzeLogicPoints(qa, logic, strengths, weaknesses, strengthDetails, weaknessDetails);
            }
        }
    }

    /**
     * 分析技术正确性中的要点
     */
    @SuppressWarnings("unchecked")
    private void analyzeCorrectnessPoints(
            Map<String, Object> qa,
            Map<String, Object> correctness,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        String question = (String) qa.get("question");

        // 正确点
        if (correctness.containsKey("correctPoints")) {
            Object points = correctness.get("correctPoints");
            if (points instanceof List && !((List<?>) points).isEmpty()) {
                List<String> correctPoints = (List<String>) points;
                for (String point : correctPoints) {
                    String desc = "问题《" + truncateString(question, 20) + "》中：" + point;
                    strengths.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "技术正确性");
                    detail.put("point", point);
                    strengthDetails.add(detail);
                }
            }
        }

        // 错误点
        if (correctness.containsKey("errorPoints")) {
            Object points = correctness.get("errorPoints");
            if (points instanceof List && !((List<?>) points).isEmpty()) {
                List<String> errorPoints = (List<String>) points;
                for (String point : errorPoints) {
                    String desc = "问题《" + truncateString(question, 20) + "》中：" + point;
                    weaknesses.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "技术正确性");
                    detail.put("point", point);
                    weaknessDetails.add(detail);
                }
            }
        }

        // 遗漏点
        if (correctness.containsKey("missingPoints")) {
            Object points = correctness.get("missingPoints");
            if (points instanceof List && !((List<?>) points).isEmpty()) {
                List<String> missingPoints = (List<String>) points;
                for (String point : missingPoints) {
                    String desc = "问题《" + truncateString(question, 20) + "》中遗漏：" + point;
                    weaknesses.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "技术正确性");
                    detail.put("point", "遗漏：" + point);
                    weaknessDetails.add(detail);
                }
            }
        }
    }

    /**
     * 分析知识深度中的要点
     */
    @SuppressWarnings("unchecked")
    private void analyzeDepthPoints(
            Map<String, Object> qa,
            Map<String, Object> depth,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        String question = (String) qa.get("question");

        // 涉及原理
        if (depth.containsKey("principles")) {
            Object principles = depth.get("principles");
            if (principles instanceof List && !((List<?>) principles).isEmpty()) {
                List<String> principleList = (List<String>) principles;
                String desc = "问题《" + truncateString(question, 20) + "》中解释了" +
                        String.join("、", principleList) + "等原理";
                strengths.add(desc);

                Map<String, Object> detail = new HashMap<>();
                detail.put("question", question);
                detail.put("type", "知识深度");
                detail.put("point", "解释了原理：" + String.join("、", principleList));
                strengthDetails.add(detail);
            }
        }

        // 举例说明
        if (depth.containsKey("examples")) {
            Object examples = depth.get("examples");
            if (examples instanceof List && !((List<?>) examples).isEmpty()) {
                List<String> exampleList = (List<String>) examples;
                String desc = "问题《" + truncateString(question, 20) + "》中通过" +
                        String.join("、", exampleList) + "等例子辅助说明";
                strengths.add(desc);

                Map<String, Object> detail = new HashMap<>();
                detail.put("question", question);
                detail.put("type", "知识深度");
                detail.put("point", "举例说明：" + String.join("、", exampleList));
                strengthDetails.add(detail);
            }
        }

        // 建议
        if (depth.containsKey("suggestions")) {
            Object suggestions = depth.get("suggestions");
            if (suggestions instanceof List && !((List<?>) suggestions).isEmpty()) {
                List<String> suggestionList = (List<String>) suggestions;
                for (String suggestion : suggestionList) {
                    String desc = "问题《" + truncateString(question, 20) + "》可以改进：" + suggestion;
                    weaknesses.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "知识深度");
                    detail.put("point", suggestion);
                    weaknessDetails.add(detail);
                }
            }
        }
    }

    /**
     * 分析逻辑严谨性中的要点
     */
    @SuppressWarnings("unchecked")
    private void analyzeLogicPoints(
            Map<String, Object> qa,
            Map<String, Object> logic,
            List<String> strengths,
            List<String> weaknesses,
            List<Map<String, Object>> strengthDetails,
            List<Map<String, Object>> weaknessDetails) {

        String question = (String) qa.get("question");

        // STAR分析
        if (logic.containsKey("starAnalysis")) {
            Map<String, Object> starAnalysis = (Map<String, Object>) logic.get("starAnalysis");

            for (Map.Entry<String, Object> entry : starAnalysis.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();

                if (value.contains("清晰") || value.contains("明确") || value.contains("具体") || value.contains("量化")) {
                    String desc = "问题《" + truncateString(question, 20) + "》中" +
                            getStarChineseName(key) + "描述" + value;
                    strengths.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "逻辑严谨性");
                    detail.put("point", getStarChineseName(key) + "描述" + value);
                    strengthDetails.add(detail);
                } else if (value.contains("可以更") || value.contains("略显")) {
                    String desc = "问题《" + truncateString(question, 20) + "》中" +
                            getStarChineseName(key) + "描述" + value;
                    weaknesses.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "逻辑严谨性");
                    detail.put("point", getStarChineseName(key) + "描述" + value);
                    weaknessDetails.add(detail);
                }
            }
        }

        // 优点
        if (logic.containsKey("strengths")) {
            Object strengthsList = logic.get("strengths");
            if (strengthsList instanceof List && !((List<?>) strengthsList).isEmpty()) {
                List<String> strengthList = (List<String>) strengthsList;
                for (String s : strengthList) {
                    String desc = "问题《" + truncateString(question, 20) + "》：" + s;
                    strengths.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "逻辑严谨性");
                    detail.put("point", s);
                    strengthDetails.add(detail);
                }
            }
        }

        // 不足
        if (logic.containsKey("weaknesses")) {
            Object weaknessesList = logic.get("weaknesses");
            if (weaknessesList instanceof List && !((List<?>) weaknessesList).isEmpty()) {
                List<String> weaknessList = (List<String>) weaknessesList;
                for (String w : weaknessList) {
                    String desc = "问题《" + truncateString(question, 20) + "》：" + w;
                    weaknesses.add(desc);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("question", question);
                    detail.put("type", "逻辑严谨性");
                    detail.put("point", w);
                    weaknessDetails.add(detail);
                }
            }
        }
    }

    /**
     * 获取STAR中文名称
     */
    private String getStarChineseName(String starKey) {
        switch (starKey) {
            case "situation": return "情境";
            case "task": return "任务";
            case "action": return "行动";
            case "result": return "结果";
            default: return starKey;
        }
    }

    /**
     * 根据维度获取改进建议
     */
    private String getSuggestionByDimension(String dimensionName) {
        switch (dimensionName) {
            case "技术正确性":
                return "建议系统复习核心知识点，多做练习题巩固理解";
            case "知识深度":
                return "建议学习技术原理和底层实现，阅读源码和官方文档";
            case "逻辑严谨性":
                return "建议练习使用STAR法则组织回答，先理清思路再表达";
            case "语速控制":
                return "建议多进行模拟面试练习，注意控制语速在100-180字/分钟";
            case "自信表达":
                return "建议减少使用'可能'、'也许'等不确定词汇，用肯定语气表达";
            default:
                return "建议多参与模拟面试，积累经验";
        }
    }

    /**
     * 生成改进建议
     */
    private List<Map<String, String>> generateImprovementSuggestions(
            List<Map<String, Object>> weaknessDetails) {

        List<Map<String, String>> suggestions = new ArrayList<>();
        Map<String, Integer> dimensionCount = new HashMap<>();

        // 统计各维度问题数量
        for (Map<String, Object> detail : weaknessDetails) {
            String type = (String) detail.get("type");
            dimensionCount.put(type, dimensionCount.getOrDefault(type, 0) + 1);
        }

        // 根据问题数量生成优先级
        for (Map.Entry<String, Integer> entry : dimensionCount.entrySet()) {
            String dimension = entry.getKey();
            int count = entry.getValue();

            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("dimension", dimension);
            suggestion.put("priority", count >= 3 ? "高" : (count >= 2 ? "中" : "低"));
            suggestion.put("suggestion", getSuggestionByDimension(dimension));
            suggestion.put("count", String.valueOf(count));

            suggestions.add(suggestion);
        }

        // 按优先级排序
        suggestions.sort((a, b) -> {
            String priorityA = a.get("priority");
            String priorityB = b.get("priority");
            return getPriorityValue(priorityB) - getPriorityValue(priorityA);
        });

        return suggestions;
    }

    /**
     * 获取优先级数值
     */
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "高": return 3;
            case "中": return 2;
            case "低": return 1;
            default: return 0;
        }
    }

    /**
     * 生成总结
     */
    private String generateSummary(int strengthCount, int weaknessCount) {
        if (strengthCount > weaknessCount * 2) {
            return String.format("整体表现优秀！你展现了%d个突出的优点，仅有%d个可以改进的地方。继续保持！",
                    strengthCount, weaknessCount);
        } else if (strengthCount > weaknessCount) {
            return String.format("表现良好！你有%d个亮点，同时有%d个方面可以进一步提升。针对性练习会更好！",
                    strengthCount, weaknessCount);
        } else if (weaknessCount > strengthCount) {
            return String.format("还有提升空间。你有%d个需要改进的地方，建议重点关注改进建议，多进行练习。",
                    weaknessCount);
        } else {
            return String.format("表现均衡。你有%d个优点和%d个不足，建议在保持优势的同时，针对性改进不足。",
                    strengthCount, weaknessCount);
        }
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 能力缺口分析
     * @param resumeSkills 简历中的技能列表
     * @param jobPosition 岗位类型（backend/frontend/algo/test）
     * @return 缺口分析结果
     */
    public Map<String, Object> analyzeSkillGap(List<String> resumeSkills, String jobPosition) {
        Map<String, Object> result = new HashMap<>();

        // 根据岗位获取要求
        Map<String, List<String>> jobRequirements = getJobRequirementsByPosition(jobPosition);
        List<String> requiredSkills = jobRequirements.get("required");
        List<String> preferredSkills = jobRequirements.get("preferred");

        if (requiredSkills == null || requiredSkills.isEmpty()) {
            result.put("matchScore", 0);
            result.put("matchedRequired", new ArrayList<>());
            result.put("missingRequired", new ArrayList<>());
            result.put("matchedPreferred", new ArrayList<>());
            result.put("suggestions", new ArrayList<>());
            result.put("level", "未知岗位");
            return result;
        }

        // 转换为小写便于匹配
        List<String> lowerResumeSkills = resumeSkills.stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toList());

        // 计算匹配的技能
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedPreferred = new ArrayList<>();

        for (String skill : requiredSkills) {
            boolean found = lowerResumeSkills.stream().anyMatch(s ->
                    s.contains(skill.toLowerCase()) || skill.toLowerCase().contains(s));
            if (found) {
                matchedRequired.add(skill);
            } else {
                missingRequired.add(skill);
            }
        }

        for (String skill : preferredSkills) {
            boolean found = lowerResumeSkills.stream().anyMatch(s ->
                    s.contains(skill.toLowerCase()) || skill.toLowerCase().contains(s));
            if (found) {
                matchedPreferred.add(skill);
            }
        }

        // 计算匹配度分数
        int requiredTotal = requiredSkills.size();
        int requiredMatched = matchedRequired.size();
        double matchScore = requiredTotal > 0 ? (requiredMatched * 100.0 / requiredTotal) : 0;

        // 生成改进建议
        List<Map<String, String>> suggestions = new ArrayList<>();
        for (String missing : missingRequired) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("skill", missing);
            suggestion.put("suggestion", getSuggestionForSkill(missing));
            suggestion.put("priority", "高");
            suggestions.add(suggestion);
        }

        // 如果没有缺失技能，添加提升建议
        if (suggestions.isEmpty() && !preferredSkills.isEmpty()) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("skill", "进阶提升");
            suggestion.put("suggestion", "您已掌握核心技能，建议学习以下加分技能：" + String.join("、", preferredSkills));
            suggestion.put("priority", "中");
            suggestions.add(suggestion);
        }

        // 确定等级
        String level;
        if (matchScore >= 80) {
            level = "优秀";
        } else if (matchScore >= 60) {
            level = "良好";
        } else if (matchScore >= 40) {
            level = "中等";
        } else {
            level = "待提升";
        }

        result.put("matchScore", Math.round(matchScore));
        result.put("matchedRequired", matchedRequired);
        result.put("missingRequired", missingRequired);
        result.put("matchedPreferred", matchedPreferred);
        result.put("suggestions", suggestions);
        result.put("level", level);
        result.put("requiredSkills", requiredSkills);
        result.put("preferredSkills", preferredSkills);

        return result;
    }

    /**
     * 根据岗位获取技能要求
     */
    private Map<String, List<String>> getJobRequirementsByPosition(String jobPosition) {
        Map<String, List<String>> requirements = new HashMap<>();

        switch (jobPosition) {
            case "backend":
                requirements.put("required", Arrays.asList("Java", "Spring Boot", "MySQL", "Git", "REST API"));
                requirements.put("preferred", Arrays.asList("Redis", "消息队列", "微服务", "Docker"));
                break;
            case "frontend":
                requirements.put("required", Arrays.asList("HTML/CSS", "JavaScript", "Vue.js", "Git"));
                requirements.put("preferred", Arrays.asList("React", "TypeScript", "Webpack", "小程序开发"));
                break;
            case "algo":
                requirements.put("required", Arrays.asList("Python", "数据结构", "算法", "机器学习基础"));
                requirements.put("preferred", Arrays.asList("NumPy/Pandas", "PyTorch/TensorFlow", "SQL", "数据可视化"));
                break;
            case "test":
                requirements.put("required", Arrays.asList("测试理论", "测试用例设计", "缺陷管理", "SQL"));
                requirements.put("preferred", Arrays.asList("自动化测试", "性能测试", "接口测试", "JMeter/Selenium"));
                break;
            default:
                requirements.put("required", Arrays.asList("编程基础", "沟通能力", "学习能力"));
                requirements.put("preferred", new ArrayList<>());
        }

        return requirements;
    }

    /**
     * 获取技能学习建议
     */
    private String getSuggestionForSkill(String skill) {
        Map<String, String> suggestions = new HashMap<>();

        // Java 相关
        suggestions.put("Java", "建议学习《Java核心技术》或参加Java进阶课程，多做项目实践。学习资源：Java官方教程 (https://docs.oracle.com/javase/tutorial/)");
        suggestions.put("Spring Boot", "建议学习Spring Boot官方文档，完成一个完整的Web项目。学习资源：Spring官网指南 (https://spring.io/guides)");
        suggestions.put("Spring", "建议学习Spring框架核心概念，理解IOC和AOP。学习资源：Spring官方文档 (https://spring.io/projects/spring-framework)");

        // 数据库相关
        suggestions.put("MySQL", "建议学习SQL优化和索引原理，练习复杂查询编写。学习资源：MySQL官方文档 (https://dev.mysql.com/doc/refman/8.0/en/)");
        suggestions.put("SQL", "建议学习SQL基础语法和高级查询，多练习复杂SQL编写。学习资源：W3Schools SQL教程 (https://www.w3schools.com/sql/)");
        suggestions.put("Redis", "建议学习Redis数据结构和缓存策略，了解缓存穿透/雪崩解决方案。学习资源：Redis官方文档 (https://redis.io/documentation)");

        // 前端相关
        suggestions.put("HTML/CSS", "建议学习Flex/Grid布局，练习页面还原。学习资源：MDN Web文档 (https://developer.mozilla.org/zh-CN/docs/Web)");
        suggestions.put("HTML", "建议学习HTML5语义化标签和表单增强特性。学习资源：MDN HTML指南 (https://developer.mozilla.org/zh-CN/docs/Web/HTML)");
        suggestions.put("CSS", "建议学习Flex/Grid布局和CSS动画。学习资源：CSS-Tricks指南 (https://css-tricks.com/)");
        suggestions.put("JavaScript", "建议学习ES6+语法，理解闭包和异步编程。学习资源：现代JavaScript教程 (https://zh.javascript.info/)");
        suggestions.put("Vue.js", "建议学习Vue3组合式API，完成一个完整的前端项目。学习资源：Vue.js官方文档 (https://cn.vuejs.org/)");
        suggestions.put("React", "建议学习React Hooks和组件化开发。学习资源：React官方文档 (https://react.dev/)");
        suggestions.put("TypeScript", "建议学习TypeScript类型系统和接口定义。学习资源：TypeScript官方文档 (https://www.typescriptlang.org/zh/)");
        suggestions.put("Webpack", "建议学习Webpack核心概念和配置优化。学习资源：Webpack官方文档 (https://webpack.js.org/)");

        // 后端框架相关
        suggestions.put("Spring Cloud", "建议学习Spring Cloud微服务架构设计。学习资源：Spring Cloud官方文档 (https://spring.io/projects/spring-cloud)");
        suggestions.put("MyBatis", "建议学习MyBatis映射配置和动态SQL。学习资源：MyBatis官方文档 (https://mybatis.org/mybatis-3/zh/index.html)");
        suggestions.put("Hibernate", "建议学习JPA规范和Hibernate ORM原理。学习资源：Hibernate官方文档 (https://hibernate.org/)");

        // 中间件相关
        suggestions.put("消息队列", "建议学习RabbitMQ/Kafka的基本使用和常见场景。学习资源：RabbitMQ教程 (https://www.rabbitmq.com/getstarted.html)");
        suggestions.put("Kafka", "建议学习Kafka架构和消息处理机制。学习资源：Apache Kafka文档 (https://kafka.apache.org/documentation/)");
        suggestions.put("Docker", "建议学习Docker基本命令和容器化部署。学习资源：Docker官方文档 (https://docs.docker.com/)");
        suggestions.put("Kubernetes", "建议学习K8s核心概念和容器编排。学习资源：Kubernetes官方文档 (https://kubernetes.io/zh-cn/docs/home/)");

        // 工具相关
        suggestions.put("Git", "建议学习Git常用命令和分支管理策略。学习资源：Git官方文档 (https://git-scm.com/book/zh/v2)");
        suggestions.put("Maven", "建议学习Maven依赖管理和生命周期。学习资源：Maven官方文档 (https://maven.apache.org/guides/)");
        suggestions.put("Gradle", "建议学习Gradle构建脚本和依赖配置。学习资源：Gradle官方文档 (https://docs.gradle.org/)");

        // Python 相关
        suggestions.put("Python", "建议学习Python高级特性，练习算法题。学习资源：Python官方教程 (https://docs.python.org/zh-cn/3/tutorial/)");
        suggestions.put("NumPy/Pandas", "建议学习NumPy数组运算和Pandas数据处理。学习资源：Pandas官方文档 (https://pandas.pydata.org/docs/)");
        suggestions.put("PyTorch/TensorFlow", "建议学习深度学习框架基础，完成一个简单模型。学习资源：PyTorch教程 (https://pytorch.org/tutorials/)");

        // 算法相关
        suggestions.put("数据结构", "建议系统学习数组、链表、树、图等数据结构。学习资源：LeetCode学习计划 (https://leetcode.cn/study-plan/)");
        suggestions.put("算法", "建议刷LeetCode，掌握常见算法题型。学习资源：算法导论书籍或在线课程");
        suggestions.put("机器学习基础", "建议学习吴恩达机器学习课程，理解基本算法原理。学习资源：Coursera机器学习课程 (https://www.coursera.org/learn/machine-learning)");

        // 测试相关
        suggestions.put("测试理论", "建议学习ISTQB基础知识，理解测试流程。学习资源：ISTQB认证官网 (https://www.istqb.org/)");
        suggestions.put("测试用例设计", "建议学习等价类、边界值、场景法等设计方法。学习资源：软件测试艺术书籍");
        suggestions.put("缺陷管理", "建议学习缺陷生命周期和JIRA等工具使用。学习资源：Atlassian教程 (https://www.atlassian.com/software/jira/guides)");
        suggestions.put("自动化测试", "建议学习Selenium或Appium自动化框架。学习资源：Selenium官方文档 (https://www.selenium.dev/documentation/)");
        suggestions.put("性能测试", "建议学习JMeter或LoadRunner性能测试工具。学习资源：JMeter官方文档 (https://jmeter.apache.org/)");
        suggestions.put("接口测试", "建议学习Postman或REST Assured接口测试。学习资源：Postman学习中心 (https://learning.postman.com/)");

        // REST API
        suggestions.put("REST API", "建议学习RESTful API设计规范，了解HTTP协议。学习资源：RESTful API设计指南 (https://restfulapi.net/)");

        // 通用
        suggestions.put("编程基础", "建议系统学习编程语言基础，多做练习。学习资源：菜鸟教程 (https://www.runoob.com/)");
        suggestions.put("沟通能力", "建议多参与团队讨论和项目汇报。学习资源：得到沟通训练营或相关书籍");
        suggestions.put("学习能力", "建议保持技术敏感度，定期阅读技术博客和文档。学习资源：掘金、InfoQ等技术社区");

        return suggestions.getOrDefault(skill, "建议通过官方文档和实战项目学习该技能。学习资源：菜鸟教程 (https://www.runoob.com/)");
    }

}
