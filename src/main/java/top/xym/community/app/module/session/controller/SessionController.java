package top.xym.community.app.module.session.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;

import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.session.model.dto.SessionCreateRequest;
import top.xym.community.app.module.session.model.dto.SessionResponse;
import top.xym.community.app.module.session.model.dto.SessionUpdateTitleRequest;
import top.xym.community.app.module.session.service.SessionService;

import static top.xym.community.app.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/api/v1/session")
@Tag(name = "会话管理", description = "聊天会话创建、查询、删除等接口")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "创建新会话", description = "创建一个新的聊天会话，需指定标题和模型名称")
    public Result<SessionResponse> createSession(
            @Valid @RequestBody SessionCreateRequest request
    ) {
        Long userId = getCurrentUserId();
        SessionResponse session = sessionService.createSession(request,userId);
        return Result.success("创建会话成功", session);
    }

    // 分页获取用户所有会话
    @GetMapping
    @Operation(summary = "分页获取用户会话", description = "分页返回当前登录用户的所有未删除会话（按创建时间倒序）")
    public Result<PageResponse<SessionResponse>> getUserSessions(
            @RequestParam(defaultValue = "1") @Schema(description = "当前页码（默认1）") Long current,
            @RequestParam(defaultValue = "10") @Schema(description = "每页条数（默认10，最大50）") Long size,
            Long userId
    ) {
        // 校验分页参数合理性
        if (current < 1) {
            return Result.error(400, "页码不能小于1");
        }
        if (size < 1 || size > 50) {
            return Result.error(400, "每页条数必须在1到50之间");
        }
        userId = getCurrentUserId();

        PageResponse<SessionResponse> sessionPage = sessionService.getUserSessions(userId, current, size);
        return Result.success("分页查询成功", sessionPage);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "删除会话", description = "根据会话ID删除指定会话（包括关联的消息记录）")
    public Result<Void> deleteSession(
            @PathVariable @Schema(description = "会话ID") Long sessionId
    ) {
        Long userId = getCurrentUserId();
        sessionService.deleteSession(sessionId, userId);
        return Result.success("会话及消息删除成功", null);
    }

    @PutMapping("/{sessionId}/title")
    @Operation(summary = "修改会话标题", description = "修改当前登录用户指定会话的标题")
    public Result<SessionResponse> updateSessionTitle(
            @PathVariable @Schema(description = "会话ID") Long sessionId,
            @Valid @RequestBody SessionUpdateTitleRequest request
    ) {
        Long userId = getCurrentUserId();
        SessionResponse session = sessionService.updateSessionTitle(sessionId, userId, request);
        return Result.success("更新会话标题成功", session);
    }

    @PostMapping("/{sessionId}/auto-title")
    @Operation(summary = "自动生成会话标题", description = "根据用户第一条消息生成并更新会话标题")
    public Result<SessionResponse> generateSessionTitle(
            @PathVariable Long sessionId,
            @RequestParam String userFirstMessage // 用户第一条提问内容
    ) {
        Long userId = getCurrentUserId();
        SessionResponse session = sessionService.generateSessionTitle(sessionId, userId, userFirstMessage);
        return Result.success("生成会话标题成功", session);
    }
}
