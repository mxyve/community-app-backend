package top.xym.community.app.module.conversation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import top.xym.community.app.module.conversation.enums.SessionStatusEnum;

import java.time.LocalDateTime;

@Data
@TableName("conversation_session")
public class ConversationSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long merchantId;

    private Long orderId;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer userUnreadCount;

    private Integer merchantUnreadCount;

    private SessionStatusEnum status;

    private Integer userDeleted;

    private Integer merchantDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}