package org.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.dao.KnowledgeRepository;
import org.demo.entity.Knowledge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RAG知识库核心服务
 * 负责知识点的向量化、存储和检索
 */
@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 1. 向量生成相关 ====================

    /**
     * 生成文本的embedding向量
     */
    public List<Float> generateEmbedding(String text) {
        return aiService.generateEmbedding(text);
    }

    /**
     * 将List<Float>转换为JSON字符串
     */
    private String vectorToJson(List<Float> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            throw new RuntimeException("向量转JSON失败", e);
        }
    }

    /**
     * 将JSON字符串转换为List<Float>向量
     */
    private List<Float> jsonToVector(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Float>>() {});
        } catch (Exception e) {
            log.error("JSON转向量失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        int dim = Math.min(vec1.size(), vec2.size());
        if (dim == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < dim; i++) {
            double v1 = vec1.get(i).doubleValue();
            double v2 = vec2.get(i).doubleValue();
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    // ==================== 2. 批量导入方法 ====================

    /**
     * 批量导入岗位JD
     * @param jdList 岗位JD列表
     * @return 导入结果统计
     */
    @Transactional
    public Map<String, Object> batchImportJobDescriptions(List<Map<String, Object>> jdList) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> jd : jdList) {
            try {
                String jobPosition = (String) jd.get("jobPosition");
                String title = (String) jd.get("title");
                String content = (String) jd.get("content");
                String tags = (String) jd.get("tags");
                String source = (String) jd.get("source");
                Integer importance = jd.get("importance") != null ?
                        Integer.parseInt(jd.get("importance").toString()) : 5;

                saveKnowledge(jobPosition, title, content, tags, source, "岗位描述", importance);
                success++;

            } catch (Exception e) {
                failed++;
                errors.add("导入失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", jdList.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);

        return result;
    }

    /**
     * 批量导入技术文档
     * @param docsList 技术文档列表
     * @return 导入结果统计
     */
    @Transactional
    public Map<String, Object> batchImportTechDocs(List<Map<String, Object>> docsList) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> doc : docsList) {
            try {
                String jobPosition = (String) doc.get("jobPosition");
                String title = (String) doc.get("title");
                String content = (String) doc.get("content");
                String tags = (String) doc.get("tags");
                String source = (String) doc.get("source");
                Integer importance = doc.get("importance") != null ?
                        Integer.parseInt(doc.get("importance").toString()) : 4;

                saveKnowledge(jobPosition, title, content, tags, source, "技术文档", importance);
                success++;

            } catch (Exception e) {
                failed++;
                errors.add("导入失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", docsList.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);

        return result;
    }

    /**
     * 批量导入企业文化
     * @param cultureList 企业文化列表
     * @return 导入结果统计
     */
    @Transactional
    public Map<String, Object> batchImportCulture(List<Map<String, Object>> cultureList) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> culture : cultureList) {
            try {
                String jobPosition = (String) culture.get("jobPosition");
                String title = (String) culture.get("title");
                String content = (String) culture.get("content");
                String tags = (String) culture.get("tags");
                String source = (String) culture.get("source");
                Integer importance = culture.get("importance") != null ?
                        Integer.parseInt(culture.get("importance").toString()) : 3;

                saveKnowledge(jobPosition, title, content, tags, source, "企业文化", importance);
                success++;

            } catch (Exception e) {
                failed++;
                errors.add("导入失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", cultureList.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);

        return result;
    }

    /**
     * 保存单条知识（通用方法）
     */
    @Transactional
    public Map<String, Object> saveKnowledge(String jobPosition, String title, String content,
                                             String tags, String source, String knowledgeType,
                                             Integer importance) {
        // 生成向量
        List<Float> embedding = generateEmbedding(content);

        // 创建知识条目
        Knowledge knowledge = new Knowledge();
        knowledge.setJobPosition(jobPosition);
        knowledge.setTitle(title);
        knowledge.setContent(content);
        knowledge.setTags(tags);
        knowledge.setSource(source);
        knowledge.setKnowledgeType(knowledgeType);
        knowledge.setImportance(importance);

        if (embedding != null) {
            knowledge.setEmbedding(vectorToJson(embedding));
            knowledge.setEmbeddingDim(embedding.size());
        }

        Knowledge saved = knowledgeRepository.save(knowledge);

        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("title", saved.getTitle());
        result.put("jobPosition", saved.getJobPosition());

        return result;
    }

    // ==================== 3. 知识库查询方法 ====================

    /**
     * 分页查询知识库列表
     * @param jobPosition 岗位类型（可选）
     * @param knowledgeType 知识类型（可选）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Map<String, Object> getKnowledgeList(String jobPosition, String knowledgeType,
                                                int page, int pageSize) {
        int pageIndex = page - 1;
        if (pageIndex < 0) {
            pageIndex = 0;
        }

        Pageable pageable = PageRequest.of(
                pageIndex, pageSize,
                Sort.by("importance").descending()
                        .and(Sort.by("createdAt").descending())
        );

        Page<Knowledge> pageData;

        if (jobPosition != null && knowledgeType != null) {
            // 按岗位和类型查询
            pageData = knowledgeRepository.findByJobPositionAndKnowledgeType(
                    jobPosition, knowledgeType, pageable);
        } else if (jobPosition != null) {
            // 只按岗位查询
            pageData = knowledgeRepository.findByJobPosition(jobPosition, pageable);
        } else if (knowledgeType != null) {
            // 只按类型查询
            pageData = knowledgeRepository.findByKnowledgeType(knowledgeType, pageable);
        } else {
            // 查询所有
            pageData = knowledgeRepository.findAll(pageable);
        }

        List<Map<String, Object>> list = pageData.getContent().stream()
                .map(k -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", k.getId());
                    item.put("title", k.getTitle());
                    item.put("jobPosition", k.getJobPosition());
                    item.put("knowledgeType", k.getKnowledgeType());
                    item.put("tags", k.getTags());
                    item.put("importance", k.getImportance());
                    item.put("source", k.getSource());
                    item.put("createdAt", k.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", pageData.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);

        return result;
    }

    // ==================== 4. RAG检索核心方法 ====================

    /**
     * 根据问题检索最相关的知识
     * @param query 用户问题
     * @param jobPosition 岗位类型（可选）
     * @param topK 返回最相似的K条结果
     * @param minScore 最低相似度阈值
     * @return 相似度排序的知识片段列表
     */
    public Map<String, Object> retrieveKnowledge(String query, String jobPosition,
                                                 int topK, double minScore) {
        Map<String, Object> result = new HashMap<>();

        // 1. 生成查询问题的向量
        List<Float> queryVector = generateEmbedding(query);
        if (queryVector == null || queryVector.isEmpty()) {
            result.put("total", 0);
            result.put("query", query);
            result.put("results", new ArrayList<>());
            return result;
        }

        // 2. 获取候选知识点
        List<Knowledge> candidates;
        if (jobPosition != null && !jobPosition.isEmpty()) {
            candidates = knowledgeRepository.findActiveByJobPosition(jobPosition);
        } else {
            candidates = knowledgeRepository.findAllActive();
        }

        if (candidates.isEmpty()) {
            result.put("total", 0);
            result.put("query", query);
            result.put("results", new ArrayList<>());
            return result;
        }

        // 3. 计算相似度并排序
        List<Map<String, Object>> results = candidates.stream()
                .map(knowledge -> {
                    List<Float> knowledgeVector = jsonToVector(knowledge.getEmbedding());
                    if (knowledgeVector == null || knowledgeVector.isEmpty()) {
                        return null;
                    }

                    double similarity = cosineSimilarity(queryVector, knowledgeVector);

                    // 只返回超过阈值的
                    if (similarity < minScore) {
                        return null;
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", knowledge.getId());
                    item.put("title", knowledge.getTitle());
                    item.put("content", truncateContent(knowledge.getContent(), 500));
                    item.put("fullContent", knowledge.getContent());
                    item.put("jobPosition", knowledge.getJobPosition());
                    item.put("knowledgeType", knowledge.getKnowledgeType());
                    item.put("tags", knowledge.getTags());
                    item.put("similarity", similarity);
                    item.put("importance", knowledge.getImportance());
                    item.put("source", knowledge.getSource());

                    return item;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    double simA = (double) a.get("similarity");
                    double simB = (double) b.get("similarity");
                    int impA = (int) a.get("importance");
                    int impB = (int) b.get("importance");

                    // 加权排序：相似度 * 重要性系数
                    double scoreA = simA * (impA / 5.0);
                    double scoreB = simB * (impB / 5.0);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(topK)
                .collect(Collectors.toList());

        // 4. 更新被检索到的知识点的使用次数
        if (!results.isEmpty()) {
            List<Long> ids = results.stream()
                    .map(item -> (Long) item.get("id"))
                    .collect(Collectors.toList());
            knowledgeRepository.batchIncrementUsageCount(ids);
        }

        result.put("total", results.size());
        result.put("query", query);
        result.put("results", results);

        return result;
    }

    /**
     * 简单检索（默认参数）
     */
    public Map<String, Object> retrieveKnowledge(String query) {
        return retrieveKnowledge(query, null, 5, 0.3);
    }

    /**
     * 带岗位筛选的检索
     */
    public Map<String, Object> retrieveKnowledgeByJob(String query, String jobPosition) {
        return retrieveKnowledge(query, jobPosition, 5, 0.3);
    }
}