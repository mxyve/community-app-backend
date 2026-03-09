package top.xym.community.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.WxLoginDTO;
import top.xym.community.app.model.vo.UserLoginVO;
import top.xym.community.app.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "手机号登录")
    public Result<UserLoginVO> loginByPhone(@RequestParam("phone") String phone,
                                            @RequestParam("code") String code) {
        return Result.ok(authService.loginByPhone(phone, code));
    }

    @PostMapping("weChatLogin")
    @Operation(summary = "微信登录")
    public Result<UserLoginVO> wxChatLogin(@RequestBody WxLoginDTO dto) {
        return Result.ok(authService.weChatLogin(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Object> logout() {
        return Result.ok();
    }

    @PostMapping("/bindPhone")
    @Operation(summary = "绑定手机号")
    public Result<String> bindPhone(@RequestParam("phone") String phone,
                                    @RequestParam("code") String code,
                                    @RequestHeader("Authorization") String accessToken) {
        authService.bindPhone(phone, code, accessToken);
        return Result.ok();
    }
}
