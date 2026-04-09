package org.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.entity.ResumeParseRequest;
import org.demo.entity.ResumeParseResult;
import org.demo.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.demo.service.AnalysisService;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private AIService aiService;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisService analysisService;

    /**
     * 简历解析接口
     * POST /api/resume/parse
     *
     * 请求体：
     * {
     *   "resumeContent": "简历文本内容...",
     *   "jobPosition": "Java后端"  // 可选
     * }
     */
    @PostMapping("/parse")
    public Map<String, Object> parseResume(@RequestBody ResumeParseRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String resumeContent = request.getResumeContent();
            String jobPosition = request.getJobPosition();

            if (resumeContent == null || resumeContent.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "简历内容不能为空");
                result.put("data", null);
                return result;
            }

            log.info("开始解析简历，内容长度: {}", resumeContent.length());

            // 调用AI解析
            String aiResponse = aiService.parseResume(resumeContent, jobPosition);

            // 解析JSON响应
            ResumeParseResult parseResult = parseAIResponseToResult(aiResponse);

            result.put("code", 200);
            result.put("message", "解析成功");
            result.put("data", parseResult);

        } catch (Exception e) {
            log.error("简历解析失败", e);
            result.put("code", 500);
            result.put("message", "简历解析失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 将AI返回的JSON字符串转换为实体对象
     */
    private ResumeParseResult parseAIResponseToResult(String aiResponse) {
        ResumeParseResult result = new ResumeParseResult();

        try {
            // 提取JSON部分
            String jsonStr = extractJson(aiResponse);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取技能
            if (parsed.containsKey("skills")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> skills = (java.util.List<String>) parsed.get("skills");
                result.setSkills(skills);
            }

            // 提取项目
            if (parsed.containsKey("projects")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> projects = (java.util.List<String>) parsed.get("projects");
                result.setProjects(projects);
            }

            // 提取学历
            if (parsed.containsKey("education")) {
                result.setEducation(parsed.get("education").toString());
            }

            // 提取工作年限
            if (parsed.containsKey("experienceYears")) {
                Object years = parsed.get("experienceYears");
                if (years instanceof Number) {
                    result.setExperienceYears(((Number) years).intValue());
                } else {
                    result.setExperienceYears(0);
                }
            }

            // 提取证书
            if (parsed.containsKey("certificates")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> certificates = (java.util.List<String>) parsed.get("certificates");
                result.setCertificates(certificates);
            }

            // 提取摘要
            if (parsed.containsKey("summary")) {
                result.setSummary(parsed.get("summary").toString());
            }

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
            result.setSkills(new java.util.ArrayList<>());
            result.setProjects(new java.util.ArrayList<>());
            result.setEducation("");
            result.setExperienceYears(0);
            result.setCertificates(new java.util.ArrayList<>());
            result.setSummary("解析失败");
        }

        return result;
    }

    /**
     * 从AI响应中提取JSON部分
     */
    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }

        return response;
    }

    /**
     * 能力缺口分析接口
     * POST /api/resume/gap
     *
     * 请求体：
     * {
     *   "skills": ["Java", "Spring Boot", "MySQL"],
     *   "jobPosition": "backend"
     * }
     */
    @PostMapping("/gap")
    public Map<String, Object> analyzeGap(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<String> skills = (List<String>) params.get("skills");
            String jobPosition = (String) params.get("jobPosition");

            if (skills == null || skills.isEmpty()) {
                result.put("code", 400);
                result.put("message", "技能列表不能为空");
                result.put("data", null);
                return result;
            }

            if (jobPosition == null || jobPosition.isEmpty()) {
                jobPosition = "backend";
            }

            log.info("开始能力缺口分析 - 岗位: {}, 技能: {}", jobPosition, skills);

            // 调用分析服务
            Map<String, Object> gapResult = analysisService.analyzeSkillGap(skills, jobPosition);

            result.put("code", 200);
            result.put("message", "分析成功");
            result.put("data", gapResult);

        } catch (Exception e) {
            log.error("能力缺口分析失败", e);
            result.put("code", 500);
            result.put("message", "分析失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

}