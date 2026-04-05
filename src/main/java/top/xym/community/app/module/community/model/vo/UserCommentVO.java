package top.xym.community.app.module.community.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentVO {
    private Long commentId;
    private Long articleId;
    private String articleTitle;
    private String content;
    private LocalDateTime createTime;
}