package org.demo.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.demo.util.PromptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class AIService {

    @Value("${aliyun.dashscope.api-key:}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            Constants.apiKey = apiKey;
            log.info("通义千问API Key配置成功");
        } else {
            log.warn("通义千问API Key未配置，请在application.properties中设置aliyun.dashscope.api-key");
        }
    }

    /**
     * 分析面试回答质量
     * 简化版本：只返回点评文本，不再返回action等字段
     * @param jobPosition 岗位类型（如：Java后端）
     * @param style 面试风格（友好/严格/温和）
     * @param question 当前问题
     * @param answer 用户回答
     * @return 点评文本
     */
    public String analyzeAnswer(String jobPosition, String style,
                                String question, String answer) {
        log.info("开始分析回答 - 岗位: {}, 风格: {}, 问题: {}", jobPosition, style, question);
        log.info("用户回答: {}", answer);

        try {
            // 1. 调用PromptUtil填充提示词模板（使用简化版提示词）
            String prompt = buildAnalysisPrompt(jobPosition, style, question, answer);
            log.debug("生成的提示词: {}", prompt);

            // 2. 调用通义千问API
            String aiResponse = callQwen(prompt);
            log.debug("AI原始响应: {}", aiResponse);

            // 3. 解析AI返回的文本（直接返回点评）
            String evaluation = extractEvaluation(aiResponse);

            log.info("分析完成 - 评价: {}", evaluation);
            return evaluation;

        } catch (Exception e) {
            log.error("分析回答时发生异常: {}", e.getMessage(), e);
            return "抱歉，AI分析服务暂时不可用，请稍后重试。";
        }
    }

    /**
     * 构建分析提示词（简化版，只要求返回点评）
     */
    private String buildAnalysisPrompt(String jobPosition, String style,
                                       String question, String answer) {
        return String.format(
                "你是一个专业的%s面试官，风格%s。\n\n" +
                        "问题：%s\n" +
                        "回答：%s\n\n" +
                        "请对上述回答进行专业点评（50-100字），指出优点和不足。只返回点评文本，不要返回JSON或其他格式。",
                jobPosition, style, question, answer
        );
    }

    /**
     * 从AI响应中提取评价文本
     */
    private String extractEvaluation(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "感谢你的回答。";
        }

        // 清理响应文本（去除可能的引号、换行等）
        String evaluation = response.trim();

        // 如果响应包含JSON，尝试提取
        if (evaluation.startsWith("{") && evaluation.endsWith("}")) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(evaluation,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                if (parsed.containsKey("evaluation")) {
                    return parsed.get("evaluation").toString();
                }
            } catch (Exception e) {
                log.debug("响应不是JSON格式，按普通文本处理");
            }
        }

        return evaluation;
    }

    /**
     * 调用通义千问API
     * @param prompt 提示词
     * @return AI响应文本
     */
    private String callQwen(String prompt) throws NoApiKeyException, ApiException, InputRequiredException {
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的AI面试官，请对用户的回答进行点评。只返回点评文本，不要返回JSON格式。")
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(0.7f)     // temperature 需要 Float，用 0.7f
                .topP(0.8)              // topP 需要 Double，用 0.8
                .build();

        Generation generation = new Generation();
        GenerationResult result = generation.call(param);

        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    /**
     * 简单对话
     */
    public String chat(String userInput) {
        try {
            return callQwen(userInput);
        } catch (Exception e) {
            log.error("对话失败", e);
            return "抱歉，AI服务暂时不可用";
        }
    }

    /**
     * 带上下文的对话
     */
    public String chatWithContext(String userInput, String context) {
        try {
            String prompt = String.format("上下文信息：%s\n\n用户问题：%s", context, userInput);
            return callQwen(prompt);
        } catch (Exception e) {
            log.error("对话失败", e);
            return "抱歉，AI服务暂时不可用";
        }
    }

    // ==================== 原有方法保持不变 ====================

    /**
     * 生成文本的embedding向量
     * @param text 输入文本
     * @return 向量列表（Float类型）
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("输入文本为空，返回空向量");
            return new ArrayList<>();
        }

        try {
            log.info("开始生成embedding向量，文本长度: {}", text.length());

            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model("text-embedding-v2")
                    .texts(Collections.singletonList(text))
                    .textType(TextEmbeddingParam.TextType.DOCUMENT)
                    .build();

            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            if (result != null && result.getOutput() != null &&
                    result.getOutput().getEmbeddings() != null &&
                    !result.getOutput().getEmbeddings().isEmpty()) {

                List<Double> doubleEmbedding = result.getOutput().getEmbeddings().get(0).getEmbedding();
                List<Float> floatEmbedding = new ArrayList<>();

                for (Double d : doubleEmbedding) {
                    floatEmbedding.add(d.floatValue());
                }

                log.info("embedding生成成功，向量维度: {}", floatEmbedding.size());
                return floatEmbedding;
            } else {
                log.error("embedding结果为空");
                return new ArrayList<>();
            }

        } catch (NoApiKeyException e) {
            log.error("API Key未配置或无效: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("生成embedding时发生异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 批量生成embedding向量
     */
    public List<List<Float>> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.info("开始批量生成embedding向量，文本数量: {}", texts.size());

            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model("text-embedding-v2")
                    .texts(texts)
                    .textType(TextEmbeddingParam.TextType.DOCUMENT)
                    .build();

            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            List<List<Float>> resultList = new ArrayList<>();

            if (result != null && result.getOutput() != null &&
                    result.getOutput().getEmbeddings() != null) {

                for (var embedding : result.getOutput().getEmbeddings()) {
                    List<Double> doubleEmbedding = embedding.getEmbedding();
                    List<Float> floatEmbedding = new ArrayList<>();

                    for (Double d : doubleEmbedding) {
                        floatEmbedding.add(d.floatValue());
                    }
                    resultList.add(floatEmbedding);
                }
            }

            log.info("批量embedding生成成功，返回数量: {}", resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("批量生成embedding时发生异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 分析面试回答质量（带RAG上下文）
     * @param jobPosition 岗位类型（如：Java后端）
     * @param style 面试风格（友好/严格/温和）
     * @param question 当前问题
     * @param answer 用户回答
     * @param context RAG检索到的知识上下文（可为null或空字符串）
     * @return 点评文本
     */
    public String analyzeAnswerWithContext(String jobPosition, String style,
                                           String question, String answer, String context) {
        log.info("开始分析回答（带RAG上下文） - 岗位: {}, 风格: {}, 问题: {}", jobPosition, style, question);
        log.info("用户回答: {}", answer);
        if (context != null && !context.isEmpty()) {
            log.info("RAG知识上下文长度: {} 字符", context.length());
        } else {
            log.info("无RAG知识上下文，使用普通模式");
        }

        try {
            // 构建提示词（包含RAG上下文）
            String prompt = buildAnalysisPromptWithContext(jobPosition, style, question, answer, context);
            log.debug("生成的提示词长度: {}", prompt.length());

            // 调用通义千问API
            String aiResponse = callQwen(prompt);
            log.debug("AI原始响应: {}", aiResponse);

            // 解析AI返回的文本
            String evaluation = extractEvaluation(aiResponse);

            log.info("分析完成 - 评价: {}", evaluation);
            return evaluation;

        } catch (Exception e) {
            log.error("分析回答时发生异常: {}", e.getMessage(), e);
            return "抱歉，AI分析服务暂时不可用，请稍后重试。";
        }
    }

    public Map<String, Object> analyzeSkillGap(List<String> resumeSkills, String jobPosition) {
        Map<String, Object> result = new HashMap<>();

        // 根据岗位获取要求
        Map<String, List<String>> jobRequirements = getJobRequirements(jobPosition);
        List<String> requiredSkills = jobRequirements.get("required");
        List<String> preferredSkills = jobRequirements.get("preferred");

        // 计算匹配的技能
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedPreferred = new ArrayList<>();

        for (String skill : requiredSkills) {
            boolean found = resumeSkills.stream().anyMatch(s ->
                    s.toLowerCase().contains(skill.toLowerCase()) ||
                            skill.toLowerCase().contains(s.toLowerCase()));
            if (found) {
                matchedRequired.add(skill);
            } else {
                missingRequired.add(skill);
            }
        }

        for (String skill : preferredSkills) {
            boolean found = resumeSkills.stream().anyMatch(s ->
                    s.toLowerCase().contains(skill.toLowerCase()) ||
                            skill.toLowerCase().contains(s.toLowerCase()));
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

        result.put("matchScore", Math.round(matchScore));
        result.put("matchedRequired", matchedRequired);
        result.put("missingRequired", missingRequired);
        result.put("matchedPreferred", matchedPreferred);
        result.put("suggestions", suggestions);
        result.put("level", matchScore >= 80 ? "优秀" : (matchScore >= 60 ? "良好" : "待提升"));

        return result;
    }

    /**
     * 获取岗位要求
     */
    private Map<String, List<String>> getJobRequirements(String jobPosition) {
        Map<String, List<String>> requirements = new HashMap<>();

        switch (jobPosition) {
            case "backend":
                requirements.put("required", Arrays.asList("Java", "Spring Boot", "MySQL", "Git", "REST API"));
                requirements.put("preferred", Arrays.asList("Redis", "消息队列", "微服务", "Docker"));
                break;
            case "frontend":
                requirements.put("required", Arrays.asList("HTML/CSS", "JavaScript", "Vue.js", "Git"));
                requirements.put("preferred", Arrays.asList("React", "TypeScript", "Webpack"));
                break;
            case "algo":
                requirements.put("required", Arrays.asList("Python", "数据结构", "算法", "机器学习基础"));
                requirements.put("preferred", Arrays.asList("NumPy/Pandas", "PyTorch/TensorFlow", "SQL"));
                break;
            case "test":
                requirements.put("required", Arrays.asList("测试理论", "测试用例设计", "缺陷管理", "SQL"));
                requirements.put("preferred", Arrays.asList("自动化测试", "性能测试", "接口测试"));
                break;
            default:
                requirements.put("required", Arrays.asList("编程基础", "沟通能力", "学习能力"));
                requirements.put("preferred", Arrays.asList());
        }

        return requirements;
    }

    /**
     * 获取技能学习建议
     */
    private String getSuggestionForSkill(String skill) {
        Map<String, String> suggestions = new HashMap<>();
        suggestions.put("Java", "建议学习《Java核心技术》或参加Java进阶课程，多做项目实践");
        suggestions.put("Spring Boot", "建议学习Spring Boot官方文档，完成一个完整的Web项目");
        suggestions.put("MySQL", "建议学习SQL优化和索引原理，练习复杂查询编写");
        suggestions.put("Git", "建议学习Git常用命令和分支管理策略");
        suggestions.put("REST API", "建议学习RESTful API设计规范，了解HTTP协议");
        suggestions.put("Redis", "建议学习Redis数据结构和缓存策略");
        suggestions.put("HTML/CSS", "建议学习Flex/Grid布局，练习页面还原");
        suggestions.put("JavaScript", "建议学习ES6+语法，理解闭包和异步编程");
        suggestions.put("Vue.js", "建议学习Vue3组合式API，完成一个完整的前端项目");
        suggestions.put("Python", "建议学习Python高级特性，练习算法题");

        return suggestions.getOrDefault(skill, "建议通过官方文档和实战项目学习");
    }

    public String parseResume(String resumeContent, String jobPosition) {
        log.info("开始解析简历，岗位: {}", jobPosition);

        try {
            String prompt = buildResumeParsePrompt(resumeContent, jobPosition);
            String aiResponse = callQwen(prompt);
            log.info("简历解析完成");
            return aiResponse;
        } catch (Exception e) {
            log.error("简历解析失败: {}", e.getMessage(), e);
            return "{\"skills\":[],\"projects\":[],\"education\":\"\",\"experienceYears\":0,\"certificates\":[],\"summary\":\"解析失败\"}";
        }
    }

    /**
     * 构建简历解析提示词
     */
    private String buildResumeParsePrompt(String resumeContent, String jobPosition) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的HR，请解析以下简历内容，提取关键信息。\n\n");
        prompt.append("【简历内容】\n");
        prompt.append(resumeContent);
        prompt.append("\n\n");

        if (jobPosition != null && !jobPosition.isEmpty()) {
            prompt.append("【目标岗位】\n").append(jobPosition).append("\n\n");
        }

        prompt.append("请严格按照以下JSON格式返回解析结果，不要返回其他内容：\n");
        prompt.append("{\n");
        prompt.append("  \"skills\": [\"技能1\", \"技能2\", \"技能3\"],\n");
        prompt.append("  \"projects\": [\"项目1\", \"项目2\"],\n");
        prompt.append("  \"education\": \"学历信息，如：本科/硕士/博士 + 学校 + 专业\",\n");
        prompt.append("  \"experienceYears\": 工作年限（数字，如没有则为0）,\n");
        prompt.append("  \"certificates\": [\"证书1\", \"证书2\"],\n");
        prompt.append("  \"summary\": \"一句话总结候选人的核心优势和特点\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }


    /**
     * 构建带RAG上下文的分析提示词
     */
    private String buildAnalysisPromptWithContext(String jobPosition, String style,
                                                  String question, String answer, String context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的").append(jobPosition).append("面试官，风格").append(style).append("。\n\n");

        // 如果有RAG知识上下文，添加到提示词中
        if (context != null && !context.isEmpty()) {
            prompt.append("【参考知识库】\n");
            prompt.append("以下是与本次面试相关的参考资料，请基于这些知识来评估用户的回答：\n");
            prompt.append("---\n");
            prompt.append(context);
            prompt.append("\n---\n\n");
            prompt.append("请对照以上参考知识库中的标准来评价用户的回答。\n\n");
        }

        prompt.append("【面试问题】\n").append(question).append("\n\n");
        prompt.append("【用户回答】\n").append(answer).append("\n\n");

        prompt.append("请对上述回答进行专业点评（80-120字），指出优点和不足。");
        prompt.append("只返回点评文本，不要返回JSON或其他格式。");

        return prompt.toString();
    }
}