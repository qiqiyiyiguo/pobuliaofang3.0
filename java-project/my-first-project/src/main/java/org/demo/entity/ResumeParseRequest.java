package org.demo.entity;

/**
 * 简历解析请求实体
 */
public class ResumeParseRequest {
    private String resumeContent;  // 简历内容（纯文本）
    private String jobPosition;     // 目标岗位（可选，用于针对性解析）

    public String getResumeContent() {
        return resumeContent;
    }

    public void setResumeContent(String resumeContent) {
        this.resumeContent = resumeContent;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }
}