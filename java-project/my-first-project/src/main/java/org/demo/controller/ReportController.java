package org.demo.controller;

import org.demo.dao.AnalysisResultRepository;
import org.demo.dao.HistoryRepository;
import org.demo.entity.AnalysisResult;
import org.demo.entity.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private HistoryRepository historyRepository;

    /**
     * 获取用户的面试历史记录列表（用于前端历史记录页面）
     * GET /api/report/history?page=1&pageSize=10
     */
    @GetMapping("/history")
    public Map<String, Object> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                userId = 1L;
            }

            // 添加 try-catch 包裹查询，防止表不存在或数据为空
            List<AnalysisResult> reports = new ArrayList<>();
            long total = 0;

            try {
                Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
                Page<AnalysisResult> reportPage = analysisResultRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
                reports = reportPage.getContent();
                total = reportPage.getTotalElements();
            } catch (Exception e) {
                log.warn("查询历史记录失败（可能是表不存在或无数据）: {}", e.getMessage());
                // 返回空数据，不报错
            }

            List<Map<String, Object>> historyList = new ArrayList<>();
            for (AnalysisResult report : reports) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", report.getId());
                item.put("session_id", report.getSessionId());
                item.put("job_position", report.getJobPosition());
                item.put("difficulty", "中级");
                item.put("overall_score", report.getTotalScore() != null ? report.getTotalScore().intValue() : 0);
                item.put("tech_score", report.getCorrectnessAvg() != null ? report.getCorrectnessAvg().intValue() : 0);
                item.put("communication_score", report.getConfidenceAvg() != null ? report.getConfidenceAvg().intValue() : 0);
                item.put("logic_score", report.getLogicAvg() != null ? report.getLogicAvg().intValue() : 0);
                item.put("created_at", report.getCreatedAt());
                historyList.add(item);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("list", historyList);
            data.put("page", page);
            data.put("pageSize", pageSize);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("获取历史记录失败", e);
            // 发生任何错误都返回空数据，而不是500
            Map<String, Object> data = new HashMap<>();
            data.put("total", 0);
            data.put("list", new ArrayList<>());
            data.put("page", page);
            data.put("pageSize", pageSize);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        }

        return result;
    }

    /**
     * 获取单次面试的详细报告
     * GET /api/report/detail/{sessionId}
     */
    @GetMapping("/detail/{sessionId}")
    public Map<String, Object> getReportDetail(@PathVariable String sessionId) {

        Map<String, Object> result = new HashMap<>();

        try {
            Optional<AnalysisResult> reportOpt = analysisResultRepository.findBySessionId(sessionId);

            if (!reportOpt.isPresent()) {
                result.put("code", 404);
                result.put("message", "报告不存在");
                result.put("data", null);
                return result;
            }

            AnalysisResult report = reportOpt.get();

            Map<String, Object> data = new HashMap<>();
            data.put("session_id", report.getSessionId());
            data.put("overall_score", report.getTotalScore() != null ? report.getTotalScore().intValue() : 0);
            data.put("tech_score", report.getCorrectnessAvg() != null ? report.getCorrectnessAvg().intValue() : 0);
            data.put("communication_score", report.getConfidenceAvg() != null ? report.getConfidenceAvg().intValue() : 0);
            data.put("logic_score", report.getLogicAvg() != null ? report.getLogicAvg().intValue() : 0);
            data.put("job_position", report.getJobPosition());
            data.put("created_at", report.getCreatedAt());
            data.put("strengths", report.getStrengths());
            data.put("weaknesses", report.getWeaknesses());
            data.put("improvements", report.getImprovements());
            data.put("summary", report.getSummary());

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取报告失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 获取用户的能力曲线数据（最近N次面试的分数趋势）
     * GET /api/report/ability-curve?limit=10
     */
    @GetMapping("/ability-curve")
    public Map<String, Object> getAbilityCurve(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                userId = 1L;
            }

            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").ascending());
            List<AnalysisResult> reports = analysisResultRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent();

            // 按时间正序排列
            Collections.reverse(reports);

            List<Map<String, Object>> curveData = new ArrayList<>();
            for (AnalysisResult report : reports) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", report.getCreatedAt());
                point.put("overall", report.getTotalScore());
                point.put("tech", report.getCorrectnessAvg());
                point.put("communication", report.getConfidenceAvg());
                point.put("logic", report.getLogicAvg());
                curveData.add(point);
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", curveData);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取能力曲线失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 从token提取用户ID（简化版本）
     */
    private Long extractUserIdFromToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        // 实际应该解析JWT token，这里简化处理
        // 可以从数据库根据token查询用户
        return 1L; // 临时返回默认用户ID
    }
}