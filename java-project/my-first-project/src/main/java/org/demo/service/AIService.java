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
}