package top.xym.community.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.UserEditDTO;
import top.xym.community.app.model.vo.UserInfoVO;
import top.xym.community.app.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
@Tag(name = "用户接口")
public class UserController {
    private final UserService userService;

    @GetMapping("info")
    @Operation(summary = "查询用户信息")
    public Result<UserInfoVO> userInfo() {
        return Result.ok(userService.userInfo());
    }

    @PostMapping("update")
    @Operation(summary = "修改用户信息")
    public Result<UserInfoVO> update(@RequestBody UserEditDTO userEditDTO) {
        return Result.ok(userService.updateInfo(userEditDTO));
    }
}
