package top.xym.community.app.module.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.session.model.dto.SessionCreateRequest;
import top.xym.community.app.module.session.model.dto.SessionResponse;
import top.xym.community.app.module.session.service.ChatSessionService;

import static top.xym.community.app.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/api/v1/session")
@Tag(name = "会话管理")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    @Operation(summary = "创建新会话", description = "创建一个新的聊天会话，需指定标题和模型名称")
    public Result<SessionResponse> createSession(@Valid @RequestBody SessionCreateRequest request) {
        Long userId = getCurrentUserId();
        SessionResponse session = chatSessionService.createSession(request,userId);
        return Result.ok("创建会话成功", session);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "删除会话", description = "根据会话ID删除指定会话（包括关联的消息记录）")
    public Result<Void> deleteSession(
            @PathVariable @Schema(description = "会话ID") Long sessionId
    ) {
        Long userId = getCurrentUserId();
        chatSessionService.deleteSession(sessionId, userId);
        return Result.ok("会话及消息删除成功", null);
    }


}
