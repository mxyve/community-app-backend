package top.xym.community.app.module.conversation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.xym.community.app.module.conversation.enums.SessionStatusEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private Long userId;
    private Long merchantId;
    private Long orderId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnreadCount;
    private Integer merchantUnreadCount;
    private SessionStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展字段（关联查询用）
    // 商家名称
    private String merchantName;
    // 商家头像
    private String merchantAvatar;
    // 用户昵称
    private String userName;
    // 用户头像
    private String userAvatar;
}