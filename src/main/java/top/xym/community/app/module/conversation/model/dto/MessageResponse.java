package top.xym.community.app.module.conversation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.xym.community.app.module.conversation.enums.MsgTypeEnum;
import top.xym.community.app.module.conversation.enums.ReceiverTypeEnum;
import top.xym.community.app.module.conversation.enums.SenderTypeEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

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
    private LocalDateTime createTime;

    // 扩展字段
    private String senderName;
    private String senderAvatar;

    // AI回答语音base64
    private String aiAudio;
}