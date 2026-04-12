package top.xym.community.app.module.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.conversation.model.dto.MessageReadRequest;
import top.xym.community.app.module.conversation.model.dto.MessageResponse;
import top.xym.community.app.module.conversation.model.dto.MessageSendRequest;
import top.xym.community.app.module.conversation.service.ConversationMessageService;
import top.xym.community.app.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversation/message")
@Tag(name = "客服消息管理", description = "消息发送、查询、删除等接口")
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageService messageService;

    @PostMapping
    @Operation(summary = "发送消息")
    public Result<MessageResponse> sendMessage(@Valid @RequestBody MessageSendRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        MessageResponse message = messageService.sendMessage(request, userId);
        return Result.success("发送成功", message);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取历史消息")
    public Result<List<MessageResponse>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MessageResponse> messages = messageService.getSessionMessages(sessionId, userId, current, size);
        return Result.success("查询成功", messages);
    }

    @GetMapping("/new")
    @Operation(summary = "拉取新消息（轮询专用）")
    public Result<List<MessageResponse>> getNewMessages( @RequestParam Long sessionId,
                                                         @RequestParam(defaultValue = "0") Long fromId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MessageResponse> messages = messageService.getNewMessages(
                sessionId, userId, fromId);
        return Result.success("查询成功", messages);
    }

    @PutMapping("/read")
    @Operation(summary = "标记消息已读")
    public Result<Void> markMessagesAsRead(@Valid @RequestBody MessageReadRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        messageService.markMessagesAsRead(request.getSessionId(), userId);
        return Result.success("标记已读成功", null);
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        messageService.deleteMessage(messageId, userId);
        return Result.success("删除成功", null);
    }

    @GetMapping("/unread/{sessionId}")
    @Operation(summary = "获取会话未读数")
    public Result<Integer> getUnreadCount(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Integer count = messageService.getUnreadCount(sessionId, userId);
        return Result.success("查询成功", count);
    }
}