package top.xym.community.app.module.conversation.controller;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.conversation.service.WebSocketService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatPushController {

    @PostMapping("/push-to-user")
    public Result<?> pushToUser(@RequestBody Map<String, Object> map) {
        log.info("【8080 收到推送请求】参数：{}", map);

        Long userId = Long.valueOf(map.get("userId").toString());
        String message = (String) map.get("message");

        log.info("【8080 准备推送给小程序】userId={}, message={}", userId, message);

        // 调用你的 WebSocket 推送（这里必须是你 8080 项目里的用户 WS 类！！）
        WebSocketService.sendToUser(userId, message);

        log.info("【8080 推送小程序成功】userId={}", userId);
        return Result.success();
    }
}