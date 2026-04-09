package org.demo.service;

import org.demo.dao.QuestionBankRepository;
import org.demo.dao.QuestionTypeRepository;
import org.demo.entity.QuestionBank;
import org.demo.entity.QuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库业务逻辑层
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    // ==================== 原有的方法 ====================

    /**
     * 新增题目
     */
    public QuestionBank createQuestion(QuestionBank question) {
        validateQuestion(question);
        checkTypeExists(question.getTypeId());
        return questionBankRepository.save(question);
    }

    /**
     * 更新题目
     */
    public QuestionBank updateQuestion(QuestionBank question) {
        Long id = question.getId();
        if (id == null) {
            throw new RuntimeException("题目ID不能为空");
        }

        QuestionBank existingQuestion = questionBankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在，ID: " + id));

        validateQuestion(question);

        if (question.getTypeId() != null &&
                !question.getTypeId().equals(existingQuestion.getTypeId())) {
            checkTypeExists(question.getTypeId());
        }

        if (StringUtils.hasText(question.getName())) {
            existingQuestion.setName(question.getName());
        }
        if (StringUtils.hasText(question.getDescription())) {
            existingQuestion.setDescription(question.getDescription());
        }
        if (question.getTypeId() != null) {
            existingQuestion.setTypeId(question.getTypeId());
        }
        if (question.getJobPosition() != null) {
            existingQuestion.setJobPosition(question.getJobPosition());
        }
        if (question.getDifficulty() != null) {
            existingQuestion.setDifficulty(question.getDifficulty());
        }

        return questionBankRepository.save(existingQuestion);
    }

    /**
     * 删除题目
     */
    public void deleteQuestion(Long id) {
        if (!questionBankRepository.existsById(id)) {
            throw new RuntimeException("题目不存在，ID: " + id);
        }
        questionBankRepository.deleteById(id);
    }

    /**
     * 根据ID查询题目
     */
    public QuestionBank getQuestionById(Long id) {
        return questionBankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在，ID: " + id));
    }

    /**
     * 获取所有题目类型
     */
    public List<Map<String, Object>> getAllQuestionTypes() {
        List<QuestionType> types = questionTypeRepository.findAllByOrderBySortAsc();

        return types.stream()
                .map(t -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", t.getId());
                    item.put("name", t.getName());
                    item.put("sort", t.getSort());
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ==================== 新增的方法 ====================

    /**
     * 根据技术模块获取题目列表（分页）- 供新接口使用
     * @param module 技术模块（Java/Web/Python/Test）
     * @param page 页码（从1开始）
     * @param pageSize 每页条数
     * @param category 题目类别（技术知识/项目经历/场景题/行为题）
     * @return 分页结果（符合接口文档格式）
     */
    public Map<String, Object> getQuestionsByModule(String module, int page, int pageSize, String category) {
        // 1. 处理分页参数
        int pageIndex = page - 1;
        if (pageIndex < 0) {
            pageIndex = 0;
        }

        // 2. 创建分页请求（按ID倒序，最新在前）
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("id").descending());

        // 3. 根据类别获取对应的typeId
        Integer typeId = null;
        if (StringUtils.hasText(category)) {
            Optional<QuestionType> typeOpt = questionTypeRepository.findByName(category);
            if (typeOpt.isPresent()) {
                typeId = typeOpt.get().getId();
                log.debug("类别 {} 映射到 typeId: {}", category, typeId);
            } else {
                log.warn("无效的题目类别: {}", category);
                throw new IllegalArgumentException("无效的题目类别: " + category);
            }
        }

        // 4. 执行分页查询
        log.debug("查询参数 - module: {}, typeId: {}, page: {}", module, typeId, page);
        Page<QuestionBank> pageData = questionBankRepository.findByModuleAndCategory(module, typeId, pageable);

        // 5. 获取类型名称映射
        Map<Integer, String> typeNameMap = getTypeNameMap();
        log.debug("类型名称映射: {}", typeNameMap);

        // 6. 转换为接口要求的格式
        List<Map<String, Object>> questionList = pageData.getContent().stream()
                .map(q -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", q.getId());
                    item.put("title", q.getName());
                    // 使用类型名称映射，如果找不到则使用默认值
                    String typeName = typeNameMap.get(q.getTypeId());
                    if (typeName == null) {
                        log.warn("未找到类型ID {} 对应的名称，题目ID: {}", q.getTypeId(), q.getId());
                        typeName = "技术知识"; // 默认值，可以根据业务调整
                    }
                    item.put("category", typeName);
                    item.put("content", q.getDescription());

                    // 可选扩展字段
                    if (q.getDifficulty() != null) {
                        item.put("difficulty", q.getDifficulty());
                    }
                    if (q.getCreatedAt() != null) {
                        item.put("createTime", q.getCreatedAt().toString());
                    }

                    return item;
                })
                .collect(Collectors.toList());

        // 7. 封装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageData.getTotalElements());
        result.put("list", questionList);

        log.info("返回 {} 条题目，总数: {}", questionList.size(), pageData.getTotalElements());
        return result;
    }

    /**
     * 根据类型ID获取题库列表（供/api/question-banks使用）
     */
    public Map<String, Object> getQuestionBanksByType(Integer typeId) {
        // 创建分页参数（查询所有）
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").descending());

        // 执行分页查询
        Page<QuestionBank> page = questionBankRepository.findByTypeId(typeId, pageable);
        List<QuestionBank> questions = page.getContent();

        // 获取类型名称
        String typeName = questionTypeRepository.findById(typeId)
                .map(QuestionType::getName)
                .orElse("未知类型");

        // 转换为接口要求的格式
        List<Map<String, Object>> list = questions.stream()
                .map(q -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", q.getId());
                    item.put("name", q.getName());
                    item.put("description", q.getDescription());
                    item.put("type_id", q.getTypeId());
                    item.put("type_name", typeName);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", questions.size());
        result.put("list", list);

        return result;
    }

    /**
     * 从Map创建题目（管理后台用）
     */
    public QuestionBank createQuestionFromMap(Map<String, Object> params) {
        QuestionBank question = new QuestionBank();

        if (params.get("typeId") != null) {
            question.setTypeId(Integer.parseInt(params.get("typeId").toString()));
        }
        if (params.get("name") != null) {
            question.setName(params.get("name").toString());
        }
        if (params.get("description") != null) {
            question.setDescription(params.get("description").toString());
        }
        if (params.get("jobPosition") != null) {
            question.setJobPosition(params.get("jobPosition").toString());
        }
        if (params.get("difficulty") != null) {
            question.setDifficulty(Integer.parseInt(params.get("difficulty").toString()));
        }

        return createQuestion(question);
    }

    /**
     * 从Map更新题目（管理后台用）
     */
    public QuestionBank updateQuestionFromMap(Map<String, Object> params) {
        if (!params.containsKey("id") || params.get("id") == null) {
            throw new RuntimeException("题目ID不能为空");
        }

        Long id = Long.parseLong(params.get("id").toString());
        QuestionBank question = getQuestionById(id);

        if (params.get("typeId") != null) {
            question.setTypeId(Integer.parseInt(params.get("typeId").toString()));
        }
        if (params.get("name") != null) {
            question.setName(params.get("name").toString());
        }
        if (params.get("description") != null) {
            question.setDescription(params.get("description").toString());
        }
        if (params.get("jobPosition") != null) {
            question.setJobPosition(params.get("jobPosition").toString());
        }
        if (params.get("difficulty") != null) {
            question.setDifficulty(Integer.parseInt(params.get("difficulty").toString()));
        }

        return questionBankRepository.save(question);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取所有题目类型的ID->名称映射
     */
    private Map<Integer, String> getTypeNameMap() {
        List<QuestionType> types = questionTypeRepository.findAll();

        // 添加日志，方便调试
        log.debug("获取到的类型列表: {}", types.stream()
                .map(t -> t.getId() + "=" + t.getName())
                .collect(Collectors.joining(", ")));

        return types.stream()
                .collect(Collectors.toMap(
                        QuestionType::getId,
                        QuestionType::getName,
                        (v1, v2) -> v1
                ));
    }

    /**
     * 校验题目参数
     */
    private void validateQuestion(QuestionBank question) {
        if (question == null) {
            throw new RuntimeException("题目信息不能为空");
        }
        if (question.getTypeId() == null) {
            throw new RuntimeException("题目类型ID不能为空");
        }
        if (!StringUtils.hasText(question.getName())) {
            throw new RuntimeException("题目名称不能为空");
        }
        if (!StringUtils.hasText(question.getDescription())) {
            throw new RuntimeException("题目内容不能为空");
        }
    }

    /**
     * 检查题目类型是否存在
     */
    private void checkTypeExists(Integer typeId) {
        if (!questionTypeRepository.existsById(typeId)) {
            throw new RuntimeException("题目类型不存在，ID: " + typeId);
        }
    }
}