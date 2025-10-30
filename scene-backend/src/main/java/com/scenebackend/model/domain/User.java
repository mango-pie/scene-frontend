package com.scenebackend.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态-0正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色0-普通用户,1-管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签列表 - 数据库存储为JSON字符串，使用@JsonIgnore避免序列化
     */


    //通过 @JsonIgnore 隐藏原始字符串字段，同时提供一个返回 List 类型的 getter 方法，
    // 让 Jackson 序列化时自动生成数组格式的 JSON 字段。因此前端最终收到的是数组形式的标签数据。
    @JsonIgnore
    private String tags;

    private String profile;

    /**
     * 获取标签列表（转换为List<String>）- 给前端返回数组格式
     */
    public List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(tags, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 如果JSON解析失败，尝试处理简单的字符串格式
            if (tags.startsWith("[") && tags.endsWith("]")) {
                // 移除方括号并按逗号分割
                String content = tags.substring(1, tags.length() - 1);
                List<String> result = new ArrayList<>();
                for (String tag : content.split(",")) {
                    String trimmedTag = tag.trim();
                    // 移除引号
                    if (trimmedTag.startsWith("\"") && trimmedTag.endsWith("\"")) {
                        trimmedTag = trimmedTag.substring(1, trimmedTag.length() - 1);
                    }
                    result.add(trimmedTag);
                }
                return result;
            } else {
                // 如果不是JSON格式，直接返回包含原始字符串的列表
                List<String> result = new ArrayList<>();
                result.add(tags);
                return result;
            }
        }
    }

    /**
     * 设置标签列表（将List<String>转换为JSON字符串存储）
     */
    public void setTagList(List<String> tagsList) {
        if (tagsList == null || tagsList.isEmpty()) {
            this.tags = null;
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.tags = objectMapper.writeValueAsString(tagsList);
        } catch (JsonProcessingException e) {
            // 如果序列化失败，使用简单的字符串格式
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < tagsList.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("\"").append(tagsList.get(i)).append("\"");
            }
            sb.append("]");
            this.tags = sb.toString();
        }
        this.setTags(this.tags);
    }
    /**
     * 获取原始的tags字符串（用于数据库操作）
     */
    @JsonIgnore
    public String getTagsRaw() {
        return tags;
    }

    /**
     * 设置原始的tags字符串（从数据库读取）
     */
    public void setTagsRaw(String tags) {
        this.tags = tags;
    }
}