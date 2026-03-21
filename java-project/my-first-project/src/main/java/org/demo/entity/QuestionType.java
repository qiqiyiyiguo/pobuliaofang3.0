package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 题目类型实体类
 * 对应数据库表：question_type
 */
@Entity
@Table(name = "question_type")
public class QuestionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;  // 类型ID

    @Column(name = "name", nullable = false, length = 50)
    private String name;  // 类型名称

    @Column(name = "sort")
    private Integer sort = 0;  // 排序序号

    // ========== 以下是补充字段 ==========

    @Column(name = "description", length = 200)
    private String description;  // 类型描述

    @Column(name = "icon", length = 100)
    private String icon;  // 类型图标（可选）

    @Column(name = "color", length = 20)
    private String color;  // 显示颜色（可选）

    @Column(name = "status")
    private Integer status = 1;  // 状态：0-禁用，1-启用

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 更新时间

    // ========== 构造方法 ==========
    public QuestionType() {
    }

    public QuestionType(String name, Integer sort) {
        this.name = name;
        this.sort = sort;
    }

    // ========== Getter和Setter方法 ==========

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== 生命周期回调 ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sort == null) {
            sort = 0;
        }
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "QuestionType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sort=" + sort +
                '}';
    }
}