package top.xym.community.app.module.tenant.information.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("i_information")
public class Information {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    private String coverImg;

    private String serviceArea;

    private Integer sort;

    private Integer status;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}