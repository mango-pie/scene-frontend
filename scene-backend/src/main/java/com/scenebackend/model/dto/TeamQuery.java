package com.scenebackend.model.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

/**
 * 队伍查询参数
 */
@Setter
@Getter
public class TeamQuery {


    @TableId(type = IdType.AUTO)
        private Long id;
    private String name;
    private String description;
    private Integer maxNum;
    private Long userId;

}
