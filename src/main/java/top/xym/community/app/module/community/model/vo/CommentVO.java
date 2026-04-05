package top.xym.community.app.module.community.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long commentId;
    private Long articleId;
    private Long userId;
    private String nickName;
    private String avatar;
    private String content;
    private String img;
    private Long likeCount;
    private Long replyCount;
    private LocalDateTime createTime;
    private Long toUserId;

    // 回复列表（第二级）
    private List<CommentVO> childList;

    // 被回复人昵称
    private String toUserName;
}