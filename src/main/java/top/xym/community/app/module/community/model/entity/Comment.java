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
@TableName("t_comment")
public class Comment {

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    @TableField("article_id")
    private Integer articleId;

    @TableField("user_id")
    private Integer userId;

    @TableField("parent_comment_id")
    private Integer parentCommentId;

    @TableField("to_user_id")
    private Integer toUserId;

    @TableField("content")
    private String content;

    @TableField("img")
    private String img;

    @TableField("like_count")
    private Long likeCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;
}