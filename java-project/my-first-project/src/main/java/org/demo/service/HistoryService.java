package org.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.dao.HistoryRepository;
import org.demo.entity.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史记录业务逻辑层
 * 处理问答记录的保存、查询和统计分析
 */
@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 1. 保存记录 ====================

    /**
     * 保存单条问答记录
     * @param params 请求参数（包含userId、question、answer、evaluation、score等）
     * @return 保存后的记录信息
     */
    @Transactional
    public Map<String, Object> saveHistory(Map<String, Object> params) {
        // 1. 参数提取
        Long userId = Long.parseLong(params.get("userId").toString());
        String question = (String) params.get("question");
        String answer = (String) params.get("answer");
        String evaluation = (String) params.get("evaluation");
        Double score = params.get("score") != null ?
                Double.parseDouble(params.get("score").toString()) : null;

        // 可选参数
        String jobPosition = (String) params.get("jobPosition");
        String style = (String) params.get("style");
        String questionType = (String) params.get("questionType");
        Integer difficulty = params.get("difficulty") != null ?
                Integer.parseInt(params.get("difficulty").toString()) : null;
        Integer answerDuration = params.get("answerDuration") != null ?
                Integer.parseInt(params.get("answerDuration").toString()) : null;

        // 2. 创建History实体
        History history = new History();
        history.setUserId(userId);
        history.setQuestion(question);
        history.setAnswer(answer);
        history.setAiReply(evaluation);
        history.setScore(score);
        history.setJobPosition(jobPosition);
        history.setStyle(style);
        history.setQuestionType(questionType);
        history.setDifficulty(difficulty);
        history.setAnswerDuration(answerDuration);

        // 3. 保存到数据库
        History savedHistory = historyRepository.save(history);

        // 4. 返回保存结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedHistory.getId());
        result.put("createdAt", formatDateTime(savedHistory.getCreatedAt()));

        return result;
    }

    /**
     * 批量保存问答记录
     * @param paramsList 参数列表
     * @return 保存结果列表
     */
    @Transactional
    public List<Map<String, Object>> batchSaveHistory(List<Map<String, Object>> paramsList) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> params : paramsList) {
            try {
                Map<String, Object> result = saveHistory(params);
                results.add(result);
            } catch (Exception e) {
                // 记录错误但继续处理其他记录
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("params", params);
                results.add(error);
            }
        }

        return results;
    }

    // ==================== 2. 查询历史记录 ====================

    /**
     * 查询用户的历史记录（分页）
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页的历史记录
     */
    public Map<String, Object> getHistoryList(Long userId, int page, int pageSize) {
        // 1. 处理分页参数
        int pageIndex = page - 1;
        if (pageIndex < 0) {
            pageIndex = 0;
        }

        // 2. 创建分页请求（按时间倒序）
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending());

        // 3. 查询数据
        List<History> histories = historyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long total = historyRepository.countByUserId(userId);

        // 4. 转换为前端需要的格式
        List<Map<String, Object>> list = histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        // 5. 封装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);

        return result;
    }

    /**
     * 查询用户指定岗位的历史记录
     * @param userId 用户ID
     * @param jobPosition 岗位类型
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页的历史记录
     */
    public Map<String, Object> getHistoryListByJobPosition(Long userId, String jobPosition, int page, int pageSize) {
        int pageIndex = page - 1;
        if (pageIndex < 0) {
            pageIndex = 0;
        }

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending());

        List<History> histories = historyRepository.findByUserIdAndJobPositionOrderByCreatedAtDesc(
                userId, jobPosition, pageable);

        // 注意：这里需要自定义分页查询，暂时返回所有
        List<Map<String, Object>> list = histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) list.size());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);

        return result;
    }

    /**
     * 获取单条历史记录详情
     * @param id 记录ID
     * @return 记录详情
     */
    public Map<String, Object> getHistoryDetail(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("历史记录不存在，ID: " + id));

        return convertToDetailMap(history);
    }

    // ==================== 3. 分数趋势分析 ====================

    /**
     * 获取用户的分数趋势数据（用于折线图）
     * @param userId 用户ID
     * @param limit 返回的最大记录数
     * @return 分数趋势数据
     */
    public Map<String, Object> getScoreTrend(Long userId, int limit) {
        // 1. 查询最近limit条有分数的记录
        Pageable pageable = PageRequest.of(0, limit);
        List<Map<String, Object>> scoreData = historyRepository.findScoreTrendByUserId(userId, pageable);

        // 2. 转换格式
        List<Map<String, Object>> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Map<String, Object> item : scoreData) {
            Map<String, Object> point = new HashMap<>();

            // 处理日期
            LocalDateTime createdAt = (LocalDateTime) item.get("createdAt");
            if (createdAt != null) {
                point.put("date", createdAt.format(formatter));
            }

            // 处理分数
            Double score = (Double) item.get("score");
            point.put("score", score != null ? score : 0);

            trendList.add(point);
        }

        // 3. 按日期正序排列（折线图通常从左到右时间递增）
        Collections.reverse(trendList);

        Map<String, Object> result = new HashMap<>();
        result.put("scores", trendList);
        result.put("total", trendList.size());

        return result;
    }

    /**
     * 获取用户在某时间范围内的分数趋势
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间范围内的分数数据
     */
    public Map<String, Object> getScoreTrendByTimeRange(Long userId, String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);

            List<Map<String, Object>> scoreData = historyRepository.findScoreTrendByUserIdAndTimeRange(
                    userId, start, end);

            List<Map<String, Object>> trendList = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Map<String, Object> item : scoreData) {
                Map<String, Object> point = new HashMap<>();
                LocalDateTime createdAt = (LocalDateTime) item.get("createdAt");
                point.put("date", createdAt.format(dateFormatter));
                point.put("score", item.get("score"));
                trendList.add(point);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("scores", trendList);
            result.put("total", trendList.size());
            return result;

        } catch (Exception e) {
            throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd 格式", e);
        }
    }

    // ==================== 4. 统计分析 ====================

    /**
     * 获取用户统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 基础统计
        long totalCount = historyRepository.countByUserId(userId);
        Double averageScore = historyRepository.getAverageScoreByUserId(userId);

        // 2. 最高分和最低分
        History highest = historyRepository.findTopByUserIdAndScoreIsNotNullOrderByScoreDesc(userId);
        History lowest = historyRepository.findTopByUserIdAndScoreIsNotNullOrderByScoreAsc(userId);

        // 3. 岗位分布
        List<Map<String, Object>> jobDistribution = historyRepository.countByUserIdAndJobPosition(userId);

        stats.put("totalCount", totalCount);
        stats.put("averageScore", averageScore != null ? averageScore : 0);
        stats.put("highestScore", highest != null ? highest.getScore() : null);
        stats.put("lowestScore", lowest != null ? lowest.getScore() : null);
        stats.put("jobDistribution", jobDistribution);

        return stats;
    }

    // ==================== 5. 删除操作 ====================

    /**
     * 删除单条历史记录
     * @param id 记录ID
     */
    @Transactional
    public void deleteHistory(Long id) {
        if (!historyRepository.existsById(id)) {
            throw new IllegalArgumentException("历史记录不存在，ID: " + id);
        }
        historyRepository.deleteById(id);
    }

    /**
     * 删除用户的所有历史记录
     * @param userId 用户ID
     */
    @Transactional
    public void deleteUserHistory(Long userId) {
        historyRepository.deleteByUserId(userId);
    }

    // ==================== 6. 搜索功能 ====================

    /**
     * 按关键词搜索历史记录
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 匹配的记录列表
     */
    public List<Map<String, Object>> searchHistory(Long userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        List<History> histories = historyRepository.searchByKeyword(userId, keyword);

        return histories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    // ==================== 7. 私有辅助方法 ====================

    /**
     * 将History实体转换为Map（用于列表展示）
     */
    private Map<String, Object> convertToMap(History history) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", history.getId());
        map.put("question", history.getQuestion());

        // 回答太长时截断
        String answer = history.getAnswer();
        if (answer != null && answer.length() > 100) {
            map.put("answer", answer.substring(0, 100) + "...");
        } else {
            map.put("answer", answer);
        }

        map.put("aiReply", history.getAiReply());
        map.put("score", history.getScore());
        map.put("jobPosition", history.getJobPosition());
        map.put("questionType", history.getQuestionType());
        map.put("createdAt", formatDateTime(history.getCreatedAt()));

        return map;
    }

    /**
     * 将History实体转换为详细Map（用于详情展示）
     */
    private Map<String, Object> convertToDetailMap(History history) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", history.getId());
        map.put("userId", history.getUserId());
        map.put("jobPosition", history.getJobPosition());
        map.put("style", history.getStyle());
        map.put("question", history.getQuestion());
        map.put("answer", history.getAnswer());
        map.put("aiReply", history.getAiReply());
        map.put("score", history.getScore());
        map.put("questionType", history.getQuestionType());
        map.put("difficulty", history.getDifficulty());
        map.put("answerDuration", history.getAnswerDuration());
        map.put("createdAt", formatDateTime(history.getCreatedAt()));

        return map;
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}