package top.xym.community.app.module.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.conversation.model.dto.SessionCreateRequest;
import top.xym.community.app.module.conversation.model.dto.SessionResponse;
import top.xym.community.app.module.conversation.model.entity.ConversationSession;
import top.xym.community.app.module.conversation.service.ConversationSessionService;
import top.xym.community.app.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/conversation/session")
@Tag(name = "客服会话管理", description = "会话创建、查询、删除等接口")
@RequiredArgsConstructor
public class ConversationSessionController {

    private final ConversationSessionService sessionService;

    @PostMapping
    @Operation(summary = "创建会话")
    public Result<SessionResponse> createSession(@Valid @RequestBody SessionCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        SessionResponse session = sessionService.createSession(request, userId);
        return Result.success("创建会话成功", session);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户会话列表")
    public Result<PageResponse<SessionResponse>> getUserSessions(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<SessionResponse> sessions = sessionService.getUserSessions(userId, current, size);
        return Result.success("查询成功", sessions);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "获取会话详情")
    public Result<ConversationSession> getSession(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ConversationSession session = sessionService.getSessionWithAuth(sessionId, userId);
        return Result.success("查询成功", session);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "删除会话")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        sessionService.deleteSession(sessionId, userId);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{sessionId}/close")
    @Operation(summary = "关闭会话")
    public Result<Void> closeSession(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        sessionService.closeSession(sessionId, userId);
        return Result.success("关闭成功", null);
    }
}