package org.demo.controller;

import org.demo.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 内容分析控制器
 * 提供技术正确性、知识深度、逻辑严谨性等评分接口
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 技术正确性评分
     * POST /api/analysis/correctness
     *
     * 请求体格式：
     * {
     *   "question": "请介绍一下Java内存区域",
     *   "answer": "Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器",
     *   "jobPosition": "Java后端",
     *   "referenceAnswer": "标准答案（可选）"
     * }
     */
    @PostMapping("/correctness")
    public Map<String, Object> analyzeCorrectness(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String question = (String) params.get("question");
            String answer = (String) params.get("answer");
            String jobPosition = (String) params.get("jobPosition");
            String referenceAnswer = (String) params.get("referenceAnswer");

            if (question == null || question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                result.put("data", null);
                return result;
            }

            if (answer == null || answer.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "回答不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeCorrectness(
                    question, answer, jobPosition, referenceAnswer);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }


    /**
     * 知识深度评分
     * POST /api/analysis/depth
     *
     * 请求体格式：
     * {
     *   "question": "请介绍一下Java内存区域",
     *   "answer": "Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器。其中堆是线程共享的，存储对象实例；栈是线程私有的，存储局部变量和方法调用。",
     *   "jobPosition": "Java后端"
     * }
     */
    @PostMapping("/depth")
    public Map<String, Object> analyzeDepth(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String question = (String) params.get("question");
            String answer = (String) params.get("answer");
            String jobPosition = (String) params.get("jobPosition");

            if (question == null || question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                result.put("data", null);
                return result;
            }

            if (answer == null || answer.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "回答不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeDepth(
                    question, answer, jobPosition);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 逻辑严谨性评分
     * POST /api/analysis/logic
     *
     * 请求体格式：
     * {
     *   "question": "请分享一个你解决过的技术难题",
     *   "answer": "在电商项目中，我们遇到了高并发下库存超卖的问题。我首先分析了问题的原因，发现是数据库行锁竞争激烈。然后我提出了使用Redis分布式锁的方案，在减库存前先获取锁。实施后，超卖问题得到解决，系统并发能力提升了3倍。",
     *   "jobPosition": "Java后端",
     *   "questionType": "project"  // project:项目经历, behavior:行为题, scenario:场景题
     * }
     */
    @PostMapping("/logic")
    public Map<String, Object> analyzeLogic(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String question = (String) params.get("question");
            String answer = (String) params.get("answer");
            String jobPosition = (String) params.get("jobPosition");
            String questionType = (String) params.get("questionType");

            if (question == null || question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                result.put("data", null);
                return result;
            }

            if (answer == null || answer.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "回答不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeLogic(
                    question, answer, jobPosition, questionType);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 岗位匹配度分析
     * POST /api/analysis/match
     *
     * 请求体格式：
     * {
     *   "resume": "3年Java开发经验，熟练掌握Spring Boot、MyBatis、MySQL，有分布式系统开发经验，熟悉Redis、消息队列...",
     *   "jobDescription": "岗位要求：5年以上Java开发经验，精通Spring Cloud微服务架构，熟悉分布式系统设计，有高并发项目经验...",
     *   "jobPosition": "Java高级工程师",
     *   "candidateName": "张三"  // 可选
     * }
     */
    @PostMapping("/match")
    public Map<String, Object> analyzeMatch(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String resume = (String) params.get("resume");
            String jobDescription = (String) params.get("jobDescription");
            String jobPosition = (String) params.get("jobPosition");
            String candidateName = (String) params.get("candidateName");

            if (resume == null || resume.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "简历内容不能为空");
                result.put("data", null);
                return result;
            }

            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "岗位描述不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeMatch(
                    resume, jobDescription, jobPosition, candidateName);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 语速分析
     * POST /api/analysis/speed
     *
     * 请求体格式：
     * {
     *   "message": "Java内存区域主要包括方法区、堆、虚拟机栈、本地方法栈和程序计数器",
     *   "duration": 45,
     *   "session_id": "abc123"
     * }
     */
    @PostMapping("/speed")
    public Map<String, Object> analyzeSpeed(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String message = (String) params.get("message");
            Integer duration = params.get("duration") != null ?
                    Integer.parseInt(params.get("duration").toString()) : null;
            String sessionId = (String) params.get("session_id");

            // 参数校验
            if (message == null || message.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "消息内容不能为空");
                result.put("data", null);
                return result;
            }

            if (duration == null || duration <= 0) {
                result.put("code", 400);
                result.put("message", "回答时长必须大于0");
                result.put("data", null);
                return result;
            }

            // 计算语速
            int wordCount = message.length();
            double speed = (wordCount / (double) duration) * 60; // 字/分钟

            // 分析语速等级
            Map<String, Object> speedResult = analyzeSpeedLevel(speed, sessionId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", speedResult);

        } catch (NumberFormatException e) {
            result.put("code", 400);
            result.put("message", "duration参数格式错误");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "语速分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 分析语速等级
     */
    private Map<String, Object> analyzeSpeedLevel(double speed, String sessionId) {
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

        result.put("speed", Math.round(speed * 10) / 10.0); // 保留一位小数
        result.put("level", level);
        result.put("description", description);
        result.put("suggestion", suggestion);
        result.put("session_id", sessionId);

        return result;
    }
    /**
     * 文本情绪分析
     * POST /api/analysis/sentiment
     *
     * 请求体格式：
     * {
     *   "message": "这个问题太棒了！我正好准备过，可以详细讲讲我的理解",
     *   "session_id": "abc123"
     * }
     */
    @PostMapping("/sentiment")
    public Map<String, Object> analyzeSentiment(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String message = (String) params.get("message");
            String sessionId = (String) params.get("session_id");

            if (message == null || message.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "消息内容不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeSentiment(message, sessionId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "情绪分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 自信度分析
     * POST /api/analysis/confidence
     *
     * 请求体格式：
     * {
     *   "message": "这个问题可能...也许这样...我不太确定...",
     *   "session_id": "abc123"
     * }
     */
    @PostMapping("/confidence")
    public Map<String, Object> analyzeConfidence(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String message = (String) params.get("message");
            String sessionId = (String) params.get("session_id");

            if (message == null || message.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "消息内容不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> analysisResult = analysisService.analyzeConfidence(message, sessionId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", analysisResult);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "自信度分析失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}