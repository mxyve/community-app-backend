package top.xym.community.app.module.conversation.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import top.xym.community.app.module.conversation.enums.MsgTypeEnum;
import top.xym.community.app.module.conversation.enums.ReceiverTypeEnum;

import javax.validation.constraints.NotNull;

@Data
public class MessageSendRequest {

    private Long sessionId;

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotNull(message = "接收者类型不能为空")
    private ReceiverTypeEnum receiverType;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private MsgTypeEnum msgType = MsgTypeEnum.TEXT;
}