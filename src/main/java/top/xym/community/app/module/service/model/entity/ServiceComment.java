package top.xym.community.app.module.service.model.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("service_comment")
@Data
public class ServiceComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long serviceId;
    private Long orderId;
    private Long userId;
    // 父评论ID
    private Long parentCommentId;
    // 被回复人ID
    private Long toUserId;
    // 评分字段
    private Integer star;
    // 回复数
    private Long replyCount;
    // 内容
    private String content;
    // 图片
    private String img;
    // 点赞数
    private Long likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}