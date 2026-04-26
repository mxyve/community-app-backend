package top.xym.community.app.module.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.message.model.dto.MessageResponse;
import top.xym.community.app.module.message.model.dto.MessageSendRequest;
import top.xym.community.app.module.message.service.MessageService;
import top.xym.community.app.module.oss.service.OssService;
import top.xym.community.app.utils.SecurityUtils;

import java.util.List;

/**
 * 消息相关 HTTP 接口
 * 路径前缀：/api/messages
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    private final OssService ossService;

    // 流式发送消息接口
    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "流式发送消息")
    public Flux<String> sendMessageStream(
            @RequestBody MessageSendRequest request,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");

        Long userId = SecurityUtils.getCurrentUserId();

        return messageService.sendMessageStream(request, userId);
    }

    /**
     * 查询会话消息历史（核心修改：添加Token校验）
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "查询会话消息历史")
    public Result<List<MessageResponse>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestHeader("Authorization") String authHeader // 新增：接收Authorization头
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MessageResponse> list = messageService.getSessionMessages(sessionId, userId, current, size);
        return Result.success(list);
    }

     /**
      * 批量上传聊天图片
      */
     @PostMapping(
             value = "/me/image/chat",
             consumes = MediaType.MULTIPART_FORM_DATA_VALUE
     )
     @Operation(summary = "批量上传聊天图片")
     @Parameters({
             @Parameter(name = "files", description = "图片文件", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
     })
     public Result<List<String>> uploadChatImages(@RequestParam List<MultipartFile> files) {
         List<String> urls = ossService.uploadChatImages(files);
         return Result.success(urls);
     }

}