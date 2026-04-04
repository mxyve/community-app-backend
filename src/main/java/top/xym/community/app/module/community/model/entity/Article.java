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
@TableName("t_article")
public class Article {

    @TableId(value = "article_id", type = IdType.AUTO)
    private Integer articleId;

    @TableField("user_id")
    private Integer userId;

    @TableField("tag_id")
    private Integer tagId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("img")
    private String img;

    @TableField("status")
    private Integer status;

    @TableField("view_count")
    private Long viewCount;

    @TableField("like_count")
    private Long likeCount;

    @TableField("comment_count")
    private Long commentCount;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("area")
    private String area;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    @TableField(exist = false)
    private String tagName;

    @TableField(exist = false)
    private String tagColor;

    @TableField(exist = false)
    private String icon;

    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private Boolean isLiked;
}