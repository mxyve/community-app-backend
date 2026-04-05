package top.xym.community.app.module.community.model.dto;

import lombok.Data;

@Data
public class UserCommentPageRequest {
    private Long current = 1L;
    private Long size = 10L;
}