package org.demo.controller;

import org.demo.entity.AnalysisResult;
import org.demo.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告控制器
 * 负责面试结束后的报告生成、查询与展示
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportGenerationService reportGenerationService;

    /**
     * 1. 生成面试报告
     * POST /api/report/generate
     * 逻辑：面试结束后点击“生成报告”，后台汇总 History 记录并调用 AI 生成总结
     */
    @PostMapping("/generate")
    public Map<String, Object> generateReport(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sessionId = (String) params.get("sessionId");
            Long userId = Long.valueOf(params.get("userId").toString());
            String title = (String) params.get("title");

            if (sessionId == null || userId == null) {
                result.put("code", 400);
                result.put("message", "sessionId 或 userId 不能为空");
                return result;
            }

            // 调用 Service 整合数据并保存到 analysis_result 表
            Map<String, Object> report = reportGenerationService.generateComprehensiveReport(sessionId, userId, title);

            result.put("code", 200);
            result.put("message", "报告生成成功");
            result.put("data", report);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "生成报告失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 2. 获取单篇报告详情
     * GET /api/report/detail/{id}
     * 用于前端渲染雷达图、优缺点列表等
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getReportDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            AnalysisResult report = reportGenerationService.getReportById(id);
            if (report == null) {
                result.put("code", 404);
                result.put("message", "报告不存在");
                return result;
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", report);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 3. 获取用户的历史报告列表
     * GET /api/report/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserReports(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<AnalysisResult> reports = reportGenerationService.getReportsByUserId(userId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", reports);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取列表失败：" + e.getMessage());
        }

        return result;
    }
}