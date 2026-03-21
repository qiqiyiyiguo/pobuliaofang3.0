package org.demo.controller;

import org.demo.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;  // 添加这个导入
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * 处理岗位JD、技术文档、企业文化的导入和管理
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 1. 批量导入岗位JD
     * POST /api/knowledge/import/jd
     */
    @PostMapping("/import/jd")
    public Map<String, Object> importJobDescriptions(@RequestBody List<Map<String, Object>> jdList) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (jdList == null || jdList.isEmpty()) {
                result.put("code", 400);
                result.put("message", "岗位JD列表不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> importResult = knowledgeService.batchImportJobDescriptions(jdList);

            result.put("code", 200);
            result.put("message", "导入成功");
            result.put("data", importResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "导入失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 2. 批量导入技术文档（Markdown/文本格式）
     * POST /api/knowledge/import/docs
     */
    @PostMapping("/import/docs")
    public Map<String, Object> importTechDocs(@RequestBody List<Map<String, Object>> docsList) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (docsList == null || docsList.isEmpty()) {
                result.put("code", 400);
                result.put("message", "技术文档列表不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> importResult = knowledgeService.batchImportTechDocs(docsList);

            result.put("code", 200);
            result.put("message", "导入成功");
            result.put("data", importResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "导入失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 3. 批量导入企业文化资料
     * POST /api/knowledge/import/culture
     */
    @PostMapping("/import/culture")
    public Map<String, Object> importCulture(@RequestBody List<Map<String, Object>> cultureList) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (cultureList == null || cultureList.isEmpty()) {
                result.put("code", 400);
                result.put("message", "企业文化资料不能为空");
                result.put("data", null);
                return result;
            }

            Map<String, Object> importResult = knowledgeService.batchImportCulture(cultureList);

            result.put("code", 200);
            result.put("message", "导入成功");
            result.put("data", importResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "导入失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 4. 上传Markdown文件导入技术文档
     * POST /api/knowledge/import/docs/file
     */
    @PostMapping("/import/docs/file")
    public Map<String, Object> importTechDocsFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobPosition") String jobPosition,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "knowledgeType", defaultValue = "技术文档") String knowledgeType) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                result.put("data", null);
                return result;
            }

            // 读取文件内容
            String content = new String(file.getBytes());
            String fileName = file.getOriginalFilename();
            String title = fileName != null ? fileName.substring(0, fileName.lastIndexOf('.')) : "未命名";

            // 自动生成标签
            String autoTags = generateTagsFromFileName(fileName, jobPosition, tags);

            Map<String, Object> saved = knowledgeService.saveKnowledge(
                    jobPosition,
                    title,
                    content,
                    autoTags,
                    "文件上传",
                    knowledgeType,
                    4
            );

            result.put("code", 200);
            result.put("message", "文件导入成功");
            result.put("data", saved);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "文件导入失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 5. 批量导入技术文档（从文本列表）
     * POST /api/knowledge/import/docs/batch-text
     */
    @PostMapping("/import/docs/batch-text")
    public Map<String, Object> importTechDocsBatchText(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jobPosition = (String) params.get("jobPosition");
            String baseTags = (String) params.get("baseTags");
            String knowledgeType = (String) params.get("knowledgeType");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> documents = (List<Map<String, String>>) params.get("documents");

            if (jobPosition == null || jobPosition.isEmpty()) {
                result.put("code", 400);
                result.put("message", "岗位类型不能为空");
                result.put("data", null);
                return result;
            }

            if (documents == null || documents.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文档列表不能为空");
                result.put("data", null);
                return result;
            }

            int success = 0;
            int failed = 0;
            List<String> errors = new ArrayList<>();  // 第234行

            for (Map<String, String> doc : documents) {
                try {
                    String title = doc.get("title");
                    String content = doc.get("content");
                    String docTags = doc.get("tags");

                    // 合并标签
                    String mergedTags = baseTags;
                    if (docTags != null && !docTags.isEmpty()) {
                        mergedTags = baseTags + "," + docTags;
                    }

                    knowledgeService.saveKnowledge(
                            jobPosition,
                            title,
                            content,
                            mergedTags,
                            "批量导入",
                            knowledgeType != null ? knowledgeType : "技术文档",
                            4
                    );
                    success++;

                } catch (Exception e) {
                    failed++;
                    errors.add("文档《" + doc.get("title") + "》导入失败: " + e.getMessage());
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("total", documents.size());
            data.put("success", success);
            data.put("failed", failed);
            data.put("errors", errors);

            result.put("code", 200);
            result.put("message", "批量导入完成");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "批量导入失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 6. 查询技术文档列表
     * GET /api/knowledge/docs/list?jobPosition=Java后端&type=技术文档
     */
    @GetMapping("/docs/list")
    public Map<String, Object> getTechDocsList(
            @RequestParam(required = false) String jobPosition,
            @RequestParam(required = false) String knowledgeType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> docsList = knowledgeService.getKnowledgeList(
                    jobPosition, knowledgeType, page, pageSize);

            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", docsList);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 7. RAG检索接口 - 根据问题检索最相关的知识
     * POST /api/knowledge/retrieve
     *
     * 请求体格式：
     * {
     *   "query": "Spring Boot自动配置原理是什么？",
     *   "jobPosition": "Java后端",      // 可选，按岗位筛选
     *   "topK": 5,                      // 可选，返回条数，默认5
     *   "minScore": 0.3                  // 可选，最低相似度阈值，默认0.3
     * }
     *
     * 响应格式：
     * {
     *   "code": 200,
     *   "message": "检索成功",
     *   "data": {
     *     "total": 5,
     *     "query": "Spring Boot自动配置原理是什么？",
     *     "results": [
     *       {
     *         "id": 101,
     *         "title": "Spring Boot自动配置原理",
     *         "content": "Spring Boot的自动配置是通过@EnableAutoConfiguration注解实现的...",
     *         "jobPosition": "Java后端",
     *         "knowledgeType": "技术原理",
     *         "tags": "Spring Boot,自动配置",
     *         "similarity": 0.92,
     *         "importance": 5
     *       },
     *       ...
     *     ]
     *   }
     * }
     */
    @PostMapping("/retrieve")
    public Map<String, Object> retrieveKnowledge(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解析参数
            String query = (String) params.get("query");
            String jobPosition = (String) params.get("jobPosition");
            Integer topK = params.get("topK") != null ?
                    Integer.parseInt(params.get("topK").toString()) : 5;
            Double minScore = params.get("minScore") != null ?
                    Double.parseDouble(params.get("minScore").toString()) : 0.3;

            // 2. 参数校验
            if (query == null || query.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "检索问题不能为空");
                result.put("data", null);
                return result;
            }

            if (topK < 1 || topK > 20) {
                topK = 5; // 限制范围
            }

            // 3. 调用Service执行检索
            Map<String, Object> retrieveResult = knowledgeService.retrieveKnowledge(
                    query, jobPosition, topK, minScore);

            result.put("code", 200);
            result.put("message", "检索成功");
            result.put("data", retrieveResult);

        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "检索失败：" + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从文件名生成标签
     */
    private String generateTagsFromFileName(String fileName, String jobPosition, String userTags) {
        StringBuilder tags = new StringBuilder(jobPosition);

        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            if (lowerName.contains("spring")) {
                tags.append(",Spring");
            }
            if (lowerName.contains("jvm")) {
                tags.append(",JVM");
            }
            if (lowerName.contains("并发") || lowerName.contains("thread")) {
                tags.append(",并发编程");
            }
            if (lowerName.contains("数据库") || lowerName.contains("mysql")) {
                tags.append(",数据库");
            }
            if (lowerName.contains("redis")) {
                tags.append(",Redis");
            }
            if (lowerName.contains("微服务") || lowerName.contains("cloud")) {
                tags.append(",微服务");
            }
        }

        if (userTags != null && !userTags.isEmpty()) {
            tags.append(",").append(userTags);
        }

        return tags.toString();
    }
}