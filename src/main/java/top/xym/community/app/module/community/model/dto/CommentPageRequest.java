package top.xym.community.app.module.community.model.dto;

import lombok.Data;

@Data
public class CommentPageRequest {
    private Long articleId;
    private long current = 1;
    private long size = 10;
}