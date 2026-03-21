package org.demo.controller;

import org.demo.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话控制器
 * 处理AI分析相关的接口，不保存记录，只调用AI服务
 */
@RestController
@RequestMapping("/api/ai")
public class ChatController {

    @Autowired
    private AIService aiService;

    /**
     * 分析用户回答
     * POST /api/ai/analyze
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeAnswer(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解析参数
            String question = params.get("question") != null ? params.get("question").toString() : null;
            String answer = params.get("answer") != null ? params.get("answer").toString() : null;
            String jobPosition = params.get("jobPosition") != null ? params.get("jobPosition").toString() : null;
            String style = params.get("style") != null ? params.get("style").toString() : null;

            // 参数校验
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

            // 设置默认值
            if (jobPosition == null || jobPosition.trim().isEmpty()) {
                jobPosition = "通用";
            }
            if (style == null || style.trim().isEmpty()) {
                style = "友好";
            }

            // 2. 调用AIService分析回答
            String evaluation = aiService.analyzeAnswer(jobPosition, style, question, answer);

            // 3. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("evaluation", evaluation);

            // 4. 返回成功响应
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "AI分析服务异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 简单对话接口
     * POST /api/ai/chat
     */
    @PostMapping("/chat")
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
            result.put("code", 500);
            result.put("message", "对话服务异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 批量分析接口
     * POST /api/ai/analyze/batch
     */
    @PostMapping("/analyze/batch")
    public Map<String, Object> analyzeBatch(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jobPosition = params.get("jobPosition") != null ? params.get("jobPosition").toString() : null;
            String style = params.get("style") != null ? params.get("style").toString() : null;

            // 获取问答列表
            Object questionsObj = params.get("questions");
            List<Map<String, String>> qaList = new ArrayList<>();

            if (questionsObj instanceof List) {
                for (Object item : (List<?>) questionsObj) {
                    if (item instanceof Map) {
                        Map<String, String> qa = new HashMap<>();
                        Map<?, ?> itemMap = (Map<?, ?>) item;

                        Object q = itemMap.get("question");
                        Object a = itemMap.get("answer");

                        if (q != null) {
                            qa.put("question", q.toString());
                        }
                        if (a != null) {
                            qa.put("answer", a.toString());
                        }
                        qaList.add(qa);
                    }
                }
            }

            if (qaList.isEmpty()) {
                result.put("code", 400);
                result.put("message", "问答列表不能为空");
                result.put("data", null);
                return result;
            }

            if (jobPosition == null || jobPosition.trim().isEmpty()) {
                jobPosition = "通用";
            }
            if (style == null || style.trim().isEmpty()) {
                style = "友好";
            }

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
            result.put("code", 500);
            result.put("message", "批量分析异常: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}