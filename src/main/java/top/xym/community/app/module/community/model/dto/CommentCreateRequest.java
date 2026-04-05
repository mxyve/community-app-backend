package top.xym.community.app.module.community.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommentCreateRequest {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentCommentId;

    private Long toUserId;

    @NotBlank(message = "评论内容不能为空" )
    private String content;

    private String img;
}
