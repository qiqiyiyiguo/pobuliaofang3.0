package org.demo.controller;

import org.demo.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 历史记录控制器
 * 处理问答记录的保存和查询
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * 1. 保存一次问答记录
     * POST /api/history/save
     *
     * 请求体格式：
     * {
     *   "userId": 1001,
     *   "question": "请介绍一下Java内存区域",
     *   "answer": "Java内存区域主要包括方法区、堆、虚拟机栈...",
     *   "evaluation": "回答得很好",
     *   "score": 85.5,
     *   "jobPosition": "Java",
     *   "style": "友好"
     * }
     *
     * @param params 请求参数
     * @return 保存结果
     */
    @PostMapping("/save")
    public Map<String, Object> saveHistory(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 参数校验
            if (!params.containsKey("userId") || params.get("userId") == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                result.put("data", null);
                return result;
            }

            if (!params.containsKey("question") || params.get("question") == null) {
                result.put("code", 400);
                result.put("message", "问题内容不能为空");
                result.put("data", null);
                return result;
            }

            if (!params.containsKey("answer") || params.get("answer") == null) {
                result.put("code", 400);
                result.put("message", "回答内容不能为空");
                result.put("data", null);
                return result;
            }

            // 调用Service保存记录
            Map<String, Object> savedData = historyService.saveHistory(params);

            result.put("code", 200);
            result.put("message", "保存成功");
            result.put("data", savedData);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "保存失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 2. 查询用户的历史问答记录
     * GET /api/history/list?userId=1001&page=1&pageSize=10
     *
     * @param userId 用户ID
     * @param page 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @return 分页的历史记录
     */
    @GetMapping("/list")
    public Map<String, Object> getHistoryList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (userId == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> historyData = historyService.getHistoryList(userId, page, pageSize);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", historyData);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 3. 获取分数折线图数据
     * GET /api/history/scores?userId=1001&limit=10
     *
     * @param userId 用户ID
     * @param limit 返回的最大记录数（默认10）
     * @return 分数趋势数据
     */
    @GetMapping("/scores")
    public Map<String, Object> getScoreTrend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (userId == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> scoreData = historyService.getScoreTrend(userId, limit);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", scoreData);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分数趋势失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 4. 获取单次问答详情（可选）
     * GET /api/history/detail/{id}
     *
     * @param id 历史记录ID
     * @return 问答详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getHistoryDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> detail = historyService.getHistoryDetail(id);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", detail);

        } catch (IllegalArgumentException e) {
            result.put("code", 404);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取详情失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 5. 删除历史记录（可选）
     * DELETE /api/history/delete/{id}
     *
     * @param id 历史记录ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteHistory(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            historyService.deleteHistory(id);

            result.put("code", 200);
            result.put("message", "删除成功");
            result.put("data", null);

        } catch (IllegalArgumentException e) {
            result.put("code", 404);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 6. 获取用户统计信息（可选）
     * GET /api/history/stats?userId=1001
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getUserStats(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> stats = historyService.getUserStats(userId);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", stats);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取统计失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}