package top.xym.community.app.module.community.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_tag")
public class Tag {

    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer tagId;

    @TableField("name")
    private String name;

    @TableField("icon")
    private String icon;

    @TableField("color")
    private String color;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    @TableField("sort_order")
    private Integer sortOrder ;
}