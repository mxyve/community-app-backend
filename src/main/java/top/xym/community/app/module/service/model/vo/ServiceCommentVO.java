package top.xym.community.app.module.service.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceCommentVO {
    private Long id;
    private Long serviceId;
    private Long userId;
    private Long merchantId;
    private Long orderId;
    private Integer star;
    private String content;
    private String img;
    private Long parentCommentId;
    private Long toUserId;
    private Long replyCount;
    private Long likeCount;
    private LocalDateTime createTime;

    // 评论人信息
    private String nickName;
    private String avatar;

    // 被回复人信息
    private String toUserName;

    // 子回复列表
    private List<ServiceCommentVO> childList;
}