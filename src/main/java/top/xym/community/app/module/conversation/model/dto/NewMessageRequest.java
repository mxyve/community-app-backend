package top.xym.community.app.module.conversation.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class   NewMessageRequest {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    // 已有消息的最大ID，0表示拉全部
    private Long fromId = 0L;
}