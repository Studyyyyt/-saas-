package com.example.springboot.entity;

import java.util.Date;

/**
 * 治疗场景步骤实体
 */
public class TreatmentSceneStep {
    private Long id;
    private Long sceneId;
    private String name;
    private Integer sortOrder;
    private String forbiddenKeywords;
    private String requiredKeywords;
    private Boolean enabled;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getForbiddenKeywords() { return forbiddenKeywords; }
    public void setForbiddenKeywords(String forbiddenKeywords) { this.forbiddenKeywords = forbiddenKeywords; }

    public String getRequiredKeywords() { return requiredKeywords; }
    public void setRequiredKeywords(String requiredKeywords) { this.requiredKeywords = requiredKeywords; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
