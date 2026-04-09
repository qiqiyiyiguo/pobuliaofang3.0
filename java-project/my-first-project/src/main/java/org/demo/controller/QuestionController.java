package org.demo.controller;

import org.demo.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 题库管理控制器
 * 提供各技术模块的题目列表接口
 */
@RestController
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // ==================== 对外API接口（供前端调用）====================

    /**
     * 1. 获取 Java 题目列表
     * GET /question/Java?page=1&pageSize=10&category=技术知识
     */
    @GetMapping("/question/Java")
    public Map<String, Object> getJavaQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return getQuestionsByModule("Java", page, pageSize, category);
    }

    /**
     * 2. 获取 Web 题目列表
     * GET /question/Web?page=1&pageSize=10&category=技术知识
     */
    @GetMapping("/question/Web")
    public Map<String, Object> getWebQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return getQuestionsByModule("Web", page, pageSize, category);
    }

    /**
     * 3. 获取 Python 题目列表
     * GET /question/Python?page=1&pageSize=10&category=技术知识
     */
    @GetMapping("/question/Python")
    public Map<String, Object> getPythonQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return getQuestionsByModule("Python", page, pageSize, category);
    }

    /**
     * 4. 获取 Test 题目列表
     * GET /question/Test?page=1&pageSize=10&category=技术知识
     */
    @GetMapping("/question/Test")
    public Map<String, Object> getTestQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return getQuestionsByModule("Test", page, pageSize, category);
    }

    /**
     * 通用方法：根据模块获取题目列表
     */
    private Map<String, Object> getQuestionsByModule(String module, int page, int pageSize, String category) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证token（实际应由拦截器处理）
            // 这里简化处理，假设token验证通过

            // 调用Service层获取分页数据
            Map<String, Object> pageData = questionService.getQuestionsByModule(module, page, pageSize, category);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", pageData);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }

    // ==================== 题目类型接口 =====================

    /**
     * 获取题目类型列表（供前端类型候选框使用）
     * GET /api/question-types
     * 对应文档 1.1
     */
    @GetMapping("/api/question-types")
    public Map<String, Object> getQuestionTypes() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service获取所有题目类型
            Object typesData = questionService.getAllQuestionTypes();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", typesData);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }

    /**
     * 根据类型ID获取题库列表
     * GET /api/question-banks?type_id=1
     * 对应文档 1.2
     */
    @GetMapping("/api/question-banks")
    public Map<String, Object> getQuestionBanksByType(
            @RequestParam("type_id") Integer typeId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 参数校验
            if (typeId == null) {
                result.put("code", 400);
                result.put("message", "类型ID不能为空");
                result.put("data", null);
                return result;
            }

            // 调用Service获取该类型下的题目列表
            Map<String, Object> banksData = questionService.getQuestionBanksByType(typeId);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", banksData);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }

    // ==================== 管理后台接口（可选，可保留）====================

    /**
     * 新增题目（管理后台用）
     * POST /api/questions
     */
    @PostMapping("/api/questions")
    public Map<String, Object> createQuestion(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里需要实现管理后台的权限验证
            Object question = questionService.createQuestionFromMap(params);

            result.put("code", 200);
            result.put("message", "添加成功");
            result.put("data", question);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }

    /**
     * 更新题目（管理后台用）
     * PUT /api/questions/{id}
     */
    @PutMapping("/api/questions/{id}")
    public Map<String, Object> updateQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            params.put("id", id);
            Object question = questionService.updateQuestionFromMap(params);

            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", question);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }

    /**
     * 删除题目（管理后台用）
     * DELETE /api/questions/{id}
     */
    @DeleteMapping("/api/questions/{id}")
    public Map<String, Object> deleteQuestion(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            questionService.deleteQuestion(id);

            result.put("code", 200);
            result.put("message", "删除成功");
            result.put("data", null);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
            result.put("data", null);
        }

        return result;
    }
}