package top.xym.community.app.module.conversation.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MessageReadRequest {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}