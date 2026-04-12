package top.xym.community.app.module.conversation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import top.xym.community.app.module.conversation.enums.MsgTypeEnum;
import top.xym.community.app.module.conversation.enums.ReceiverTypeEnum;
import top.xym.community.app.module.conversation.enums.SenderTypeEnum;

import java.time.LocalDateTime;

@Data
@TableName("conversation_message")
public class ConversationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long senderId;

    private SenderTypeEnum senderType;

    private Long receiverId;

    private ReceiverTypeEnum receiverType;

    private MsgTypeEnum msgType;

    private String content;

    private Integer isRead;

    private LocalDateTime readTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}